package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.monitor.data.entities.SlingshotMeasuringValue;
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
            return measures.stream()
                .reduce(0.0, Double::sum);
        }
    }

    class SubtractionFunction extends Function {
        @Override
        double getFunctionResult(List<Double> measures) {
            measures.get(0);
            return measures.stream()
                .skip(1)
                .reduce(measures.get(0), (a, b) -> {
                    return a - b;
                });
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
                LOGGER.error("Exponential takes only one input, but " + reward.getRewards()
                    .size() + " inputs were specified");
                throw new Exception("Exponential takes only one input, but " + reward.getRewards()
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

    @Override
    public double getReward() {
        List<Double> inputs = new ArrayList<Double>(this.inputRewards.size());
        for (RewardEvaluator evaluator : this.inputRewards) {
            inputs.add(evaluator.getReward());
        }
        return this.function.getFunctionResult(inputs);
    }

    @Override
    public void addMeasurement(SlingshotMeasuringValue measure) {
        for (RewardEvaluator evaluator : this.inputRewards) {
            evaluator.addMeasurement(measure);
        }
    }

}
