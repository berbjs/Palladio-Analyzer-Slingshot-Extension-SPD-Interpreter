package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.adjustor;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.ModelAdjustmentRequested;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.StepBasedAdjustor;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterChain;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.spd.ScalingPolicy;
import org.palladiosimulator.spd.adjustments.AdjustmentType;
import org.palladiosimulator.spd.adjustments.StepAdjustment;
import org.palladiosimulator.spd.targets.TargetGroup;

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
	public FilterResult doProcess(final Object event) {
		return FilterResult.success(new ModelAdjustmentRequested(this.policy));
	}

}
