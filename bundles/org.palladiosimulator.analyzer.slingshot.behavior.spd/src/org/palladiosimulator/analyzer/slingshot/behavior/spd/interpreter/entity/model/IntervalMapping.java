package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class IntervalMapping {
    /**
     * Record for an interval
     * 
     * @param upperBound
     *            Upper bound of the interval, this is an inclusive bound
     * @param action
     *            Associated Action
     */
    record Interval(double upperBound, int action, ReducedActionSpaceCalculator qValues) implements Comparable<IntervalMapping.Interval> {
        @Override
        public int compareTo(IntervalMapping.Interval otherInterval) {
            return Double.compare(upperBound, otherInterval.upperBound);
        }

        public IntervalMapping.Interval movedInterval(IntervalMapping.Interval previousInterval, int newAction) {
            return previousInterval;
        }
    }

    private List<IntervalMapping.Interval> intervals;
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
        intervals.add(new Interval(1.0, 0, new ReducedActionSpaceCalculator(learningRate, discountFactor, actionCount)));
        this.actionCount = actionCount;
        this.alpha = learningRate;
    }

    /**
     * Get the action that the given {@code state} is mapped to
     */
    int getMapping(double state) {
        return intervals.stream()
            .filter((IntervalMapping.Interval mapping) -> {
                return state <= mapping.upperBound;
            })
            .findFirst()
            .orElse(new Interval(0.0, 0, new ReducedActionSpaceCalculator(alpha, gamma, actionCount))).action;
    }

    ReducedActionSpaceCalculator getQValues(double state) {
        return intervals.stream()
            .filter((IntervalMapping.Interval mapping) -> {
                return state <= mapping.upperBound;
            })
            .findFirst()
            .orElse(new Interval(0.0, 0, new ReducedActionSpaceCalculator(alpha, gamma, actionCount))).qValues;
    }

    /**
     * Adjust the internal mapping such that the given {@code state} will map to {@code action}
     */
    void adjustMapping(double state, int action) {
        if (intervals.isEmpty()) {
            intervals.add(new Interval(1.0, action, new ReducedActionSpaceCalculator(alpha, gamma, actionCount)));
        } else {
            IntervalMapping.Interval previousInterval = new Interval(-1.0, 0,
                    new ReducedActionSpaceCalculator(alpha, gamma, actionCount));
            for (int i = 0; i < intervals.size(); i += 1) {
                IntervalMapping.Interval interval = intervals.get(i);
                if (state <= interval.upperBound && state > previousInterval.upperBound) {
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
                                    new Interval(state, interval.action - 1, new ReducedActionSpaceCalculator(alpha,
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
                        new ReducedActionSpaceCalculator(alpha, gamma, actionCount,
                                intervals.get(intervals.size() - 1).qValues.getMinChange() + 1)));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IntervalMapping.Interval interval : intervals) {
            sb.append(interval.toString());
        }
        return sb.toString();
    }
}