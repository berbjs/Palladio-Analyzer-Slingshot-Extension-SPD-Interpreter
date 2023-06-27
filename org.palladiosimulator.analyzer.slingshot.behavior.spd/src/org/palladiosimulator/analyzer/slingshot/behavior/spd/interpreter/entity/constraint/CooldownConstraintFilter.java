package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.constraint;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.spd.constraints.policy.CooldownConstraint;

public class CooldownConstraintFilter extends AbstractConstraintFilter<CooldownConstraint> {

	public CooldownConstraintFilter(final CooldownConstraint constraint) {
		super(constraint);
	}

	@Override
	public FilterResult doProcess(final FilterObjectWrapper event) {
		final int numberScales = event.getState().numberOfScales();
		final double lastScaling = event.getState().getLatestAdjustmentAtSimulationTime();
		
		if (event.getEventToFilter().time() >= lastScaling + constraint.getCooldownTime()) {
			if (numberScales < constraint.getMaxScalingOperations()) {
				return FilterResult.success(event.getEventToFilter());
			} else {
				return FilterResult.disregard(String.format("Max number scales reached: %d >= %d", numberScales, constraint.getMaxScalingOperations()));
			}
		} else {
			return FilterResult.disregard(String.format("Cooldown not reached yet: %d < %d", event.getEventToFilter().time() - lastScaling, constraint.getCooldownTime()));
		}
	}

}
