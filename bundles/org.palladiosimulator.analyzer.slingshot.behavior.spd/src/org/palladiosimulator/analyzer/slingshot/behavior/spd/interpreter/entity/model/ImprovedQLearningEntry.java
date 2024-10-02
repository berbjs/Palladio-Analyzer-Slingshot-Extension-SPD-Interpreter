package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

public class ImprovedQLearningEntry extends ReducedActionSpaceCalculator {

    ImprovedQLearningEntry(final double learningRate, final double gamma, final int actionCount) {
        super(learningRate, gamma, actionCount);
    }

    ImprovedQLearningEntry(final double learningRate, final double gamma, final int actionCount, final int minChange) {
        super(learningRate, gamma, actionCount, minChange);
    }

    @Override
    void update(final int action, final double reward, final double nextStateMax) {
        super.update(action, reward, nextStateMax);
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

    private void updateQValueMax(final int index, final double maxValue) {
        if (!this.initializedQValues[index]) {
            if (this.qValues[index] == null) {
                this.qValues[index] = maxValue;
            } else {
                this.qValues[index] = Double.min(this.qValues[index], maxValue);
            }
        }
    }

    void updateQValueMin(final int index, final double minValue) {
        if (!this.initializedQValues[index]) {
            if (this.qValues[index] == null) {
                this.qValues[index] = minValue;
            } else {
                this.qValues[index] = Double.max(this.qValues[index], minValue);
            }
        }
    }

}
