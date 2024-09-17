package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.NotEmittableException;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.models.ImprovedQLearningModel;

public class ImprovedQLearningModelEvaluator extends LearningBasedModelEvaluator {

    static class IntervalMapping {
        /**
         * Record for an interval
         * 
         * @param upperBound
         *            Upper bound of the interval, this is an inclusive bound
         * @param action
         *            Associated Action
         */
        record Interval(double upperBound, int action, ImprovedQLearningEntry qValues) implements Comparable<Interval> {
            @Override
            public int compareTo(Interval otherInterval) {
                return Double.compare(upperBound, otherInterval.upperBound);
            }

            public Interval movedInterval(Interval previousInterval, int newAction) {
                return previousInterval;
            }
        }

        private List<Interval> intervals;
        private double alpha;
        private int actionCount;
        private double gamma;

        /**
         * Constructs a new IntervalMapping. The initial mapping will link all states to the action
         * 0 and will need to be extended by calling {@link #adjustMapping(double, int)}
         */
        IntervalMapping(double learningRate, int actionCount, double discountFactor) {
            this.gamma = discountFactor;
            intervals = new ArrayList<>();
            intervals.add(new Interval(1.0, 0, new ImprovedQLearningEntry(learningRate, discountFactor, actionCount)));
            this.actionCount = actionCount;
            this.alpha = learningRate;
        }

        /**
         * Get the action that the given {@code state} is mapped to
         */
        int getMapping(double state) {
            return intervals.stream()
                .filter((Interval mapping) -> {
                    return state <= mapping.upperBound;
                })
                .findFirst()
                .orElse(new Interval(0.0, 0, new ImprovedQLearningEntry(alpha, gamma, actionCount))).action;
        }

        ImprovedQLearningEntry getQValues(double state) {
            return intervals.stream()
                .filter((Interval mapping) -> {
                    return state <= mapping.upperBound;
                })
                .findFirst()
                .orElse(new Interval(0.0, 0, new ImprovedQLearningEntry(alpha, gamma, actionCount))).qValues;
        }

        /**
         * Adjust the internal mapping such that the given {@code state} will map to {@code action}
         */
        void adjustMapping(double state, int action) {
            if (intervals.isEmpty()) {
                intervals.add(new Interval(1.0, action, new ImprovedQLearningEntry(alpha, gamma, actionCount)));
            } else {
                Interval previousInterval = new Interval(-1.0, 0,
                        new ImprovedQLearningEntry(alpha, gamma, actionCount));
                for (int i = 0; i < intervals.size(); i += 1) {
                    Interval interval = intervals.get(i);
                    if (state <= interval.upperBound && state > previousInterval.upperBound) {
                        // TODO IMPORTANT implement the moving of the states modeled by the Q-Value
                        // table, perhaps with the help of Interval.movedInterval and
                        // ImprovedQLearningEntry
                        if (interval.action < action) {
                            // Case: The current interval has a too low action
                            // Action: move interval s.t. the state falls into the next higher
                            // interval
                            intervals.set(i,
                                    new Interval(Math.nextAfter(state, 0.0), interval.action, interval.qValues));
                        } else if (interval.action > action) {
                            // Case: The current interval has a too high action
                            // Action: Move interval st.t. the state falls into the previous
                            // interval
                            if (i != 0) {
                                intervals.set(i - 1,
                                        new Interval(state, previousInterval.action, previousInterval.qValues));
                            } else {
                                intervals.add(0,
                                        new Interval(state, interval.action - 1, new ImprovedQLearningEntry(alpha,
                                                gamma, actionCount, interval.action - 1 - (actionCount - 1) / 2)));
                            }
                        }
                    }
                    previousInterval = interval;
                }
                Collections.sort(intervals);
                if (intervals.get(intervals.size() - 1).upperBound != 1.0) {
                    // Ensure that there is a highest interval reaching until state 1.0
                    intervals.add(new Interval(1.0, intervals.get(intervals.size() - 1).action + 1,
                            new ImprovedQLearningEntry(alpha, gamma, actionCount,
                                    intervals.get(intervals.size() - 1).qValues.getMinChange() + 1)));
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Interval interval : intervals) {
                sb.append(interval.toString());
            }
            return sb.toString();
        }
    }

