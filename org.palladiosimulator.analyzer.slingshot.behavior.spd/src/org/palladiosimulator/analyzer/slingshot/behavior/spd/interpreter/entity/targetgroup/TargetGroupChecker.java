package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.targetgroup;

import java.util.Objects;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.pcmmeasuringpoint.ActiveResourceMeasuringPoint;
import org.palladiosimulator.pcmmeasuringpoint.AssemblyReference;
import org.palladiosimulator.pcmmeasuringpoint.ResourceContainerMeasuringPoint;
import org.palladiosimulator.pcmmeasuringpoint.ResourceEnvironmentMeasuringPoint;
import org.palladiosimulator.spd.targets.ElasticInfrastructure;
import org.palladiosimulator.spd.targets.ServiceGroup;
import org.palladiosimulator.spd.targets.TargetGroup;

public class TargetGroupChecker implements Filter {

	private final TargetGroup targetGroup;
	
	public TargetGroupChecker(final TargetGroup targetGroup) {
		this.targetGroup = Objects.requireNonNull(targetGroup);
	}
	
	@Override
	public FilterResult doProcess(final FilterObjectWrapper objectWrapper) {
		final DESEvent event = objectWrapper.getEventToFilter();
		if (event instanceof MeasurementMade) {
			if (this.isInsideTargetGroup((MeasurementMade) event)) {
				return FilterResult.success(event);
			} else {
				return FilterResult.disregard("The measurement is not inside this target group");
			}
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

	/**
	 * Checks whether the measuring point of the event is somewhere inside the
	 * target group (or is the target group itself).
	 * 
	 * For ElasticInfrastructure, the measuring points to consider are
	 *  - ResourceEnvironmentMeasuringPoint
	 *  - ResourceContainerMeauringPoint
	 *  - ActiveResourceMeasuringPoint
	 * 
	 * For ServiceGroups, the measuring points to consider are all the measuring points
	 * that extend the {@link AssemblyReference} interface, such as
	 *   - AssemblyOperationMeasuringPoint
	 *   - AssemblyPassiveResourceMeasuringPoint
	 *   - ...
	 * 
	 * @param measurementMade The event
	 * @return true iff the measurement made was somewhere inside the target group.
	 */
	private boolean isInsideTargetGroup(final MeasurementMade measurementMade) {
		final MeasuringPoint measuringPoint = measurementMade.getEntity().getMeasuringPoint();
		
		boolean result = false;
		if (targetGroup instanceof final ElasticInfrastructure ei) {
			if (measuringPoint instanceof final ResourceEnvironmentMeasuringPoint remp) {
				result = remp.getResourceEnvironment().equals(ei.getPCM_ResourceEnvironment());
			} else if (measuringPoint instanceof final ResourceContainerMeasuringPoint rcmp) {
				result = ei.getPCM_ResourceEnvironment().getResourceContainer_ResourceEnvironment().stream()
								.anyMatch(container -> container.getId().equals(rcmp.getResourceContainer().getId()));
			} else if (measuringPoint instanceof final ActiveResourceMeasuringPoint armp) {
				result = ei.getPCM_ResourceEnvironment().getResourceContainer_ResourceEnvironment().stream()
								.flatMap(container -> container.getActiveResourceSpecifications_ResourceContainer().stream())
								.anyMatch(spec -> spec.getId().equals(armp.getActiveResource().getId()));
			}
		} else if (targetGroup instanceof final ServiceGroup sg) {
			if (measuringPoint instanceof final AssemblyReference ar) {
				result = ar.getAssembly().getId().equals(sg.getUnitAssembly().getId());
			}
		}
		
		return result;
	}
}
