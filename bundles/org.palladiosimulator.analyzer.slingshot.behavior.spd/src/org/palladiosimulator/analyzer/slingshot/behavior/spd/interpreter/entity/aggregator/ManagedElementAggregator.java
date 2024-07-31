package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.targetgroup.TargetGroupChecker;
import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;
import org.palladiosimulator.analyzer.slingshot.monitor.data.entities.SlingshotMeasuringValue;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.metricspec.BaseMetricDescription;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.spd.targets.TargetGroup;
import org.palladiosimulator.spd.triggers.AGGREGATIONMETHOD;
import org.palladiosimulator.spd.triggers.stimuli.AggregatedStimulus;
import org.palladiosimulator.spd.triggers.stimuli.ManagedElementsStateStimulus;

/**
 * This abstract class implements the base functionality for {@link ManagedElementsStateStimulus}
 * elements. Measurements for this elements need to be aggregated first, which is done by
 * {@link #aggregateMeasurement(MeasurementMade, MeasuringPoint)}. Afterwards,
 * {@link #getResult(DESEvent)} checks whether enough measurements were made, and whether the
 * calculated value triggers the policy.
 * 
 * The precondition is that the measuring point, where the measurements are coming from, are inside
 * the target group. This is done in the {@link TargetGroupChecker} filter, that should be placed
 * before this filter.
 * 
 * @author Julijan Katic
 *
 * @param <T>
 *            The concrete element the class is checking for.
 */
public class ManagedElementAggregator<T extends AggregatedStimulus> extends ModelAggregatorWrapper {
    protected final WindowAggregation aggregator;

    @SuppressWarnings("unchecked")
    public ManagedElementAggregator(final T stimulus, final TargetGroup targetGroup,
            final MetricSetDescription metricSetDescription, final BaseMetricDescription baseMetricDescription) {
        super(stimulus, targetGroup, metricSetDescription, baseMetricDescription);
        // TODO IMPORTANT change + enhance aggregation based on newly introduced types
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
            throw new Exception("Values not emittable.");
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
            aggregator.aggregate(getPointInTime(measurementMade.getEntity()),
                    getValueForAggregation(measurementMade.getEntity()));
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