    static class ImprovedQLearningEntry {
        private Double[] qValues;
        private boolean[] initializedQValues;
        private int minChange;
        private double alpha;
        private double gamma;

        ImprovedQLearningEntry(double learningRate, double discountFactor, int actionCount) {
            this(learningRate, discountFactor, actionCount, -(actionCount - 1) / 2);
        }

        ImprovedQLearningEntry(double learningRate, double gamma, int actionCount, int minChange) {
            super();
            qValues = new Double[actionCount];
            initializedQValues = new boolean[actionCount];
            Arrays.fill(initializedQValues, false);
            this.minChange = minChange;
            this.gamma = gamma;
            assert (0 <= learningRate && learningRate <= 1);
            this.alpha = learningRate;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("minChange=" + minChange + ", Q-Values=[");
            for (Double value : qValues) {
                sb.append(value)
                    .append(", ");
            }
            sb.delete(sb.length() - 2, sb.length())
                .append("]");
            return sb.toString();
        }

        int getMinChange() {
            return minChange;
        }

        void setMinChange(int minChange) {
            this.minChange = minChange;
        }

        int getOptimalAction() {
            Double maxQValue = -Double.MAX_VALUE;
            int optimalAction = 0;
            for (int i = 0; i < qValues.length; i += 1) {
                if (qValues[i] != null && qValues[i] > maxQValue) {
                    optimalAction = i;
                    maxQValue = qValues[optimalAction];
                }
            }
            return optimalAction + minChange;
        }

        double getMaxValue() {
            return Arrays.stream(qValues)
                .filter(Double.class::isInstance)
                .max(Double::compare)
                .orElse(0.0);
        }

        private void updateQValueMax(int index, double maxValue) {
            if (!initializedQValues[index]) {
                if (qValues[index] == null) {
                    qValues[index] = maxValue;
                } else {
                    qValues[index] = Double.min(qValues[index], maxValue);
                }
            }
        }

        private void updateQValueMin(int index, double minValue) {
            if (!initializedQValues[index]) {
                if (qValues[index] == null) {
                    qValues[index] = minValue;
                } else {
                    qValues[index] = Double.max(qValues[index], minValue);
                }
            }
        }

        void update(int action, double reward, double nextStateMax) {
            if (initializedQValues[action - minChange]) {
                qValues[action - minChange] = (1 - alpha) * qValues[action - minChange]
                        + alpha * (reward + gamma * nextStateMax);
            } else {
                qValues[action - minChange] = alpha * reward;
                initializedQValues[action - minChange] = true;
                // Basically treat this as though it was initialized as 0 (Realistic initialization)
            }
            Double minApproximatedValue = Math.nextAfter(qValues[action - minChange], Double.MAX_VALUE);
            Double maxApproximatedValue = Math.nextAfter(qValues[action - minChange], -Double.MAX_VALUE);
            if (reward < 0 && action > 0) {
                // Extrapolation Rule 1
                for (int i = action - minChange + 1; i < qValues.length; i += 1) {
                    updateQValueMin(i, minApproximatedValue);
                }
            } else if (reward < 0) {
                // Extrapolation Rule 2
                for (int i = 0; i < qValues.length; i += 1) {
                    if (i + minChange < action) {
                        updateQValueMax(i, maxApproximatedValue);
                    } else if (i + minChange > action) {
                        updateQValueMin(i, minApproximatedValue);
                    }
                }
            } else if (reward > 0) {
                // Extrapolation Rule 3
                for (int i = action - minChange + 1; i < qValues.length; i += 1) {
                    updateQValueMax(i, maxApproximatedValue);
                }
            } else if (action == getOptimalAction()) {
                // Extrapolation Rule 4
                for (int i = 0; i < qValues.length; i += 1) {
                    if (i != action - minChange) {
                        updateQValueMax(i, maxApproximatedValue);
                    }
                }
            }
        }
    }

