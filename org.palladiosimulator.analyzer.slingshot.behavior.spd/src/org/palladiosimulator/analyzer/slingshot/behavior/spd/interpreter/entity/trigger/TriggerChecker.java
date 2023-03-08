package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterChain;
import org.palladiosimulator.spd.triggers.ScalingTrigger;
import org.palladiosimulator.spd.triggers.SimpleFireOnValue;
import org.palladiosimulator.spd.triggers.expectations.ExpectedTime;
import org.palladiosimulator.spd.triggers.stimuli.SimulationTime;

import com.google.common.base.Preconditions;

public abstract class TriggerChecker implements Filter {
	
	private TriggerChecker() {
	}

	public static final class SimulationTimeChecker extends TriggerChecker {
		
		private final SimpleFireOnValue trigger;

		public SimulationTimeChecker(SimpleFireOnValue trigger) {
			Preconditions.checkArgument(trigger.getStimulus().getClass().equals(SimulationTime.class));
			this.trigger = trigger;
		}

		

		@Override
		public void doProcess(Object event, FilterChain chain) {
			if (!(event instanceof SimulationTimeReached)) {
				chain.disregard("");
			}
			if (!(trigger.getExpectedValue() instanceof ExpectedTime)) {
				chain.disregard("");
			}
			
			final SimulationTimeReached simulationTimeReached = (SimulationTimeReached) event;
			final ExpectedTime expectedTime = (ExpectedTime) trigger.getExpectedValue();
			
			if (simulationTimeReached.time() >= expectedTime.getValue()) {
				chain.next(event);
			} else {
				chain.disregard("");
			}
		}
		
	}

}
