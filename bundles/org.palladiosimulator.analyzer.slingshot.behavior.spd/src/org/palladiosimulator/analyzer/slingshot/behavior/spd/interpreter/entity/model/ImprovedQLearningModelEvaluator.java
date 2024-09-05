package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.NotEmittableException;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward.RewardEvaluator;
import org.palladiosimulator.spd.models.ImprovedQLearningModel;

public class ImprovedQLearningModelEvaluator extends LearningBasedModelEvaluator {

    class IntervalMapping {
        record Interval(double upperBound, int action) {
        }

        private final List<Interval> intervals;

        /**
         * Constructs a new IntervalMapping. The initial mapping will link all states to the action
         * 0 and will need to be extended by calling {@link #adjustMapping(double, int)}
         */
        IntervalMapping() {
            intervals = new ArrayList<>();
            intervals.add(new Interval(1.0, 0));
        }

        /**
         * Get the action that the given {@code state} is mapped to
         */
        int getMapping(double state) {
            return intervals.stream()
                .filter((Interval mapping) -> {
                    return state < mapping.upperBound;
                })
                .findFirst()
                .orElse(new Interval(0.0, 0)).action;
        }

        /**
         * Adjust the internal mapping such that the given {@code state} will map to {@code action}
         */
        void adjustMapping(double state, int action) {
            if (intervals.isEmpty()) {
                intervals.add(new Interval(1.0, action));
            } else {
                intervals.stream()
                    .filter(mapping -> state < mapping.upperBound)
                    .forEach(mapping -> {
                        if (mapping.action < action) {
                            // The new interval does not include state anymore, it is instead
                            // included in the next higher action
                            mapping = new Interval(state, mapping.action);
                            if (intervals.get(-1).upperBound != 1.0) {
                                // Ensure that there is a highest interval reaching until state 1.0
                                intervals.add(new Interval(1.0, intervals.get(-1).action + 1));
                            }
                            return;
                        } else if (mapping.action == action) {
                            // The interval of action should now just include the state
                            mapping = new Interval(Math.nextAfter(state, 1.0), action);
                            return;
                        }
                    });
                // We reach this case only if no interval exists yet that has a low enough action,
                // thus we construct a new interval
                intervals.add(0, new Interval(state, action));
            }
        }
    }

    class ImprovedQLearningEntry {
        private Double[] QValues;
        private int minChange;
        private int alpha;

        ImprovedQLearningEntry(double alpha) {
            super();
            QValues = new Double[5];
            minChange = -2;
            assert (0 <= alpha && alpha <= 1);
        }

        int getMinChange() {
            return minChange;
        }

        void setMinChange(int minChange) {
            this.minChange = minChange;
        }

        int getOptimalAction() {
            Double maxQValue = -Double.MAX_VALUE;
            int optimalAction = minChange;
            for (int i = 0; i < QValues.length; i += 1) {
                if (QValues[i] > maxQValue) {
                    maxQValue = QValues[optimalAction];
                    optimalAction = i;
                }
            }
            return optimalAction;
        }

        double getMaxValue() {
            return Arrays.stream(QValues)
                .max(Double::compare)
                .get();
        }

        void update(int action, double reward) {
            QValues[action - minChange] = (1 - alpha) * QValues[action - minChange] + alpha * (reward);
            if (reward < 0 && action > 0) {
                // Extrapolation Rule 1
                for (int i = action - minChange + 1; i < QValues.length; i += 1) {
                    Double minApproximatedValue = Math.nextAfter(reward, Double.MAX_VALUE);
                    if (QValues[i] == null) {
                        QValues[i] = minApproximatedValue;
                    }
                    QValues[i] = Double.max(QValues[i], minApproximatedValue);
                }
            } else if (reward < 0) {
                // TODO IMPORTANT Extrapolation Rule 2
            } else if (reward > 0) {
                // TODO IMPORTANT Extrapolation Rule 3
            } else if (action == getOptimalAction()) {
                // Extrapolation Rule 4
                for (int i = 0; i < QValues.length; i += 1) {
                    if (i != action - minChange) {
                        QValues[i] = Double.min(QValues[i], Math.nextAfter(reward, -Double.MAX_VALUE));
                    }
                }
            }
        }
    }

    private Map<Double, ImprovedQLearningEntry> Q; // TODO IMPORTANT the Q-Learning entries should
                                                   // actually be per-interval (See State 0.5-0.6 in
                                                   // paper), not per-util / per-rate
    private Double state;
    private int action;
    private final double epsilon;
    private int actionCount;
    private IntervalMapping intervalMapping;
    private Random random;
    private double alpha;

    public ImprovedQLearningModelEvaluator(ImprovedQLearningModel model,
            ModelAggregatorWrapper<?> modelAggregatorWrapper, RewardEvaluator rewardEvaluator) {
        super(Collections.singletonList(modelAggregatorWrapper), rewardEvaluator);
        epsilon = model.getEpsilon();
        actionCount = model.getActionCount();
        alpha = model.getLearningRate();
        intervalMapping = new IntervalMapping();
        random = new Random();
    }

    @Override
    public int getDecision() throws NotEmittableException {
        Double input = aggregatorList.get(0)
            .getResult();
        double reward = rewardEvaluator.getReward();
        if (state != null) {
            update(state, action, reward);
        }
        state = input;
        action = evaluateState(input);
        return action;
    }

    private int evaluateState(Double state) {
        // Epsilon-Greedy exploratory action
        if (Math.random() < epsilon) {
            // TODO IMPORTANT Should ideally be performed "only around the boundary between adjacent
            // states and only using the adjacent actions"
            return random.nextInt(0, actionCount);
        } else {
            return intervalMapping.getMapping(state);
        }
    }

    private void update(Double state, int action, double reward) {
        ImprovedQLearningEntry entry = Q.getOrDefault(state, new ImprovedQLearningEntry(alpha));
        // TODO IMPORTANT update Q-Values here
        entry.update(action, reward);
        int optimalAction = entry.getOptimalAction();
        if (optimalAction != action) {
            this.intervalMapping.adjustMapping(state, optimalAction);
        }
    }
}
