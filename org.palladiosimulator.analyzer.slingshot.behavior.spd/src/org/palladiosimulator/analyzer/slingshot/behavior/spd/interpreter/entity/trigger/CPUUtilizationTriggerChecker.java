package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import java.util.Set;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.utils.TargetGroupUtils;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcmmeasuringpoint.ActiveResourceMeasuringPoint;
import org.palladiosimulator.spd.targets.TargetGroup;
import org.palladiosimulator.spd.triggers.SimpleFireOnValue;
import org.palladiosimulator.spd.triggers.expectations.ExpectedPercentage;
import org.palladiosimulator.spd.triggers.stimuli.CPUUtilization;

public class CPUUtilizationTriggerChecker extends AbstractManagedElementTriggerChecker<CPUUtilization, ActiveResourceMeasuringPoint> {

	public CPUUtilizationTriggerChecker(final SimpleFireOnValue trigger,
										final CPUUtilization stimulus,
								 		final TargetGroup targetGroup) {
		super(trigger, 
				stimulus, 
				ActiveResourceMeasuringPoint.class, 
				targetGroup,
				Set.of(ExpectedPercentage.class),
				MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
				MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
	}
	
	@Override
	protected boolean isMeasuringPointInTargetGroup(final ActiveResourceMeasuringPoint activeResourceMP) {
		final ProcessingResourceSpecification spec = activeResourceMP.getActiveResource();
		return TargetGroupUtils.isContainerInTargetGroup(spec.getResourceContainer_ProcessingResourceSpecification(), targetGroup);
	}
}
