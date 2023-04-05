package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.spd.triggers.SimpleFireOnValue;
import org.palladiosimulator.spd.triggers.expectations.ExpectedTime;
import org.palladiosimulator.spd.triggers.stimuli.SimulationTime;

public final class SimulationTimeChecker extends TriggerChecker<SimulationTime> {

	public SimulationTimeChecker(final SimpleFireOnValue trigger) {
		super(trigger, SimulationTime.class);
	}

	@Override
	public FilterResult doProcess(final Object event) {
		if (!(event instanceof SimulationTimeReached)) {
			return FilterResult.disregard("");
		}
		if (!(this.trigger.getExpectedValue() instanceof ExpectedTime)) {
			return FilterResult.disregard("");
		}

		final SimulationTimeReached simulationTimeReached = (SimulationTimeReached) event;
		final ExpectedTime expectedTime = (ExpectedTime) this.trigger.getExpectedValue();

		if (this.compareValues(expectedTime.getValue(), simulationTimeReached.time())) {
			return FilterResult.success(event);
		} else {
			return FilterResult.disregard(String.format("Simulationtime of %f is not [%s] expectedValue %f", simulationTimeReached.time(), this.trigger.getRelationalOperator().toString(), expectedTime.getValue()));
		}

	}

}
