package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import org.palladiosimulator.spd.adjustments.models.rewards.FunctionReward;
import org.palladiosimulator.spd.adjustments.models.rewards.SLOReward;
import org.palladiosimulator.spd.adjustments.models.rewards.StaticReward;
import org.palladiosimulator.spd.adjustments.models.rewards.util.RewardsSwitch;

public final class RewardInterpreter extends RewardsSwitch<RewardEvaluator> {

    @Override
    public RewardEvaluator caseStaticReward(StaticReward object) {
        return new StaticRewardEvaluator(object);
    }

    @Override
    public RewardEvaluator caseSLOReward(SLOReward rewardObject) {
        return new SloRewardEvaluator(rewardObject);
    }

    @Override
    public RewardEvaluator caseFunctionReward(FunctionReward object) {
        try {
            return new FunctionRewardEvaluator(object);
        } catch (Exception e) {
            return null;
        }
    }

}