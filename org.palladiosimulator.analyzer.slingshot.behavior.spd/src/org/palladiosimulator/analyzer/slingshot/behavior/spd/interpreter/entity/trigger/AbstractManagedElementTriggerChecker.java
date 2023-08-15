package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.FixedLengthWindowAggregation;
import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;
import org.palladiosimulator.analyzer.slingshot.monitor.data.entities.SlingshotMeasuringValue;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.metricspec.BaseMetricDescription;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.spd.targets.TargetGroup;
import org.palladiosimulator.spd.triggers.BaseTrigger;
import org.palladiosimulator.spd.triggers.expectations.ExpectedPrimitive;
import org.palladiosimulator.spd.triggers.stimuli.ManagedElementsStateStimulus;

/**
 * This abstract class implements the base functionality for {@link ManagedElementsStateStimulus} elements. Measurements
 * for this elements need to be aggregated first, which is done by {@link #aggregateMeasurement(MeasurementMade, MeasuringPoint)}.
 * Afterwards, {@link #getResult(DESEvent)} checks whether enough measurements were made, and whether the calculated value
 * triggers the policy.
 * 
 * Classes that extend this checker need to implement {@link #isMeasuringPointInTargetGroup(MeasuringPoint)}, since each
 * measuring point has a different way of retrieving the right Resource container.
 * 
 * @author Julijan Katic
 *
 * @param <T> The concrete element the class is checking for.
 * @param <MP> The concrete measuring point the checker needs to listen to.
 */
public abstract class AbstractManagedElementTriggerChecker<T extends ManagedElementsStateStimulus, MP extends MeasuringPoint> extends TriggerChecker<T> {
	
	protected final TargetGroup targetGroup;
	protected final T managedElementsStateStimulus;
	protected final MetricSetDescription metricSetDescription;
	protected final BaseMetricDescription baseMetricDescription;
	protected final FixedLengthWindowAggregation aggregator;
	protected final Class<MP> measuringPointType;
	
	@SuppressWarnings("unchecked")
	public AbstractManagedElementTriggerChecker(final BaseTrigger trigger, 
												final T stimulus,
												final Class<MP> measuringPointType,
												final TargetGroup targetGroup,
												final Set<Class<? extends ExpectedPrimitive>> allowedExpectedPrimitives,
												final MetricSetDescription metricSetDescription,
												final BaseMetricDescription baseMetricDescription) {
		super(trigger, (Class<T>) stimulus.getClass(), allowedExpectedPrimitives);
		
		this.targetGroup = targetGroup;
		this.managedElementsStateStimulus = stimulus;
		this.metricSetDescription = metricSetDescription;
		this.baseMetricDescription = baseMetricDescription;
		this.aggregator = FixedLengthWindowAggregation.getFromAggregationMethod(stimulus.getAggregationOverElements());
		this.measuringPointType = measuringPointType;
	}

	@Override
	public FilterResult doProcess(FilterObjectWrapper event) {
		if (event.getEventToFilter() instanceof final MeasurementMade measurementMade) {
			final MeasuringPoint measuringPoint = measurementMade.getEntity().getMeasuringPoint();
			if (measuringPointType.isAssignableFrom(measuringPoint.getClass())) {
				/*
				 * Check that the measuring point points to one of the resource container's
				 * specifications, and count them only once!
				 */
				aggregateMeasurement(measurementMade, measuringPointType.cast(measuringPoint));


				/*
				 * If all the values are collected, then check whether the expected percentage
				 * matches, otherwise disregard.
				 */
				return getResult(measurementMade);
			}
		}


		return FilterResult.disregard("");
	}

	/**
	 * Helper method to retrieve the filter result. If the aggregated value is in
	 * accordance with the specified trigger, success is returned. If not all
	 * measurements were made yet, or if the value was not in accordance, then
	 * disregard.
	 */
	protected FilterResult getResult(final DESEvent event) {
		if (!this.aggregator.isWindowFull()) {
			return FilterResult.disregard("There are not enough values yet to aggregate.");
		}
		
		final double aggregatedValue = this.aggregator.getCurrentValue();
		if (this.compareToTrigger(aggregatedValue) == ComparatorResult.IN_ACCORDANCE) {
			return FilterResult.success(event);
		}
		return FilterResult.disregard();
	}

	/**
	 * Helper method to aggregate the measurement in case if the measurement comes 
	 * from one of the resource containers in the target group.
	 */
	protected void aggregateMeasurement(final MeasurementMade measurementMade, final MP measuringPoint) {
		if (this.isMeasuringPointInTargetGroup(measuringPoint)
				&& measurementMade.getEntity().getMetricDesciption().getId().equals(this.metricSetDescription.getId())) {
			aggregator.aggregate(this.getValueForAggregation(measurementMade.getEntity()));
		}
	}
	
	protected double getValueForAggregation(final SlingshotMeasuringValue smv) {
		final Measure<Double, Dimensionless> measure = smv.getMeasureForMetric(this.baseMetricDescription);
		final double value = measure.getValue();
		return value;
	}
	
	/**
	 * Checks whether the measuring point is somewhere in the target group and thus, whether
	 * its measurements are relevant for this trigger. Since each measuring point has a
	 * different way of retrieving such information, it has to be implemented by the concrete
	 * trigger.
	 * 
	 * @param measuringPoint The measuring point to check.
	 * @return true if the measuring point lies somewhere in the target group, i.e.
	 * 		   is relevant for this trigger.
	 */
	protected abstract boolean isMeasuringPointInTargetGroup(final MP measuringPoint);
}