    private Double previousState;
    private int previousAction;
    private final double epsilon;
    private int actionCount;
    private IntervalMapping intervalMapping;
    private Random random;
    private ModelAggregatorWrapper<?> responseTimeAggregator;
    private ModelAggregatorWrapper<?> utilizationAggregator;
    private double exponentialSteepness;
    private double targetResponseTime;
    private static final Logger LOGGER = Logger.getLogger(ImprovedQLearningModelEvaluator.class);

    public ImprovedQLearningModelEvaluator(ImprovedQLearningModel model,
            ModelAggregatorWrapper<?> modelAggregatorWrapper) {
        super(Collections.singletonList(modelAggregatorWrapper), false, true);
        if (model.getTargetResponseTime() <= 0) {
            throw new IllegalArgumentException("The target response time must be greater than zero");
        }
        ModelInterpreter modelInterpreter = new ModelInterpreter();
        this.exponentialSteepness = model.getExponentialSteepness();
        this.targetResponseTime = model.getTargetResponseTime();
        this.actionCount = model.getActionCount();
        this.responseTimeAggregator = modelInterpreter.getAggregatorForStimulus(model.getResponseTimeStimulus(), model);
        this.utilizationAggregator = modelInterpreter.getAggregatorForStimulus(model.getUtilizationStimulus(), model);
        epsilon = model.getEpsilon();
        if (model.getActionCount() % 2 == 0) {
            throw new IllegalArgumentException("The action count must be odd");
        }
        intervalMapping = new IntervalMapping(model.getLearningRate(), model.getActionCount(),
                model.getDiscountFactor());
        random = new Random();
    }

    @Override
    public int getDecision() throws NotEmittableException {
        Double input = aggregatorList.get(0)
            .getResult();
        double actualResponseTime = responseTimeAggregator.getResult();
        double utilization = utilizationAggregator.getResult();
        double reward = (1 - Math.exp(-exponentialSteepness * (1 - (actualResponseTime / targetResponseTime))))
                / (1 - utilization);
        if (previousState != null) {
            update(reward, intervalMapping.getQValues(input)
                .getMaxValue());
        }
        previousState = input;
        previousAction = evaluateState(input);
        return previousAction;
    }

    private int evaluateState(Double state) {
        // Epsilon-Greedy exploratory action
        LOGGER.debug("Current state: " + state);
        LOGGER.debug("Current mapping: " + intervalMapping.getMapping(state));
        if (Math.random() < epsilon) {
            // TODO IMPORTANT Should ideally be performed "only around the boundary between adjacent
            // states and only using the adjacent actions"
            LOGGER.debug("Performed Epsilon-Action!");
            return random.nextInt(intervalMapping.getMapping(state) - (actionCount - 1) / 2,
                    intervalMapping.getMapping(state) + (actionCount - 1) / 2 + 1);
        } else {
            return intervalMapping.getMapping(state);
        }
    }

    @Override
    void recordRewardMeasurement(MeasurementMade measurement) {
        this.utilizationAggregator.aggregateMeasurement(measurement);
        this.responseTimeAggregator.aggregateMeasurement(measurement);
    }

    private void update(double reward, double nextStateMax) {
        ImprovedQLearningEntry entry = intervalMapping.getQValues(previousState);
        // TODO IMPORTANT update Q-Values here
        entry.update(previousAction, reward, nextStateMax);
        int optimalAction = entry.getOptimalAction();
        if (optimalAction != previousAction) {
            this.intervalMapping.adjustMapping(previousState, optimalAction);
        }
        LOGGER.debug("Current interval mapping: " + intervalMapping);
    }
}
