package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.utils.TargetGroupUtils;
import org.palladiosimulator.analyzer.slingshot.monitor.data.entities.SlingshotMeasuringValue;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcmmeasuringpoint.ActiveResourceMeasuringPoint;
import org.palladiosimulator.spd.targets.TargetGroup;
import org.palladiosimulator.spd.triggers.SimpleFireOnValue;
import org.palladiosimulator.spd.triggers.expectations.ExpectedCount;
import org.palladiosimulator.spd.triggers.stimuli.TaskCount;

public class TaskCountTriggerChecker extends AbstractManagedElementTriggerChecker<TaskCount, ActiveResourceMeasuringPoint> {

	public TaskCountTriggerChecker(SimpleFireOnValue trigger, TaskCount stimulus, TargetGroup targetGroup) {
		super(trigger, 
				stimulus, 
				ActiveResourceMeasuringPoint.class, 
				targetGroup, 
				Set.of(ExpectedCount.class), 
				MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE,
				MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC);
	}

	@Override
	protected boolean isMeasuringPointInTargetGroup(ActiveResourceMeasuringPoint activeResourceMP) {
		final ProcessingResourceSpecification spec = activeResourceMP.getActiveResource();
		return TargetGroupUtils.isContainerInTargetGroup(spec.getResourceContainer_ProcessingResourceSpecification(), targetGroup);
	}
	
	@Override
	protected double getValueForAggregation(final SlingshotMeasuringValue smv) {
		final Measure<Long, Dimensionless> measure = smv.getMeasureForMetric(this.baseMetricDescription);
		final long value = measure.getValue();
		return value;
	}
}
