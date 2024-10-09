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
    record Interval(double upperBound, int action, ReducedActionSpaceCalculator qValues)
            implements Comparable<Interval> {
        @Override
        public int compareTo(final Interval otherInterval) {
            return Double.compare(this.upperBound, otherInterval.upperBound);
        }
    }

    private final List<Interval> intervals;
    private final double alpha;
    private final int actionCount;
    private final double gamma;

    /**
     * Constructs a new IntervalMapping. The initial mapping will link all states to the action 0
     * and will need to be extended by calling {@link #adjustMapping(double, int)}
     */
    IntervalMapping(final double learningRate, final int actionCount, final double discountFactor) {
        this.gamma = discountFactor;
        this.intervals = new ArrayList<>();
        this.intervals.add(new Interval(1.0, 0,
                new ReducedActionSpaceCalculator(learningRate, discountFactor, actionCount, true)));
        this.actionCount = actionCount;
        this.alpha = learningRate;
    }

    /**
     * Get the action that the given {@code state} is mapped to
     */
    int getMapping(final double state) {
        return this.intervals.stream()
            .filter((final Interval mapping) -> {
                return state <= mapping.upperBound;
            })
            .findFirst()
            .orElse(new Interval(0.0, 0,
                    new ReducedActionSpaceCalculator(this.alpha, this.gamma, this.actionCount, true))).action;
    }

    ReducedActionSpaceCalculator getQValues(final double state) {
        return this.intervals.stream()
            .filter((final Interval mapping) -> {
                return state <= mapping.upperBound;
            })
            .findFirst()
            .orElse(new Interval(0.0, 0,
                    new ReducedActionSpaceCalculator(this.alpha, this.gamma, this.actionCount, true))).qValues;
    }

    /**
     * Adjust the internal mapping such that the given {@code state} will map to {@code action}
     */
    void adjustMapping(final double state, final int action) {
        if (this.intervals.isEmpty()) {
            this.intervals.add(new Interval(1.0, action,
                    new ReducedActionSpaceCalculator(this.alpha, this.gamma, this.actionCount, true)));
        } else {
            Interval previousInterval = new Interval(-1.0, 0,
                    new ReducedActionSpaceCalculator(this.alpha, this.gamma, this.actionCount, true));
            for (int i = 0; i < this.intervals.size(); i += 1) {
                final Interval interval = this.intervals.get(i);
                if (state <= interval.upperBound && state > previousInterval.upperBound) {
                    if (interval.action < action) {
                        // Case: The current interval has a too low action
                        // Action: move interval s.t. the state falls into the next higher
                        // interval
                        this.intervals.set(i,
                                new Interval(Math.nextAfter(state, 0.0), interval.action, interval.qValues));
                    } else if (interval.action > action) {
                        // Case: The current interval has a too high action
                        // Action: Move interval st.t. the state falls into the previous
                        // interval
                        if (i != 0) {
                            this.intervals.set(i - 1,
                                    new Interval(state, previousInterval.action, previousInterval.qValues));
                        } else {
                            this.intervals.add(0,
                                    new Interval(state, interval.action - 1,
                                            new ReducedActionSpaceCalculator(this.alpha, this.gamma, this.actionCount,
                                                    interval.action - 1 - (this.actionCount - 1) / 2, true)));
                        }
                    }
                }
                previousInterval = interval;
            }
            Collections.sort(this.intervals);
            if (this.intervals.get(this.intervals.size() - 1).upperBound != 1.0) {
                // Ensure that there is a highest interval reaching until state 1.0
                this.intervals.add(new Interval(1.0, this.intervals.get(this.intervals.size() - 1).action + 1,
                        new ReducedActionSpaceCalculator(this.alpha, this.gamma, this.actionCount,
                                this.intervals.get(this.intervals.size() - 1).qValues.getMinChange() + 1, true)));
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Interval interval : this.intervals) {
            sb.append(interval.toString());
        }
        return sb.toString();
    }
}