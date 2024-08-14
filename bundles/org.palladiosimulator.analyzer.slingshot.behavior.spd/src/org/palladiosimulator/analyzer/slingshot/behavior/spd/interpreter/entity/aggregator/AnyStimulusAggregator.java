package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import java.util.Optional;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.OutputInterpreterWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.targetgroup.TargetGroupChecker;
import org.palladiosimulator.analyzer.slingshot.monitor.data.entities.SlingshotMeasuringValue;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.metricspec.BaseMetricDescription;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcmmeasuringpoint.OperationReference;
import org.palladiosimulator.spd.targets.TargetGroup;
import org.palladiosimulator.spd.triggers.AGGREGATIONMETHOD;
import org.palladiosimulator.spd.triggers.stimuli.AggregatedStimulus;
import org.palladiosimulator.spd.triggers.stimuli.ManagedElementsStateStimulus;
import org.palladiosimulator.spd.triggers.stimuli.NumberOfElements;
import org.palladiosimulator.spd.triggers.stimuli.OperationResponseTime;
import org.palladiosimulator.spd.triggers.stimuli.QueueLength;

/**
 * This class implements the base functionality for aggregating {@link ManagedElementsStateStimulus}
 * elements. Measurements for this elements need to be aggregated first, which is done by
 * {@link #aggregateMeasurement(MeasurementMade)}. Afterwards, {@link #getResult()} checks whether
 * enough measurements were made, and which value is aggregated.
 * 
 * The precondition is that the measuring point, where the measurements are coming from, are inside
 * the target group. This is done in the {@link TargetGroupChecker} filter, that should be placed
 * before this filter.
 * 
 * @author Jens Berberich, based on work by Julijan Katic
 *
 * @param <T>
 *            The concrete element the class is checking for.
 */
public class AnyStimulusAggregator<T extends AggregatedStimulus> extends ModelAggregatorWrapper<T> {
    abstract class StimulusChecker {
        abstract Optional<Measure<Double, ?>> checkStimulus(MeasurementMade measurementMade);
    }

    class OperationResponseTimeStimulusChecker extends StimulusChecker {
        private final OperationSignature operationSignature;

        OperationResponseTimeStimulusChecker(OperationResponseTime operationResponseTime) {
            this.operationSignature = operationResponseTime.getOperationSignature();
        }

        Optional<Measure<Double, ?>> checkStimulus(MeasurementMade measurementMade) {
            final SlingshotMeasuringValue measuringValue = measurementMade.getEntity();

            final MeasuringPoint point = measuringValue.getMeasuringPoint();
            if (point instanceof final OperationReference reference) {
                final OperationSignature referencedSignature = reference.getOperationSignature();
                final OperationSignature thisSignature = this.operationSignature;
                if (thisSignature.getId()
                    .equals(referencedSignature.getId())) {
                    return Optional
                        .of(measuringValue.getMeasureForMetric(MetricDescriptionConstants.RESPONSE_TIME_METRIC));

                }
            }
            return Optional.empty();
        }
    }

    class NumberOfElementsStimulusChecker extends StimulusChecker {

        public NumberOfElementsStimulusChecker(NumberOfElements numberOfElements) {
            LOGGER.debug("Encountered a NumberOfElements Stimulus");
            // TODO Auto-generated constructor stub
        }

        @Override
        Optional<Measure<Double, ?>> checkStimulus(MeasurementMade measurementMade) {
            // TODO Auto-generated method stub
            final SlingshotMeasuringValue measuringValue = measurementMade.getEntity();
            final MeasuringPoint point = measuringValue.getMeasuringPoint();
            // if (point instanceof final ActiveR)
            return Optional.empty();
        }
    }

    protected final WindowAggregation aggregator;
    private AnyStimulusAggregator<T>.StimulusChecker stimulusChecker;
    private static final Logger LOGGER = Logger.getLogger(OutputInterpreterWrapper.class);

