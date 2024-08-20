package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.adjustments.models.rewards.StaticReward;

public class StaticRewardEvaluator extends RewardEvaluator {
    private double rewardValue;

    public StaticRewardEvaluator(StaticReward reward) {
        this.rewardValue = reward.getValue();
    }

    @Override
    public double getReward() {
        return this.rewardValue;
    }

    @Override
    public void addMeasurement(MeasurementMade measurementMade) {
        // As the reward is static, no measurement aggregation is performed
        return;
    }

}
