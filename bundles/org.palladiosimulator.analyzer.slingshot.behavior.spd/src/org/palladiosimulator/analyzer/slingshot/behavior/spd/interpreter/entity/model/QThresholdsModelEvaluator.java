package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ImprovedQLearningModelEvaluator.ImprovedQLearningEntry;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward.RewardEvaluator;
import org.palladiosimulator.spd.models.QThresholdsModel;

public class QThresholdsModelEvaluator extends LearningBasedModelEvaluator {

    private Map<Double, ImprovedQLearningEntry> qValuesLowerThreshold = new HashMap<>();
    private Map<Double, ImprovedQLearningEntry> qValuesUpperThreshold = new HashMap<>();
    private ModelAggregatorWrapper<?> stimulusListener;
    private ModelAggregatorWrapper<?> utilizationStimulus;

    public QThresholdsModelEvaluator(QThresholdsModel model, ModelAggregatorWrapper<?> stimulusListener,
            ModelAggregatorWrapper<?> utilizationStimulus, RewardEvaluator rewardEvaluator) {
        super(Arrays.asList(stimulusListener, utilizationStimulus), rewardEvaluator);
        this.stimulusListener = stimulusListener;
        this.utilizationStimulus = utilizationStimulus;
    }

    @Override
    public int getDecision() throws Exception {
        Double input = aggregatorList.get(0)
            .getResult();

        double reward = rewardEvaluator.getReward();
        // TODO IMPORTANT Add model evaluation here
        return 0;
    }
}
