package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.RepeatedSimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger.TriggerChecker;
import org.palladiosimulator.spd.adjustments.AdjustmentsFactory;
import org.palladiosimulator.spd.adjustments.StepAdjustment;
import org.palladiosimulator.spd.triggers.stimuli.Stimulus;

public class OutputInterpreterWrapper<T extends Stimulus> implements Filter{
	private final TriggerChecker<T> triggerChecker;
	
	private static final Logger LOGGER = Logger.getLogger(OutputInterpreterWrapper.class);
	
	public OutputInterpreterWrapper(TriggerChecker<T> triggerChecker) {
		this.triggerChecker = triggerChecker;
	}

	@Override
	public FilterResult doProcess(FilterObjectWrapper event) {
		if (event.getEventToFilter() instanceof RepeatedSimulationTimeReached) {
			// TODO IMPORTANT do the model evaluation (+ update) here
		} else {
			// TODO IMPORTANT do the aggregation here
		}
		FilterResult filterResult = this.triggerChecker.doProcess(event);
		StepAdjustment newAdjustment = AdjustmentsFactory.eINSTANCE.createStepAdjustment();
		// This will be where the magic will actually happen!
		int value = -1;
		if (Math.random() < 0.5) {
			value = 1;
		}
		newAdjustment.setStepValue(value);
		event.getState().getScalingPolicy().setAdjustmentType(newAdjustment);
		return filterResult;
	}

}
