package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.spd.adjustments.models.rewards.FunctionReward;
import org.palladiosimulator.spd.adjustments.models.rewards.SLOReward;
import org.palladiosimulator.spd.adjustments.models.rewards.StaticReward;
import org.palladiosimulator.spd.adjustments.models.rewards.UtilizationReward;
import org.palladiosimulator.spd.adjustments.models.rewards.util.RewardsSwitch;

public final class RewardInterpreter extends RewardsSwitch<RewardEvaluator> {

    private ModelInterpreter modelInterpreter;

    public RewardInterpreter(ModelInterpreter modelInterpreter) {
        this.modelInterpreter = modelInterpreter;
    }

    @Override
    public RewardEvaluator caseUtilizationReward(UtilizationReward object) {
        return new UtilizationRewardEvaluator(object, this.modelInterpreter);
    }

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
            return new FunctionRewardEvaluator(object, this);
        } catch (Exception e) {
            return null;
        }
    }

}