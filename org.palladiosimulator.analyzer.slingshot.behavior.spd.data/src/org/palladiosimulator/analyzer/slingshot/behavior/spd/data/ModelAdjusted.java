package org.palladiosimulator.analyzer.slingshot.behavior.spd.data;

import java.util.List;

import org.palladiosimulator.analyzer.slingshot.common.events.AbstractSimulationEvent;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;

public final class ModelAdjusted extends AbstractSimulationEvent implements SpdBasedEvent {

	private final List<ResourceContainer> changedResourceContainer;
	private final Mode mode;



	public ModelAdjusted(final List<ResourceContainer> changedResourceContainer, final Mode mode) {
		super();
		this.changedResourceContainer = changedResourceContainer;
		this.mode = mode;
	}



	public static enum Mode {
		UNCHANGED, ADDED, DELETED
	}
}
