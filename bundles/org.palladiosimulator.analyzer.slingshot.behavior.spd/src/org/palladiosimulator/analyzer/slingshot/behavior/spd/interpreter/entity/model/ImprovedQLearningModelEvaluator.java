package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.NotEmittableException;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.spd.models.ImprovedQLearningModel;

public class ImprovedQLearningModelEvaluator extends LearningBasedModelEvaluator {

    private Double previousState;
    private int previousAction;
    private final double epsilon;
    private int actionCount;
    private Map<Long, IntervalMapping> intervalMappings;
    private final Random random;
    private final ModelAggregatorWrapper<?> responseTimeAggregator;
    private final ModelAggregatorWrapper<?> utilizationAggregator;
    private final double exponentialSteepness;
    private final double targetResponseTime;
    private long resourceCount;
    private double discountFactor;
    private double learningRate;
    private static final Logger LOGGER = Logger.getLogger(ImprovedQLearningModelEvaluator.class);

    public ImprovedQLearningModelEvaluator(final ImprovedQLearningModel model,
            final ModelAggregatorWrapper<?> modelAggregatorWrapper) {
        super(Collections.singletonList(modelAggregatorWrapper), false, true);
        if (model.getTargetResponseTime() <= 0) {
            throw new IllegalArgumentException("The target response time must be greater than zero");
        }
        final ModelInterpreter modelInterpreter = new ModelInterpreter();
        this.exponentialSteepness = model.getExponentialSteepness();
        this.targetResponseTime = model.getTargetResponseTime();
        this.actionCount = model.getActionCount();
        this.responseTimeAggregator = modelInterpreter.getAggregatorForStimulus(model.getResponseTimeStimulus(), model,
                model.getResponseTimeAggregationMethod());
        this.utilizationAggregator = modelInterpreter.getAggregatorForStimulus(model.getUtilizationStimulus(), model);
        this.epsilon = model.getEpsilon();
        this.actionCount = model.getActionCount();
        this.learningRate = model.getLearningRate();
        this.discountFactor = model.getDiscountFactor();
        this.intervalMappings = new HashMap();
        if (model.getActionCount() % 2 == 0) {
            throw new IllegalArgumentException("The action count must be odd");
        }
        // this.intervalMapping = new IntervalMapping(model.getLearningRate(),
        // model.getActionCount(),
        // model.getDiscountFactor());
        this.random = new Random();
    }

    @Override
    public void recordUsage(final MeasurementMade measurement) {
        super.recordUsage(measurement);
        if (measurement.getEntity()
            .getMetricDesciption()
            .getId()
            .equals(MetricDescriptionConstants.NUMBER_OF_RESOURCE_CONTAINERS_OVER_TIME.getId())) {
            this.resourceCount = (long) measurement.getEntity()
                .getMeasureForMetric(MetricDescriptionConstants.NUMBER_OF_RESOURCE_CONTAINERS)
                .getValue();
        }
    }

    @Override
    public int getDecision() throws NotEmittableException {
        final Double input = this.aggregatorList.get(0)
            .getResult();
        final double actualResponseTime = this.responseTimeAggregator.getResult();
        final double utilization = this.utilizationAggregator.getResult();
        final double reward = (1
                - Math.exp(-this.exponentialSteepness * (1 - (actualResponseTime / this.targetResponseTime))))
                / (1 - utilization);
        LOGGER.debug("Reward (for the last period): " + reward);
        if (this.previousState != null) {
            if (!this.intervalMappings.containsKey(this.resourceCount)) {
                this.intervalMappings.put(this.resourceCount,
                        new IntervalMapping(this.learningRate, this.actionCount, this.discountFactor));
            }
            this.update(reward,
                    this.intervalMappings
                        .getOrDefault(this.resourceCount + this.evaluateState(input),
                                new IntervalMapping(this.learningRate, this.actionCount, this.discountFactor))
                        .getQValues(input)
                        .getMaxValue());
        }
        this.previousState = input;
        this.previousAction = this.evaluateState(input);
        return this.previousAction;
    }

    private int evaluateState(final Double state) {
        // Epsilon-Greedy exploratory action
        LOGGER.debug("Current state: " + state);
        LOGGER.debug("Current interval mapping: " + this.intervalMappings.get(this.resourceCount));
        if (Math.random() < this.epsilon) {
            // TODO Should ideally be performed "only around the boundary between adjacent
            // states and only using the adjacent actions"
            LOGGER.debug("Performed Epsilon-Action!");
            return this.random.nextInt(this.intervalMappings.get(this.resourceCount)
                .getMapping(state) - (this.actionCount - 1) / 2,
                    this.intervalMappings.get(this.resourceCount)
                        .getMapping(state) + (this.actionCount - 1) / 2 + 1);
        } else {
            return this.intervalMappings.get(this.resourceCount)
                .getMapping(state);
        }
    }

    @Override
    void recordRewardMeasurement(final MeasurementMade measurement) {
        this.utilizationAggregator.aggregateMeasurement(measurement);
        this.responseTimeAggregator.aggregateMeasurement(measurement);
    }

    @Override
    public void update() {
        // The update is performed inside getDecision(), so this is only a stub.
    }

    private void update(final double reward, final double nextStateMax) {
        final ReducedActionSpaceCalculator entry = this.intervalMappings.get(this.resourceCount)
            .getQValues(this.previousState);
        entry.update(this.previousAction, reward, nextStateMax);
        final int optimalAction = entry.getOptimalAction();
        if (optimalAction != this.previousAction) {
            this.intervalMappings.get(this.resourceCount)
                .adjustMapping(this.previousState, optimalAction);
        }
    }
}
