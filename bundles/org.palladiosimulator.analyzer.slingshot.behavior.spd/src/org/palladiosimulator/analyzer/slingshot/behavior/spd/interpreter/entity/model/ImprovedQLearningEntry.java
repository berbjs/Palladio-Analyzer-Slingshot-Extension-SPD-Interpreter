package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.Arrays;

class ImprovedQLearningEntry {
    private final Double[] qValues;
    private final boolean[] initializedQValues;
    private int minChange;
    private final double alpha;
    private final double gamma;

    ImprovedQLearningEntry(final double learningRate, final double discountFactor, final int actionCount) {
        this(learningRate, discountFactor, actionCount, -(actionCount - 1) / 2);
    }

    ImprovedQLearningEntry(final double learningRate, final double gamma, final int actionCount, final int minChange) {
        super();
        this.qValues = new Double[actionCount];
        this.initializedQValues = new boolean[actionCount];
        Arrays.fill(this.initializedQValues, false);
        this.minChange = minChange;
        this.gamma = gamma;
        assert (0 <= learningRate && learningRate <= 1);
        this.alpha = learningRate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("minChange=" + this.minChange + ", Q-Values=[");
        for (final Double value : this.qValues) {
            sb.append(value)
                .append(", ");
        }
        sb.delete(sb.length() - 2, sb.length())
            .append("]");
        return sb.toString();
    }

    int getMinChange() {
        return this.minChange;
    }

    void setMinChange(final int minChange) {
        this.minChange = minChange;
    }

    int getOptimalAction() {
        Double maxQValue = -Double.MAX_VALUE;
        int optimalAction = 0;
        for (int i = 0; i < this.qValues.length; i += 1) {
            if (this.qValues[i] != null && this.qValues[i] > maxQValue) {
                optimalAction = i;
                maxQValue = this.qValues[optimalAction];
            }
        }
        return optimalAction + this.minChange;
    }

    double getMaxValue() {
        return Arrays.stream(this.qValues)
            .filter(Double.class::isInstance)
            .max(Double::compare)
            .orElse(0.0);
    }

    private void updateQValueMax(final int index, final double maxValue) {
        if (!this.initializedQValues[index]) {
            if (this.qValues[index] == null) {
                this.qValues[index] = maxValue;
            } else {
                this.qValues[index] = Double.min(this.qValues[index], maxValue);
            }
        }
    }

    private void updateQValueMin(final int index, final double minValue) {
        if (!this.initializedQValues[index]) {
            if (this.qValues[index] == null) {
                this.qValues[index] = minValue;
            } else {
                this.qValues[index] = Double.max(this.qValues[index], minValue);
            }
        }
    }

    void update(final int action, final double reward, final double nextStateMax) {
        if (this.initializedQValues[action - this.minChange]) {
            this.qValues[action - this.minChange] = (1 - this.alpha) * this.qValues[action - this.minChange]
                    + this.alpha * (reward + this.gamma * nextStateMax);
        } else {
            this.qValues[action - this.minChange] = this.alpha * reward;
            this.initializedQValues[action - this.minChange] = true;
            // Basically treat this as though it was initialized as 0 (Realistic initialization)
        }
        final Double minApproximatedValue = Math.nextAfter(this.qValues[action - this.minChange], Double.MAX_VALUE);
        final Double maxApproximatedValue = Math.nextAfter(this.qValues[action - this.minChange], -Double.MAX_VALUE);
        if (reward < 0 && action > 0) {
            // Extrapolation Rule 1
            for (int i = action - this.minChange + 1; i < this.qValues.length; i += 1) {
                this.updateQValueMin(i, minApproximatedValue);
            }
        } else if (reward < 0) {
            // Extrapolation Rule 2
            // TODO should only be performed for "higher utilization and higher rates"
            for (int i = 0; i < this.qValues.length; i += 1) {
                if (i + this.minChange < action) {
                    this.updateQValueMax(i, maxApproximatedValue);
                } else if (i + this.minChange > action) {
                    this.updateQValueMin(i, minApproximatedValue);
                }
            }
        } else if (reward > 0) {
            // Extrapolation Rule 3
            // TODO should only be performed for "lower utilization and lower rates"
            for (int i = action - this.minChange + 1; i < this.qValues.length; i += 1) {
                this.updateQValueMax(i, maxApproximatedValue);
            }
        } else if (action == this.getOptimalAction()) {
            // Extrapolation Rule 4
            for (int i = 0; i < this.qValues.length; i += 1) {
                if (i != action - this.minChange) {
                    this.updateQValueMax(i, maxApproximatedValue);
                }
            }
        }
    }
}