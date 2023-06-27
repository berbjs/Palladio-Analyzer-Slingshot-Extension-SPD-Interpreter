package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.targetgroup;

import java.util.Objects;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterChain;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.targets.TargetGroup;

public class TargetGroupChecker implements Filter {

	private final TargetGroup targetGroup;
	
	public TargetGroupChecker(final TargetGroup targetGroup) {
		this.targetGroup = Objects.requireNonNull(targetGroup);
	}
	
	@Override
	public FilterResult doProcess(FilterObjectWrapper objectWrapper) {
		final DESEvent event = objectWrapper.getEventToFilter();
		if (event instanceof MeasurementMade) {
			return FilterResult.success(event);
			// TODO: How to check that MeasuringPoint (which only is ResourceURI) is INSIDE of the target group?
		} else if (event instanceof SimulationTimeReached) {
			final SimulationTimeReached simulationTimeReached = (SimulationTimeReached) event;
			if (simulationTimeReached.getTargetGroupId().equals(targetGroup.getId())) {
				return FilterResult.success(event);
			} else {
				return FilterResult.disregard("The target group does not match the event.");
			}
		} else {
			return FilterResult.disregard("The event can only be checked if it is a MeasurementMade OR SimulationTimeReached at the moment.");
		}
	}

}
