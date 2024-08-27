package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.spd.models.LearningBasedModel;
import org.palladiosimulator.spd.models.rewards.FunctionReward;
import org.palladiosimulator.spd.models.rewards.SLOReward;
import org.palladiosimulator.spd.models.rewards.StaticReward;
import org.palladiosimulator.spd.models.rewards.UtilizationReward;
import org.palladiosimulator.spd.models.rewards.util.RewardsSwitch;

public final class RewardInterpreter extends RewardsSwitch<RewardEvaluator> {

    private LearningBasedModel model;
    private ModelInterpreter modelInterpreter;

    public RewardInterpreter(ModelInterpreter modelInterpreter, LearningBasedModel model) {
        this.modelInterpreter = modelInterpreter;
        this.model = model;
    }

    @Override
    public RewardEvaluator caseUtilizationReward(UtilizationReward object) {
        return new UtilizationRewardEvaluator(object, this.modelInterpreter, this.model);
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