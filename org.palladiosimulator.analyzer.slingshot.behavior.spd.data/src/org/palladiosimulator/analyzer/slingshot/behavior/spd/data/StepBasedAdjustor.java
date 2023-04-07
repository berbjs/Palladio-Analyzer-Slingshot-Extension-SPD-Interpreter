package org.palladiosimulator.analyzer.slingshot.behavior.spd.data;

import org.palladiosimulator.spd.targets.TargetGroup;

/**
 *
 * @author Julijan Katic
 *
 */
public final class StepBasedAdjustor extends AdjustorBasedEvent {

	private final int step;

	public StepBasedAdjustor(final TargetGroup targetGroup, final int step) {
		super(targetGroup);
		this.step = step;
	}

	public int getStepCount() {
		return this.step;
	}

}
