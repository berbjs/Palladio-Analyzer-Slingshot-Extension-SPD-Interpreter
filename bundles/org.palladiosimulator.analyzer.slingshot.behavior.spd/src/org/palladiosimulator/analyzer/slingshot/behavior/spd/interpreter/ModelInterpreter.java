package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.AggregatedStimulusAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.AnyStimulusAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ManagedElementAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ImprovedQLearningModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.QThresholdsModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.RandomModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward.RewardEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward.RewardInterpreter;
import org.palladiosimulator.spd.models.ImprovedQLearningModel;
import org.palladiosimulator.spd.models.LearningBasedModel;
import org.palladiosimulator.spd.models.QThresholdsModel;
import org.palladiosimulator.spd.models.RandomModel;
import org.palladiosimulator.spd.models.util.ModelsSwitch;
import org.palladiosimulator.spd.triggers.AGGREGATIONMETHOD;
import org.palladiosimulator.spd.triggers.stimuli.AggregatedStimulus;
import org.palladiosimulator.spd.triggers.stimuli.ManagedElementsStateStimulus;
import org.palladiosimulator.spd.triggers.stimuli.Stimulus;

public class ModelInterpreter extends ModelsSwitch<ModelEvaluator> {

    private List<ModelAggregatorWrapper<?>> getStimuliListeners(LearningBasedModel model) {
        List<ModelAggregatorWrapper<?>> stimuliListenerList = new ArrayList<>();
        for (Stimulus stimulus : model.getInputs()) {
            stimuliListenerList.add(getAggregatorForStimulus(stimulus, model));
        }
        return stimuliListenerList;
    }

    @SuppressWarnings("rawtypes")
    public ModelAggregatorWrapper getAggregatorForStimulus(Stimulus stimulus, LearningBasedModel model) {
        double windowSize = model.getInterval();
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

    private RewardEvaluator getRewardEvaluator(LearningBasedModel model) {
        RewardInterpreter rewardInterpreter = new RewardInterpreter(this, model);
        return rewardInterpreter.doSwitch(model.getReward());
    }

    @Override
    public ModelEvaluator caseRandomModel(RandomModel model) {
        return new RandomModelEvaluator(model);
    }

    @Override
    public ModelEvaluator caseQThresholdsModel(QThresholdsModel model) {
        return new QThresholdsModelEvaluator(model, getStimuliListeners(model), getRewardEvaluator(model));
    }

    @Override
    public ModelEvaluator caseImprovedQLearningModel(ImprovedQLearningModel model) {
        return new ImprovedQLearningModelEvaluator(model, getStimuliListeners(model), getRewardEvaluator(model));
    }
}
