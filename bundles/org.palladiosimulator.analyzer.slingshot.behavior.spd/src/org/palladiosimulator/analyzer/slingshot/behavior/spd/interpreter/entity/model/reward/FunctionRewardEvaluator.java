package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Measure;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.palladiosimulator.spd.adjustments.models.rewards.BaseReward;
import org.palladiosimulator.spd.adjustments.models.rewards.FunctionReward;

public class FunctionRewardEvaluator extends RewardEvaluator {

    private static final Logger LOGGER = LogManager.getLogger(FunctionRewardEvaluator.class);

    abstract class Function {
        abstract double getFunctionResult(List<Double> measures);
    }

    class AdditionFunction extends Function {
        @Override
        double getFunctionResult(List<Double> measures) {
            // TODO Auto-generated method stub
            return 0;
        }
    }

    class SubtractionFunction extends Function {
        @Override
        double getFunctionResult(List<Double> measures) {
            return measures.get(0) - measures.get(1);
        }
    }

    class ExponentialFunction extends Function {
        @Override
        double getFunctionResult(List<Double> measures) {
            return Math.exp((double) measures.get(0));
        }
    }

    final Function function;
    final List<RewardEvaluator> inputRewards;

    public FunctionRewardEvaluator(FunctionReward reward) throws Exception {
        switch (reward.getAggregationMethod()) {
        case ADDITION -> {
            this.function = new AdditionFunction();
        }
        case SUBTRACTION -> {
            if (reward.getRewards()
                .size() != 2) {
                LOGGER.error("Subtraction takes only two inputs, but " + reward.getRewards()
                    .size() + " input(s) were specified");
                throw new Exception("Subtraction takes only two inputs, but " + reward.getRewards()
                    .size() + " input(s) were specified");
            }
            this.function = new SubtractionFunction();
        }
        case EXPONENTIAL -> {
            if (reward.getRewards()
                .size() != 1) {
                LOGGER.error("Subtraction takes only one input, but " + reward.getRewards()
                    .size() + " inputs were specified");
                throw new Exception("Subtraction takes only one input, but " + reward.getRewards()
                    .size() + " inputs were specified");
            }
            this.function = new ExponentialFunction();
        }
        default -> {
            LOGGER.error("Unexpected function type " + reward.getAggregationMethod() + " encountered for a reward ");
            throw new Exception(
                    "Unexpected function type " + reward.getAggregationMethod() + " encountered for a reward ");
        }
        }
        ;
        List<RewardEvaluator> inputRewards = new ArrayList<RewardEvaluator>();
        RewardInterpreter rewardInterpreter = new RewardInterpreter();
        for (BaseReward nestedReward : reward.getRewards()) {
            inputRewards.add(rewardInterpreter.doSwitch(nestedReward));
        }
        this.inputRewards = inputRewards;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public double getReward(Measure measure) {
        List<Double> inputs = new ArrayList<Double>(this.inputRewards.size());
        for (RewardEvaluator evaluator : this.inputRewards) {
            inputs.add(evaluator.getReward(measure));
        }
        return this.function.getFunctionResult(inputs);
    }

}
