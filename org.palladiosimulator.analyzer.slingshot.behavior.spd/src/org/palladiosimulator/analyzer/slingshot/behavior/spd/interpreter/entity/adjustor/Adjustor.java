package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.adjustor;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.ModelAdjustmentRequested;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.spd.ScalingPolicy;

/**
 * This filter creates an {@link ModelAdjustmentRequested} event
 * at the end of the filter chain, which should trigger an adjustment.
 * 
 * @author Julijan Katic
 */
public class Adjustor implements Filter {

	private final ScalingPolicy policy;


	public Adjustor(final ScalingPolicy policy) {
		super();
		this.policy = policy;
	}

	@Override
	public FilterResult doProcess(final FilterObjectWrapper objectWrapper) {
		// We reached the end, so we can safely say that here the adjustment happens.
		objectWrapper.getState().setLatestAdjustmentAtSimulationTime(
				objectWrapper.getEventToFilter().time());
		objectWrapper.getState().incrementNumberScales();
		return FilterResult.success(new ModelAdjustmentRequested(this.policy));
	}

}
