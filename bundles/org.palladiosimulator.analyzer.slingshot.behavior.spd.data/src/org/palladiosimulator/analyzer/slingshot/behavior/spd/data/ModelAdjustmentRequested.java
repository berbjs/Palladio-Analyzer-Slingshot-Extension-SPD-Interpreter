package org.palladiosimulator.analyzer.slingshot.behavior.spd.data;

import java.util.Objects;

import org.palladiosimulator.analyzer.slingshot.common.events.AbstractSimulationEvent;
import org.palladiosimulator.spd.ScalingPolicy;

/**
 * Tells that a certain adjustment to the PCM model is requested. An adjustor
 * module can then perform these adjustments.
 * <br>
 * This event holds the enacted scaling policy that was fired as well as
 * the target group.
 * 
 * @author Julijan Katic
 */
public final class ModelAdjustmentRequested extends AbstractSimulationEvent implements SpdBasedEvent {

	private final ScalingPolicy scalingPolicy;
	private final ScalingDirection scalingDirection;
	private final int scalingMagnitude;
	
	public ModelAdjustmentRequested(final ScalingPolicy scalingPolicy, final ScalingDirection scalingDirection, final int scalingMagnitude) {
		this.scalingPolicy = Objects.requireNonNull(scalingPolicy);
		this.scalingDirection = scalingDirection;
		this.scalingMagnitude = scalingMagnitude;
	}
	
	public ModelAdjustmentRequested(final ScalingPolicy scalingPolicy) {
		this.scalingPolicy = Objects.requireNonNull(scalingPolicy);
		this.scalingDirection = ScalingDirection.UNDEFINED;
		this.scalingMagnitude = 0;
	}
	
	public ScalingPolicy getScalingPolicy() {
		return this.scalingPolicy;
	}
	
	public ScalingDirection getScalingDirection() {
		return this.scalingDirection;
	}

	/**
	 * @return the magnitude of scaling if {@link #getScalingDirection()}  != {@link ScalingDirection#UNDEFINED}, else 0
	 */
	public int getScalingMagnitude() {
		return this.scalingMagnitude;
	}
}
