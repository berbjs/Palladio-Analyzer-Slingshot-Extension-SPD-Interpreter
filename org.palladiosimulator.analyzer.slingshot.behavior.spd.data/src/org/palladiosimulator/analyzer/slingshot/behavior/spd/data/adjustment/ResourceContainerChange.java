package org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;

public class ResourceContainerChange extends ModelChange<ResourceContainer> {

	private ResourceEnvironmentChange parent;
	
	public ResourceContainerChange(final Builder builder) {
		super(builder.reference, builder.previousState, builder.simulationTime, builder.mode);
		this.parent = builder.parent;
	}
	
	void setParent(final ResourceEnvironmentChange parent) {
		this.parent = parent;
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private ResourceContainer reference;
		private ResourceContainerChange previousState;
		private double simulationTime;
		private Mode mode;
		private ResourceEnvironmentChange parent;
		
		public Builder reference(final ResourceContainer reference) {
			this.reference = reference;
			return this;
		}
		
		public Builder previousState(final ResourceContainerChange previousState) {
			this.previousState = previousState;
			return this;
		}
		
		public Builder simulationTime(final double simulationTime) {
			this.simulationTime = simulationTime;
			return this;
		}
		
		public Builder mode(final Mode mode) {
			this.mode = mode;
			return this;
		}
		
		public Builder parent(final ResourceEnvironmentChange parent) {
			this.parent = parent;
			return this;
		}
		
		public ResourceContainerChange build() {
			return new ResourceContainerChange(this);
		}
	}
}
