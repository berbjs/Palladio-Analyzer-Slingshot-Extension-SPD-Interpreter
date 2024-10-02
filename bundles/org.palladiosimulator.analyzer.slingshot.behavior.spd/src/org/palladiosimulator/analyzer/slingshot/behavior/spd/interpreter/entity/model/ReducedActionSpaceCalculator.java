package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.Arrays;

class ReducedActionSpaceCalculator {
    protected Double[] qValues;
    protected boolean[] initializedQValues;
    protected int minChange;
    private final double alpha;
    private final double gamma;

    ReducedActionSpaceCalculator(final double learningRate, final double discountFactor, final int actionCount) {
        this(learningRate, discountFactor, actionCount, -(actionCount - 1) / 2);
    }

    ReducedActionSpaceCalculator(final double learningRate, final double gamma, final int actionCount,
            final int minChange) {
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

    void update(final int action, final double reward, final double nextStateMax) {
        if (this.initializedQValues[action - this.minChange]) {
            this.qValues[action - this.minChange] = (1 - this.alpha) * this.qValues[action - this.minChange]
                    + this.alpha * (reward + this.gamma * nextStateMax);
        } else {
            this.qValues[action - this.minChange] = this.alpha * reward;
            this.initializedQValues[action - this.minChange] = true;
            // Basically treat this as though it was initialized as 0 (Realistic initialization)
        }
        if (this.getOptimalAction() != this.minChange + (this.qValues.length - 1) / 2) {
            final int actionCount = this.qValues.length;
            final Double[] newQValues = new Double[actionCount];
            final boolean[] newInitializedQValues = new boolean[actionCount];
            if (this.getOptimalAction() > this.minChange + (this.qValues.length - 1) / 2) {
                // Shift to the right
                this.minChange += 1;
                for (int i = 1; i < actionCount; i++) {
                    newQValues[i - 1] = this.qValues[i];
                    newInitializedQValues[i - 1] = this.initializedQValues[i];
                }
                newQValues[actionCount - 1] = 0.0;
                newInitializedQValues[actionCount - 1] = false;
            } else {
                // Shift to the left
                this.minChange -= 1;
                for (int i = 0; i < actionCount - 1; i++) {
                    newQValues[i + 1] = this.qValues[i];
                    newInitializedQValues[i + 1] = this.initializedQValues[i];
                }
                newQValues[0] = 0.0;
                newInitializedQValues[0] = false;
            }
            this.qValues = newQValues;
            this.initializedQValues = newInitializedQValues;
        }
    }
}