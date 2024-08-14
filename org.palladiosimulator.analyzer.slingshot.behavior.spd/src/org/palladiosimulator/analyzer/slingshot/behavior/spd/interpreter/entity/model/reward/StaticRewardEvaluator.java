package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import javax.measure.Measure;

import org.palladiosimulator.spd.adjustments.models.rewards.StaticReward;

public class StaticRewardEvaluator extends RewardEvaluator {
    private double rewardValue;

    public StaticRewardEvaluator(StaticReward reward) {
        this.rewardValue = reward.getValue();
    }

    @Override
    public double getReward(Measure measure) {
        // TODO Auto-generated method stub
        return this.rewardValue;
    }

}
