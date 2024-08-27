package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward.RewardEvaluator;
import org.palladiosimulator.spd.models.ImprovedQLearningModel;

public class ImprovedQLearningModelEvaluator extends LearningBasedModelEvaluator {

    public ImprovedQLearningModelEvaluator(ImprovedQLearningModel model,
            List<ModelAggregatorWrapper<?>> stimuliListeners, RewardEvaluator rewardEvaluator) {
        super(stimuliListeners, rewardEvaluator);
    }

    @Override
    public int getDecision() throws Exception {
        List<Double> input = new ArrayList<>();
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : this.aggregatorList) {
            input.add(modelAggregatorWrapper.getResult());
        }
        this.rewardEvaluator.getReward();
        // TODO IMPORTANT Add model evaluation here
        return 0;
    }
}
