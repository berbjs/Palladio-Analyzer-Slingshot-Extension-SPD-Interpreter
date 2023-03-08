package org.palladiosimulator.analyzer.slingshot.behavior.spd.data;

import org.palladiosimulator.analyzer.slingshot.common.events.AbstractSimulationEvent;
import org.palladiosimulator.spd.targets.TargetGroup;

public abstract sealed class AdjustorBasedEvent extends AbstractSimulationEvent implements SpdBasedEvent
	permits StepBasedAdjustor {

	private final TargetGroup targetGroup;

	public AdjustorBasedEvent(final TargetGroup targetGroup) {
		this.targetGroup = targetGroup;
	}

	public TargetGroup getTargetGroup() {
		return this.targetGroup;
	}
}
