package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.adjustor;
import java.util.Optional;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.ModelAdjustmentRequested;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.spd.ScalingPolicy;
import org.palladiosimulator.spd.adjustments.AdjustmentsFactory;
import org.palladiosimulator.spd.constraints.policy.CooldownConstraint;
import org.palladiosimulator.spd.triggers.SimpleFireOnOutput;
import org.palladiosimulator.spd.adjustments.StepAdjustment;

/**
 * This filter creates an {@link ModelAdjustmentRequested} event
 * at the end of the filter chain, which should trigger an adjustment.
 *
 * @author Julijan Katic, Floriment Klinaku
 */
public class Adjustor implements Filter {

	private final ScalingPolicy policy;


	public Adjustor(final ScalingPolicy policy) {
		super();
		this.policy = policy;
	}

	@Override
	public FilterResult doProcess(final FilterObjectWrapper objectWrapper) {
		final double currentSimTime = objectWrapper.getEventToFilter().time();
		// We reached the end, so we can safely say that here the adjustment happens.
		objectWrapper.getState().setLatestAdjustmentAtSimulationTime(
				objectWrapper.getEventToFilter().time());
		objectWrapper.getState().incrementNumberScales();


		objectWrapper.getState().getTargetGroupState().addEnactedPolicy(currentSimTime, policy);

		final Optional<CooldownConstraint> cooldownConstraint = policy.getPolicyConstraints().stream()
                .filter(obj -> obj instanceof CooldownConstraint)
                .map(obj -> (CooldownConstraint) obj).findAny();

		if(cooldownConstraint.isPresent()) {
			double cooldownTime=cooldownConstraint.get().getCooldownTime();
			
			if(cooldownTime!=0) {
				if(currentSimTime > objectWrapper.getState().getCoolDownEnd()) {
					//cooldown ended 
					objectWrapper.getState().setNumberOfScalesInCooldown(0);
					objectWrapper.getState().setCoolDownEnd(currentSimTime+cooldownTime);
				} 
				else if (currentSimTime==objectWrapper.getState().getCoolDownEnd()) 
				{
					objectWrapper.getState().setCoolDownEnd(currentSimTime+cooldownTime);
				}
				else {
					objectWrapper.getState().incrementNumberOfAdjustmentsInCooldown();
				}
			}
		}
		// TODO IMPORTANT test if this replacement of this.policy with objectWrapper.getState().getScalingPolicy() doesn't break anything
		return FilterResult.success(new ModelAdjustmentRequested(objectWrapper.getState().getScalingPolicy()));
	}

}
