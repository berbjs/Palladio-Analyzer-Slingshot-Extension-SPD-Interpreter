package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.Arrays;

class ReducedActionSpaceCalculator {

    protected class QValue {
        private Double value;
        private boolean isInitialized;
        private final int action;

        QValue(final int action) {
            this(null, false, action);
        }

        QValue(final Double value, final boolean isInitialized, final int action) {
            this.value = value;
            this.isInitialized = isInitialized;
            this.action = action;
        }

        Double getValue() {
            if (this.value == null) {
                return 0.0;
            }
            return this.value;
        }

        void setValue(final Double newValue) {
            this.value = (1 - ReducedActionSpaceCalculator.this.alpha) * this.getValue()
                    + ReducedActionSpaceCalculator.this.alpha * newValue;
        }

        void updateValue(final double alpha, final Double newValue) {
            this.isInitialized = true;
            this.value = (1 - alpha) * this.getValue() + alpha * newValue;
        }

        void setInitialized() {
            this.isInitialized = true;
        }

        @Override
        public String toString() {
            if (this.isInitialized) {
                return this.action + ":" + this.value;
            } else {
                return this.action + ":" + "~" + this.value;
            }
        }

        public boolean isInitialized() {
            return this.isInitialized;
        }

        private void updateMax(final double maxValue) {
            if (!this.isInitialized) {
                if (this.value == null) {
                    this.value = maxValue;
                } else {
                    this.value = Double.min(this.value, maxValue);
                }
            }
        }

        void updateMin(final double minValue) {
            if (!this.isInitialized) {
                if (this.value == null) {
                    this.value = minValue;
                } else {
                    this.value = Double.max(this.value, minValue);
                }
            }
        }
    }

    protected QValue[] qValues;
    private final double alpha;
    private final double gamma;
    private final boolean doExtrapolation;

    ReducedActionSpaceCalculator(final double learningRate, final double discountFactor, final int actionCount,
            final boolean performApproximation) {
        this(learningRate, discountFactor, actionCount, -(actionCount - 1) / 2, performApproximation);
    }

    ReducedActionSpaceCalculator(final double learningRate, final double gamma, final int actionCount,
            final int minChange, final boolean performApproximation) {
        super();
        this.qValues = new QValue[actionCount];
        for (int i = 0; i < actionCount; i++) {
            this.qValues[i] = new QValue(minChange + i);
        }
        this.gamma = gamma;
        assert (0 <= learningRate && learningRate <= 1);
        this.alpha = learningRate;
        this.doExtrapolation = performApproximation;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("minChange=" + this.getMinChange() + ", Q-Values=[");
        for (final QValue value : this.qValues) {
            sb.append(value)
                .append(", ");
        }
        sb.delete(sb.length() - 2, sb.length())
            .append("]");
        return sb.toString();
    }

    int getMinChange() {
        return this.qValues[0].action;
    }

    private int getMinChange(final QValue[] qValues) {
        return qValues[0].action;
    }

    int getOptimalAction() {
        return this.getOptimalAction(this.qValues);
    }

    private int getOptimalAction(final QValue[] qValues) {
        Double maxQValue = qValues[(qValues.length - 1) / 2].getValue();
        int optimalAction = (qValues.length - 1) / 2;
        for (int i = 0; i < qValues.length; i += 1) {
            if (qValues[i].value != null && qValues[i].getValue() > maxQValue) {
                optimalAction = i;
                maxQValue = qValues[optimalAction].getValue();
            }
        }
        return optimalAction + this.getMinChange(qValues);
    }

    double getMaxValue() {
        return Arrays.stream(this.qValues)
            .filter(Double.class::isInstance)
            .map(QValue::getValue)
            .max(Double::compare)
            .orElse(0.0);
    }

    void update(final int action, final double reward, final double nextStateMax) {
        this.qValues = this.performUpdate(action, reward, nextStateMax);
    }

