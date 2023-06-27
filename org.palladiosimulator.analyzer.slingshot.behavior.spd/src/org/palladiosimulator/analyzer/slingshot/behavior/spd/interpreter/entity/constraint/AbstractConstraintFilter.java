package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.constraint;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.spd.constraints.AbstractConstraint;
import org.palladiosimulator.spd.constraints.policy.CooldownConstraint;
import org.palladiosimulator.spd.constraints.policy.IntervallConstraint;

/**
 * Defines accordingly to {@link PolicyConstraint} or {@link TargetConstraint} whether
 * the constraint is met and a trigger can happen or not.
 * 
 * @author Julijan Katic
 */
public abstract class AbstractConstraintFilter<T extends AbstractConstraint> implements Filter {

	protected final T constraint;
	
	public AbstractConstraintFilter(final T constraint) {
		this.constraint = constraint;
	}
	
	public static Filter createAbstractConstraintFilter(final AbstractConstraint constraint) {
		if (constraint instanceof final CooldownConstraint cooldownConstraint) {
			return new CooldownConstraintFilter(cooldownConstraint);
		} else if (constraint instanceof final IntervallConstraint intervallConstraint) {
			return new IntervallConstraintFilter(intervallConstraint);
		} else {
			throw new UnsupportedOperationException("Currently, only cooldown and intervall is supported");
		}
	}
}
