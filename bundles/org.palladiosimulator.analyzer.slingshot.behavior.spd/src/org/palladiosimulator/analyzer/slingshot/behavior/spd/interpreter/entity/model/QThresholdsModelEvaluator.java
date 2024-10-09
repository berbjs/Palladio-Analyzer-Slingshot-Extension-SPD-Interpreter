package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.NotEmittableException;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.spd.models.QThresholdsModel;

/**
 * This evaluator implements the evaluation steps needed for Q-Threshold-Models. It is based on the
 * work „Efficient Autoscaling in the Cloud Using Predictive Models for Workload Forecasting“ by N.
 * Roy, A. Dubey, and A. Gokhale (see: https://doi.org/10.1109/CLOUD.2011.42)
 * 
 * @author Jens Berberich
 *
 */
public class QThresholdsModelEvaluator extends LearningBasedModelEvaluator {

    private final Map<Long, ReducedActionSpaceCalculator> qValuesLowerThreshold = new HashMap<>();
    private final Map<Long, ReducedActionSpaceCalculator> qValuesUpperThreshold = new HashMap<>();
    private final ModelAggregatorWrapper<?> stimulusListener;
    private final ModelAggregatorWrapper<?> responseTimeAggregator;
    private final ModelAggregatorWrapper<?> utilizationAggregator;
    private final double exponentialSteepness;
    private final double targetResponseTime;
    private int lastScalingAction;
    private long previousState = 1;
    private double learningRate;
    private double discountFactor;
    private long state;
    private static final Logger LOGGER = Logger.getLogger(QThresholdsModelEvaluator.class);

    public QThresholdsModelEvaluator(final QThresholdsModel model, final ModelAggregatorWrapper<?> stimulusListener) {
        super(Collections.singletonList(stimulusListener), true, false);
        this.stimulusListener = stimulusListener;
        final ModelInterpreter modelInterpreter = new ModelInterpreter();
        this.exponentialSteepness = model.getExponentialSteepness();
        this.targetResponseTime = model.getTargetResponseTime();
        this.learningRate = model.getLearningRate();
        this.discountFactor = model.getDiscountFactor();
        this.responseTimeAggregator = modelInterpreter.getAggregatorForStimulus(model.getResponseTimeStimulus(), model);
        this.utilizationAggregator = modelInterpreter.getAggregatorForStimulus(model.getUtilizationStimulus(), model);
    }

    @Override
    public int getDecision() throws NotEmittableException {
        // TODO make initial scaling policy (here: 40,80) configurable
        final int upperThreshold = this.qValuesUpperThreshold
            .getOrDefault(this.previousState,
                    new ReducedActionSpaceCalculator(this.learningRate, this.discountFactor, 5, 78, false))
            .getOptimalAction();
        final int lowerThreshold = this.qValuesLowerThreshold
            .getOrDefault(this.previousState,
                    new ReducedActionSpaceCalculator(this.learningRate, this.discountFactor, 5, 38, false))
            .getOptimalAction();
        final double currentUtilization = this.utilizationAggregator.getResult();
        this.lastScalingAction = 0;
        if (currentUtilization > upperThreshold) {
            this.lastScalingAction = 1;
        } else if (currentUtilization < lowerThreshold) {
            this.lastScalingAction = -1;
        }
        this.previousState = this.state;
        return this.lastScalingAction;
    }

    @Override
    public void recordUsage(MeasurementMade measurement) {
        super.recordUsage(measurement);
        // TODO use the measurement specified in model.getInput()
        if (measurement.getEntity()
            .getMetricDesciption()
            .getId()
            .equals(MetricDescriptionConstants.NUMBER_OF_RESOURCE_CONTAINERS_OVER_TIME.getId())) {
            this.state = (long) measurement.getEntity()
                .getMeasureForMetric(MetricDescriptionConstants.NUMBER_OF_RESOURCE_CONTAINERS)
                .getValue();
        }
    }

    @Override
    void recordRewardMeasurement(final MeasurementMade measurement) {
        this.responseTimeAggregator.aggregateMeasurement(measurement);
        this.utilizationAggregator.aggregateMeasurement(measurement);
    }

    private double getReward(final double currentResponseTime) throws NotEmittableException {
        double exponentialFactor = 1;
        if (currentResponseTime > this.targetResponseTime) {
            // Case respTime > SLA
            exponentialFactor -= currentResponseTime / this.targetResponseTime;
        }
        return (1 - Math.exp(-this.exponentialSteepness * exponentialFactor))
                / (1 - this.utilizationAggregator.getResult());
    }

    @Override
    public void update() throws NotEmittableException {
        final double currentResponseTime = this.responseTimeAggregator.getResult();
        final double reward = this.getReward(currentResponseTime);
        LOGGER.debug("Reward " + reward + "for state " + this.previousState);
        // TODO This should be an int actually
        final double nextStateMax = Math.max(this.qValuesLowerThreshold.get(state)
            .getMaxValue(),
                this.qValuesUpperThreshold.get(state)
                    .getMaxValue());
        // TODO Is this the "correct" approach for getting the maximum Q-Value for the next state?
        if (this.lastScalingAction < 0 && currentResponseTime > this.targetResponseTime) {
            this.qValuesLowerThreshold.get(this.previousState)
                .update(this.lastScalingAction, reward, nextStateMax);
            LOGGER.debug("Potentially new lower threshold for state " + this.previousState + ": "
                    + this.qValuesLowerThreshold.get(this.previousState)
                        .getOptimalAction());
        } else if (currentResponseTime > this.targetResponseTime || this.lastScalingAction > 0) {
            this.qValuesUpperThreshold.get(this.previousState)
                .update(this.lastScalingAction, reward, nextStateMax);
            LOGGER.debug("Potentially new upper threshold for state " + this.previousState + ": "
                    + this.qValuesUpperThreshold.get(this.previousState)
                        .getOptimalAction());
        } else if (this.lastScalingAction < 0) {
            this.qValuesLowerThreshold.get(this.previousState)
                .update(this.lastScalingAction, reward, nextStateMax);
            LOGGER.debug("Potentially new lower threshold for state " + this.previousState + ": "
                    + this.qValuesLowerThreshold.get(this.previousState)
                        .getOptimalAction());
        }
    }
}
