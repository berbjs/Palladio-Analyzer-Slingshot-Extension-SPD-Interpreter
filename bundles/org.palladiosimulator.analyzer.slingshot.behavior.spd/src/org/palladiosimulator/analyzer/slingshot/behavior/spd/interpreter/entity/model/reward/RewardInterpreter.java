package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.spd.models.LearningBasedModel;
import org.palladiosimulator.spdmodelreward.FunctionReward;
import org.palladiosimulator.spdmodelreward.SLOReward;
import org.palladiosimulator.spdmodelreward.StaticReward;
import org.palladiosimulator.spdmodelreward.UtilizationReward;
import org.palladiosimulator.spdmodelreward.util.SpdmodelrewardSwitch;

public final class RewardInterpreter extends SpdmodelrewardSwitch<RewardEvaluator> {

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