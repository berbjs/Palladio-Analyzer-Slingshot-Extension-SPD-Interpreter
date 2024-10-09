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
    private final Map<Long, IntervalMapping> intervalMappings;
    private final Random random;
    private final ModelAggregatorWrapper<?> responseTimeAggregator;
    private final ModelAggregatorWrapper<?> utilizationAggregator;
    private final double exponentialSteepness;
    private final double targetResponseTime;
    private long resourceCount;
    private long previousResourceCount;
    private final double discountFactor;
    private final double learningRate;
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
        this.intervalMappings = new HashMap<>();
        if (model.getActionCount() % 2 == 0) {
            throw new IllegalArgumentException("The action count must be odd");
        }
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
        LOGGER.info("Reward (for the last period): " + reward);
        if (this.previousState != null) {
            this.update(reward, this.intervalMappings
                .getOrDefault(this.previousResourceCount + this.evaluateState(input, this.previousResourceCount),
                        new IntervalMapping(this.learningRate, this.actionCount, this.discountFactor))
                .getQValues(input)
                .getMaxValue());
        }
        LOGGER.info("Current state: " + this.resourceCount + " resources; " + input);
        this.intervalMappings.computeIfAbsent(this.resourceCount,
                key -> new IntervalMapping(this.learningRate, this.actionCount, this.discountFactor));
        LOGGER.info("Current interval mapping: " + this.intervalMappings.get(this.resourceCount));
        this.previousAction = this.evaluateState(input, this.resourceCount);
        this.previousState = input;
        this.previousResourceCount = this.resourceCount;
        return this.previousAction;
    }

    private int evaluateState(final Double state, final long resourceCount) {
        // Epsilon-Greedy exploratory action
        if (Math.random() < this.epsilon) {
            // TODO Should ideally be performed "only around the boundary between adjacent
            // states and only using the adjacent actions"
            LOGGER.debug("Performed Epsilon-Action!");
            return this.random.nextInt(this.intervalMappings.get(resourceCount)
                .getMapping(state) - 1,
                    this.intervalMappings.get(resourceCount)
                        .getMapping(state) + 2);
        } else {
            return this.intervalMappings.get(resourceCount)
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
        final ReducedActionSpaceCalculator entry = this.intervalMappings.get(this.previousResourceCount)
            .getQValues(this.previousState);
        final int updatedOptimalAction = entry.getUpdatedOptimalAction(this.previousAction, reward, nextStateMax);
        if (entry.getOptimalAction() != updatedOptimalAction) {
            this.intervalMappings.get(this.previousResourceCount)
                .adjustMapping(this.previousState, updatedOptimalAction);
        } else {
            entry.update(this.previousAction, reward, nextStateMax);
        }
    }
}
