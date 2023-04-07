package org.palladiosimulator.analyzer.slingshot.behavior.spd.data;


import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment.AdjustmentResult;
import org.palladiosimulator.analyzer.slingshot.common.events.AbstractSimulationEvent;

public final class ModelAdjusted extends AbstractSimulationEvent implements SpdBasedEvent {

	private AdjustmentResult adjustmentResult;

	public ModelAdjusted(final AdjustmentResult adjustmentResult) {
		super();
		this.adjustmentResult = adjustmentResult;
	}

	public AdjustmentResult getAdjustmentResult() {
		return this.adjustmentResult;
	}
	
}
