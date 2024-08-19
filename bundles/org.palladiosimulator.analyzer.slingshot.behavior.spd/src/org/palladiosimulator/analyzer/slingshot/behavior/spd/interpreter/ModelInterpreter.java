package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.AnyStimulusAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ManagedElementAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models.ImprovedQLearningModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models.QThresholdsModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models.RandomModelEvaluator;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.spd.adjustments.models.ImprovedQLearningModel;
import org.palladiosimulator.spd.adjustments.models.QThresholdsModel;
import org.palladiosimulator.spd.adjustments.models.RandomModel;
import org.palladiosimulator.spd.adjustments.models.util.ModelsSwitch;
import org.palladiosimulator.spd.triggers.stimuli.AggregatedStimulus;
import org.palladiosimulator.spd.triggers.stimuli.ManagedElementsStateStimulus;
import org.palladiosimulator.spd.triggers.stimuli.Stimulus;

public class ModelInterpreter extends ModelsSwitch<ModelEvaluator> {

    private EList<Stimulus> stimuli;
    private ScalingTriggerInterpreter triggerInterpreter;

    public ModelInterpreter(EList<Stimulus> stimuli, ScalingTriggerInterpreter triggerInterpreter) {
        this.stimuli = stimuli;
        this.triggerInterpreter = triggerInterpreter;
    }

    private List<ModelAggregatorWrapper<?>> getStimuliListeners() {
        List<ModelAggregatorWrapper<?>> stimuliListenerList = new ArrayList<>();
        int windowSize = ((int) ((ModelBasedAdjustment) this.triggerInterpreter.policy.getAdjustmentType())
            .getUsedModel()
            .getInterval());
        for (Stimulus stimulus : this.stimuli) {
            if (stimulus instanceof AggregatedStimulus aggregatedStimulus) {
                stimuliListenerList.add(
                        new AnyStimulusAggregator<>(aggregatedStimulus, this.triggerInterpreter.policy.getTargetGroup(),
                                windowSize, MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                                MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE));
            } else if (stimulus instanceof ManagedElementsStateStimulus managedElementsStateStimulus) {
                stimuliListenerList.add(new ManagedElementAggregator<>(managedElementsStateStimulus,
                        this.triggerInterpreter.policy.getTargetGroup(), windowSize,
                        MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                        MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE));
            }
        }
        return stimuliListenerList;
    }

    @Override
    public ModelEvaluator caseRandomModel(RandomModel object) {
        return new RandomModelEvaluator();
    }

    @Override
    public ModelEvaluator caseQThresholdsModel(QThresholdsModel object) {
        return new QThresholdsModelEvaluator(object, getStimuliListeners());
    }

    @Override
    public ModelEvaluator caseImprovedQLearningModel(ImprovedQLearningModel object) {
        return new ImprovedQLearningModelEvaluator(object, getStimuliListeners());
    }
}
