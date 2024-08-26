package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.AggregatedStimulusAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.AnyStimulusAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ManagedElementAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ImprovedQLearningModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.QThresholdsModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.RandomModelEvaluator;
import org.palladiosimulator.spd.models.ImprovedQLearningModel;
import org.palladiosimulator.spd.models.QThresholdsModel;
import org.palladiosimulator.spd.models.RandomModel;
import org.palladiosimulator.spd.models.util.ModelsSwitch;
import org.palladiosimulator.spd.triggers.AGGREGATIONMETHOD;
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
            stimuliListenerList.add(getAggregatorForStimulus(stimulus));
        }
        return stimuliListenerList;
    }

    @SuppressWarnings("rawtypes")
    public ModelAggregatorWrapper getAggregatorForStimulus(Stimulus stimulus) {
        int windowSize = ((int) ((ModelBasedAdjustment) this.triggerInterpreter.policy.getAdjustmentType())
            .getUsedModel()
            .getInterval());
        if (stimulus instanceof AggregatedStimulus aggregatedStimulus) {
            return new AggregatedStimulusAggregator<>(aggregatedStimulus, windowSize);
        } else if (stimulus instanceof ManagedElementsStateStimulus managedElementsStateStimulus) {
            return new ManagedElementAggregator<>(managedElementsStateStimulus, windowSize);
        } else {
            // TODO IMPORTANT currently using average aggregation by default for non-aggregated
            // stimuli, this might need to be changed
            return new AnyStimulusAggregator<>(stimulus, windowSize, AGGREGATIONMETHOD.AVERAGE);
        }
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
