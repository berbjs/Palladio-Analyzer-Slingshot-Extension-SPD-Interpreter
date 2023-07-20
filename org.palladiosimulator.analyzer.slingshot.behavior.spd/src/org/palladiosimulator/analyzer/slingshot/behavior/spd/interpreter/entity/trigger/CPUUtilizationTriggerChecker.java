package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.AbstractWindowAggregation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.MaxWindowAggregation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.MeanWindowAggregation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.MedianWindowAggregation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.MinWindowAggregation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.SumWindowAggregation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.utils.TargetGroupUtils;
import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcmmeasuringpoint.ActiveResourceMeasuringPoint;
import org.palladiosimulator.spd.targets.TargetGroup;
import org.palladiosimulator.spd.triggers.AGGREGATIONMETHOD;
import org.palladiosimulator.spd.triggers.SimpleFireOnValue;
import org.palladiosimulator.spd.triggers.expectations.ExpectedPercentage;
import org.palladiosimulator.spd.triggers.stimuli.CPUUtilization;

public class CPUUtilizationTriggerChecker extends TriggerChecker<CPUUtilization> {

	/** The number of measurements to consider at most. */
	private static final int THRESHOLD = 10;

	private final AbstractWindowAggregation aggregator;
	private final TargetGroup targetGroup;

	public CPUUtilizationTriggerChecker(final SimpleFireOnValue trigger,
										final AGGREGATIONMETHOD aggregationMethod,
								 		final TargetGroup targetGroup) {
		super(trigger, CPUUtilization.class, Set.of(ExpectedPercentage.class));

		this.targetGroup = targetGroup;
		this.aggregator = switch (aggregationMethod) {
			case MIN -> new MinWindowAggregation(THRESHOLD);
			case AVERAGE -> new MeanWindowAggregation(THRESHOLD);
			case MAX -> new MaxWindowAggregation(THRESHOLD);
			case MEDIAN -> new MedianWindowAggregation(THRESHOLD);
			case SUM -> new SumWindowAggregation(THRESHOLD);
			default -> throw new IllegalArgumentException("Unexpected value: " + aggregationMethod);
		};
	}

	@Override
	public FilterResult doProcess(final FilterObjectWrapper object) {
		final DESEvent event = object.getEventToFilter();
		if (event instanceof final MeasurementMade measurementMade) {
			final MeasuringPoint measuringPoint = measurementMade.getEntity().getMeasuringPoint();
			if (measuringPoint instanceof final ActiveResourceMeasuringPoint activeResourceMP) {
				/*
				 * Check that the measuring point points to one of the resource container's
				 * specifications, and count them only once!
				 */
				aggregateMeasurement(measurementMade, activeResourceMP);


				/*
				 * If all the values are collected, then check whether the expected percentage
				 * matches, otherwise disregard.
				 */
				return getResult(event);
			}
		}


		return FilterResult.disregard("");
	}

	/**
	 * Helper method to retrieve the filter result. If the aggregated value is in accordance with the specified trigger,
	 * success is returned. If not all measurements were made yet, or if the value was not in accordance, then disregard.
	 */
	private FilterResult getResult(final DESEvent event) {
		final double aggregatedValue = this.aggregator.getCurrentValue();
		if (this.compareToTrigger(aggregatedValue) == ComparatorResult.IN_ACCORDANCE) {
			return FilterResult.success(event);
		}
		return FilterResult.disregard();
	}

	/*
	 * Keep history of last n measurements.
	 */
	/**
	 * Helper method to aggregate the measurement in case if the measurement comes from one of the resource containers in the target group.
	 */
	private void aggregateMeasurement(final MeasurementMade measurementMade, final ActiveResourceMeasuringPoint activeResourceMP) {
		final ProcessingResourceSpecification spec = activeResourceMP.getActiveResource();
		if (TargetGroupUtils.isContainerInTargetGroup(spec.getResourceContainer_ProcessingResourceSpecification(),
				this.targetGroup)
				&& measurementMade.getEntity().getMetricDesciption().getId()
						.equals(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE.getId())) {
			final Measure<Double, Dimensionless> measure // TODO: Find the right metrics
					= measurementMade.getEntity()
							.getMeasureForMetric(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
			final double value = 0;//measure.getValue();
			aggregator.aggregate(value);
		}
	}

}
