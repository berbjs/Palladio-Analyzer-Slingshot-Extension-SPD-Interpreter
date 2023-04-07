package org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment;

import java.util.Collections;
import java.util.List;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;

public class ResourceEnvironmentChange extends ModelChange<ResourceEnvironment> {
	
	private final int currentSize;
	private final List<ResourceContainerChange> changedContainers;
	
	public ResourceEnvironmentChange(final Builder builder) {
		super(builder.container, builder.previousState, builder.simulationTime, deriveModeFromNewSize(builder.previousState, builder.currentSize));
		this.currentSize = builder.currentSize;
		this.changedContainers = builder.changedContainers;
		this.changedContainers.forEach(cc -> cc.setParent(this));
	}

	public int getCurrentSize() {
		return this.currentSize;
	}
	
	public List<ResourceContainerChange> changedContainers() {
		return this.changedContainers;
	}
	
	private static Mode deriveModeFromNewSize(final ResourceEnvironmentChange previousState, final int currentSize) {
		if (previousState == null) {
			return Mode.ADDITION;
		}
		
		return switch(Integer.compare(previousState.currentSize, currentSize)) {
			case -1 -> Mode.ADDITION;
			case 0 -> Mode.LOCAL_CHANGE;
			case 1 -> Mode.DELETION;
			default -> Mode.UNCHANGED;
		};
	}
	
	public Builder transform() {
		return builder().previousState(this);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static final class Builder {
		private ResourceEnvironment container;
		private ResourceEnvironmentChange previousState;
		private double simulationTime;
		private int currentSize;
		private List<ResourceContainerChange> changedContainers;
		
		private Builder() {}
		
		public Builder container(final ResourceEnvironment container) {
			this.container = container;
			return this;
		}
		
		public Builder previousState(final ResourceEnvironmentChange previousState) {
			this.previousState = previousState;
			return this;
		}
		
		public Builder simulationTime(final double simulationTime) {
			this.simulationTime = simulationTime;
			return this;
		}
		
		public Builder currentSize(final int currentSize) {
			this.currentSize = currentSize;
			return this;
		}
		
		public Builder changedContainers(final List<ResourceContainerChange> changedContainers) {
			this.changedContainers = changedContainers;
			return this;
		}
		
		public ResourceEnvironmentChange build() {
			return new ResourceEnvironmentChange(this);
		}
	}
}
