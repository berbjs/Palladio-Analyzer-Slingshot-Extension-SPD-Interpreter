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

    private ScalingTriggerInterpreter triggerInterpreter;

    public ModelInterpreter(ScalingTriggerInterpreter triggerInterpreter) {
        this.triggerInterpreter = triggerInterpreter;
    }

    private List<ModelAggregatorWrapper<?>> getStimuliListeners(EList<Stimulus> stimuli) {
        List<ModelAggregatorWrapper<?>> stimuliListenerList = new ArrayList<>();
        for (Stimulus stimulus : stimuli) {
            stimuliListenerList.add(extracted(stimulus));
        }
        return stimuliListenerList;
    }

    @SuppressWarnings("rawtypes")
    public ModelAggregatorWrapper extracted(Stimulus stimulus) {
        int windowSize = ((int) ((ModelBasedAdjustment) this.triggerInterpreter.policy.getAdjustmentType())
            .getUsedModel()
            .getInterval());
        if (stimulus instanceof AggregatedStimulus aggregatedStimulus) {
            return new AnyStimulusAggregator<>(aggregatedStimulus, this.triggerInterpreter.policy.getTargetGroup(),
                    windowSize, MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                    MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
        } else if (stimulus instanceof ManagedElementsStateStimulus managedElementsStateStimulus) {
            return new ManagedElementAggregator<>(managedElementsStateStimulus,
                    this.triggerInterpreter.policy.getTargetGroup(), windowSize,
                    MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                    MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
        }
        return null; // TODO IMPORTANT FIXME
    }

    @Override
    public ModelEvaluator caseRandomModel(RandomModel model) {
        return new RandomModelEvaluator();
    }

    @Override
    public ModelEvaluator caseQThresholdsModel(QThresholdsModel model) {
        return new QThresholdsModelEvaluator(model, getStimuliListeners());
    }

    @Override
    public ModelEvaluator caseImprovedQLearningModel(ImprovedQLearningModel model) {
        return new ImprovedQLearningModelEvaluator(model, getStimuliListeners());
    }
}