    public AnyStimulusAggregator(final T stimulus, final TargetGroup targetGroup,
            final MetricSetDescription metricSetDescription, final BaseMetricDescription baseMetricDescription) {
        super(stimulus, targetGroup, metricSetDescription, baseMetricDescription);
        // TODO IMPORTANT change + enhance aggregation based on newly introduced types
        if (stimulus.getAggregatedStimulus() instanceof AggregatedStimulus
                | stimulus.getAggregatedStimulus() instanceof ManagedElementsStateStimulus) {
            LOGGER.error("AggregatedStimulus needs to contain a stimulus that is not by itself aggregated");
            throw new IllegalArgumentException(
                    "AggregatedStimulus needs to contain a stimulus that is not by itself aggregated");
        }
        // QueueLength, NumberOfElements, OperationResponseTime
        else if (stimulus.getAggregatedStimulus() instanceof OperationResponseTime operationResponseTime) {
            this.stimulusChecker = new OperationResponseTimeStimulusChecker(operationResponseTime);
            this.baseMetricDescription = MetricDescriptionConstants.RESPONSE_TIME_METRIC;
            this.metricSetDescription = MetricDescriptionConstants.RESPONSE_TIME_METRIC_TUPLE;
        } else if (stimulus.getAggregatedStimulus() instanceof NumberOfElements numberOfElements) {
            this.stimulusChecker = new NumberOfElementsStimulusChecker(numberOfElements);
        } else if (stimulus.getAggregatedStimulus() instanceof QueueLength queueLenght) {
            LOGGER.debug("Queue Length stimuli are not yet supported");
            // TODO IMPORTANT implement queue length aggregator
            this.baseMetricDescription = MetricDescriptionConstants.STATE_OF_PASSIVE_RESOURCE_METRIC;
            this.metricSetDescription = MetricDescriptionConstants.STATE_OF_PASSIVE_RESOURCE_METRIC_TUPLE;
        }
        if (stimulus.getAggregationMethod()
            .equals(AGGREGATIONMETHOD.AVERAGE)) {
            this.aggregator = new SlidingTimeWindowAggregationBasedOnEMA(60, 10, 0.2);
        } else {
            this.aggregator = FixedLengthWindowSimpleAggregation
                .getFromAggregationMethod(stimulus.getAggregationMethod());
        }
    }

    /**
     * Helper method to retrieve the filter result. If the aggregated value is in accordance with
     * the specified trigger, success is returned. If not all measurements were made yet, or if the
     * value was not in accordance, then disregard.
     * 
     * @throws Exception
     *             if {@link #aggregator} cannot currently emit a value
     */
    public double getResult() throws Exception {
        if (!this.aggregator.isEmittable()) {
            if (this.stimulusChecker instanceof NumberOfElementsStimulusChecker) {
                return -1; // TODO IMPORTANT return the initial number of elements of the group
                           // here!
            } else {
                throw new Exception("Values for " + this.stimulusChecker + " not emittable.");
            }
        }
        return this.aggregator.getCurrentValue();
    }

    /**
     * Helper method to aggregate the measurement in case if the measurement comes from one of the
     * resource containers in the target group.
     */
    public void aggregateMeasurement(final MeasurementMade measurementMade) {
        if (measurementMade.getEntity()
            .getMetricDesciption()
            .getId()
            .equals(this.metricSetDescription.getId())) {
            Optional<Measure<Double, ?>> measure = this.stimulusChecker.checkStimulus(measurementMade);
            if (measure.isPresent()) {
                aggregator.aggregate(getPointInTime(measurementMade.getEntity()), measure.get()
                    .getValue());
            }
        }
    }

    protected double getPointInTime(final SlingshotMeasuringValue smv) {
        final Measure<Double, Duration> pointInTime = smv
            .getMeasureForMetric(MetricDescriptionConstants.POINT_IN_TIME_METRIC);
        return pointInTime.getValue();
    }

    protected double getValueForAggregation(final SlingshotMeasuringValue smv) {
        final Measure<Double, Dimensionless> measure = smv.getMeasureForMetric(this.baseMetricDescription);

        return measure.getValue();
    }
}
