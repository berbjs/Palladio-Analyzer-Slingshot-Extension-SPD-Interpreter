package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.constraint;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.spd.constraints.policy.IntervallConstraint;

public class IntervallConstraintFilter extends AbstractConstraintFilter<IntervallConstraint> {

	/** The sum of the offset and the interval size. */
	private final double delta;

	public IntervallConstraintFilter(final IntervallConstraint constraint) {
		super(constraint);
		this.delta = constraint.getOffset() + constraint.getIntervallDuration();
	}

	@Override
	public FilterResult doProcess(final FilterObjectWrapper event) {
		final double currentSimulationTime = event.getEventToFilter().time();
		if (currentSimulationTime % delta > constraint.getOffset()) {
			return FilterResult.success(event.getEventToFilter());
		} else {
			return FilterResult.disregard(String.format("The simulation time is outside of the offset: %f <= %d",
					(currentSimulationTime % delta), constraint.getOffset()));
		}

	}

}
