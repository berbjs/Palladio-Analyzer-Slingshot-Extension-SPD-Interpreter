package org.palladiosimulator.analyzer.slingshot.behavior.spd.data;

import org.palladiosimulator.spd.ScalingPolicy;
import org.palladiosimulator.spd.targets.TargetGroup;

/**
 *
 * @author Julijan Katic
 * @deprecated Use ModelAdjustmentRequested instead
 */
public final class StepBasedAdjustor extends AdjustorBasedEvent {

	private final int step;
	private final ScalingPolicy enactedPolicy;

	public StepBasedAdjustor(final TargetGroup targetGroup, 
			final int step, final ScalingPolicy policy) {
		super(targetGroup);
		this.step = step;
		this.enactedPolicy = policy;
	}

	public int getStepCount() {
		return this.step;
	}
	
	public ScalingPolicy getEnactedPolicy() {
		return this.enactedPolicy;
	}
	
}