    /**
     * Performs an update to the Q-Value corresponding to the given action, taking into account the
     * reward and the future rewards (provided as nextStateMax). Doesn't change any object variables
     * and instead returns the updated Q-Value-Table
     *
     * @param action
     * @param reward
     * @param nextStateMax
     * @return
     */
    private QValue[] performUpdate(final int action, final double reward, final double nextStateMax) {
        final QValue[] newQValues = new QValue[this.qValues.length];
        for (int i = 0; i < this.qValues.length; i++) {
            final QValue oldQValue = this.qValues[i];
            if (oldQValue.value == null) {
                newQValues[i] = new QValue(oldQValue.action);
            } else {
                newQValues[i] = new QValue(oldQValue.getValue()
                    .doubleValue(), oldQValue.isInitialized(), oldQValue.action);
            }
        }
        newQValues[action - this.getMinChange()].updateValue(this.alpha, reward + this.gamma * nextStateMax);
        this.performExtrapolation(action, reward, newQValues);
        if (this.getOptimalAction(newQValues) != this.getMinChange() + (newQValues.length - 1) / 2) {
            final int actionCount = newQValues.length;
            final QValue[] shiftedQValues = new QValue[actionCount];
            if (this.getOptimalAction(newQValues) > this.getMinChange() + (newQValues.length - 1) / 2) {
                // Shift to the right
                for (int i = 1; i < actionCount; i++) {
                    shiftedQValues[i - 1] = newQValues[i];
                }
                shiftedQValues[actionCount - 1] = new QValue(this.getMinChange() + actionCount);
            } else {
                // Shift to the left
                for (int i = 0; i < actionCount - 1; i++) {
                    shiftedQValues[i + 1] = newQValues[i];
                }
                shiftedQValues[0] = new QValue(this.getMinChange() - 1);
            }
            this.performExtrapolation(action, reward, shiftedQValues);
            return shiftedQValues;
        }
        return newQValues;
    }

    /**
     * If {@link ReducedActionSpaceCalculator#doExtrapolation} is true: The given {@link qValues}
     * are extrapolated using the four rules defined in the Improved-Q-Learning paper (for the given
     * action and reward)
     *
     * @param qValues
     *            A list of qValues for which the extrapolation will be performed. This list will be
     *            modified!
     * @param action
     *            The previous action
     * @param reward
     *            The observed reward for the previous action
     */
    private QValue[] performExtrapolation(final int action, final double reward, final QValue[] qValues) {
        if (!this.doExtrapolation) {
            return qValues;
        }
        final Double minApproximatedValue = Math.nextAfter(qValues[action - this.getMinChange(qValues)].getValue(),
                Double.POSITIVE_INFINITY);
        final Double maxApproximatedValue = Math.nextAfter(qValues[action - this.getMinChange(qValues)].getValue(),
                Double.NEGATIVE_INFINITY);
        if (reward < 0 && action > 0) {
            // Extrapolation Rule 1
            for (int i = action - qValues[0].action + 1; i < qValues.length; i += 1) {
                qValues[i].updateMin(minApproximatedValue);
            }
        } else if (reward < 0) {
            // Extrapolation Rule 2
            // TODO should only be performed for "higher utilization and higher rates"
            for (int i = 0; i < qValues.length; i += 1) {
                if (i + qValues[0].action < action) {
                    qValues[i].updateMax(maxApproximatedValue);
                } else if (i + qValues[0].action > action) {
                    qValues[i].updateMin(minApproximatedValue);
                }
            }
        } else if (reward > 0) {
            // Extrapolation Rule 3
            // TODO should only be performed for "lower utilization and lower rates"
            for (int i = action - qValues[0].action + 1; i < qValues.length; i += 1) {
                qValues[i].updateMax(maxApproximatedValue);
            }
        } else if (action == this.getOptimalAction()) {
            // Extrapolation Rule 4
            for (int i = 0; i < qValues.length; i += 1) {
                if (i != action - qValues[0].action) {
                    qValues[i].updateMax(maxApproximatedValue);
                }
            }
        }
        return qValues;
    }

    public int getUpdatedOptimalAction(final int previousAction, final double reward, final double nextStateMax) {
        final QValue[] updatedQValues = this.performUpdate(previousAction, reward, nextStateMax);
        return this.getOptimalAction(updatedQValues);
    }
}