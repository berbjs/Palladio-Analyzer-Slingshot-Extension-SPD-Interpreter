package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

import java.util.Objects;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.adjustor.Adjustor;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.targetgroup.TargetGroupChecker;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger.TriggerChecker;
import org.palladiosimulator.spd.ScalingPolicy;

public final class SPDAdjustorContext {

	private final FilterChain filterChain = new FilterChain();

	private final ScalingPolicy scalingPolicy;

	public SPDAdjustorContext(final ScalingPolicy policy, final TriggerChecker triggerChecker) {
		this.scalingPolicy = policy;

		this.filterChain.add(new TargetGroupChecker(policy.getTargetGroup()));

		// TODO: Add trigger checker based on composition
		this.filterChain.add(triggerChecker);

		// TODO: Add policy constraints

		this.filterChain.add(new Adjustor(policy.getAdjustmentType(), policy.getTargetGroup()));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.scalingPolicy.getId());
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof final SPDAdjustorContext otherContext) {
			return Objects.equals(this.scalingPolicy.getId(), otherContext.scalingPolicy.getId());
		}
		return false;
	}
}
