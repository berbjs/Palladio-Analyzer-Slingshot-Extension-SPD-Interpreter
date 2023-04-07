package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.analyzer.slingshot.monitor.data.entities.SlingshotMeasuringValue;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcmmeasuringpoint.OperationReference;
import org.palladiosimulator.spd.triggers.SimpleFireOnValue;
import org.palladiosimulator.spd.triggers.expectations.ExpectedTime;
import org.palladiosimulator.spd.triggers.stimuli.OperationResponseTime;

public final class OperationResponseTimeTriggerChecker extends TriggerChecker<OperationResponseTime> {

	public OperationResponseTimeTriggerChecker(final SimpleFireOnValue trigger) {
		super(trigger, OperationResponseTime.class);
	}

	@Override
	public FilterResult doProcess(final Object event) {
		final ExpectedTime expectedTime = this.getExpectedValueAs(ExpectedTime.class);

		if (event instanceof final MeasurementMade measurementMade) {
			final SlingshotMeasuringValue measuringValue = measurementMade.getEntity();

			final MeasuringPoint point = measuringValue.getMeasuringPoint();
			if (point instanceof final OperationReference reference) {
				final OperationSignature referencedSignature = reference.getOperationSignature();
				final OperationSignature thisSignature = getStimulus().getOperationSignature();

				if(thisSignature.getId().equals(referencedSignature.getId())) {
					final Measure<Double,Duration> measure = measuringValue.getMeasureForMetric(MetricDescriptionConstants.RESPONSE_TIME_METRIC);
					final double operationTime = measure.doubleValue(SI.SECOND);

					if (this.compareValues(expectedTime.getValue(), operationTime)) {
						return FilterResult.success(event);
					} else {
						return FilterResult.disregard("");
					}
				} else {
					return FilterResult.disregard("The signatures do not match");
				}
			} else {
				return FilterResult.disregard("Yo wtf: Point is " + point.getClass().getName());
			}
		} else {
			return FilterResult.disregard("Yo wtf: Event is " + event.getClass().getName());
		}
	}
}