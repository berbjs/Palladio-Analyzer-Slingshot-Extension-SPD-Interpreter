package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SpdBasedEvent;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.SPDAdjustorContext;
import org.palladiosimulator.analyzer.slingshot.core.api.SimulationDriver;
import org.palladiosimulator.spd.SPD;
import org.palladiosimulator.spd.ScalingPolicy;
import org.palladiosimulator.spd.util.SpdSwitch;

/**
 *
 * @author Julijan Katic
 */
class SpdInterpreter extends SpdSwitch<SpdInterpreter.InterpretationResult> {

	private static final Logger LOGGER = Logger.getLogger(SpdInterpreter.class);

	private final SimulationDriver driver;


	SpdInterpreter(final SimulationDriver driver) {
		this.driver = driver;
	}

	@Override
	public InterpretationResult caseSPD(final SPD spd) {
		LOGGER.debug("Interpreting SPD Model " + spd.getEntityName() + "[" + spd.getId() + "]");



		return spd.getScalingPolicies().stream()
									   .map(this::doSwitch)
									   .reduce(InterpretationResult::add)
									   .orElseGet(() -> InterpretationResult.EMPTY_RESULT);
	}

	@Override
	public InterpretationResult caseScalingPolicy(final ScalingPolicy policy) {
		if (!policy.isActive()) {
			return InterpretationResult.EMPTY_RESULT;
		}
		final ScalingTriggerInterpreter.InterpretationResult intrResult = (new ScalingTriggerInterpreter(this.driver, policy)).doSwitch(policy.getScalingTrigger());
		return (new InterpretationResult())
				.adjustorContext(new SPDAdjustorContext(policy, intrResult.getTriggerChecker()))
				.eventsToSchedule(intrResult.getEventsToSchedule());
	}

	/**
	 * An object that combines all the necessary information of interpretation result.
	 *
	 * @author Julijan Katic
	 */
	static final class InterpretationResult {

		public static final InterpretationResult EMPTY_RESULT = new InterpretationResult();

		private final List<SPDAdjustorContext> adjustorContexts;
		private final List<SpdBasedEvent> eventsToSchedule;

		InterpretationResult() {
			this.adjustorContexts = new ArrayList<>();
			this.eventsToSchedule = new ArrayList<>();
		}

		InterpretationResult(final List<SPDAdjustorContext> adjustorContexts,
							 final List<SpdBasedEvent> eventsToSchedule) {
			this.adjustorContexts = new ArrayList<>(adjustorContexts);
			this.eventsToSchedule = new ArrayList<>(eventsToSchedule);
		}

		/**
		 * Copies a result from another result.
		 *
		 * @param other
		 */
		InterpretationResult(final InterpretationResult other) {
			this(other.adjustorContexts, other.eventsToSchedule);
		}

		public InterpretationResult adjustorContext(final SPDAdjustorContext adjustorContext) {
			this.adjustorContexts.add(adjustorContext);
			return this;
		}

		public InterpretationResult adjustorContext(final Collection<? extends SPDAdjustorContext> adjustorContexts) {
			this.adjustorContexts.addAll(adjustorContexts);
			return this;
		}

		public InterpretationResult eventsToSchedule(final Collection<? extends SpdBasedEvent> eventsToSchedule) {
			this.eventsToSchedule.addAll(eventsToSchedule);
			return this;
		}

		/**
		 * Adds the results from another interpretation result to this.
		 *
		 * @param other The other result.
		 * @return
		 */
		public InterpretationResult add(final InterpretationResult other) {
			this.adjustorContexts.addAll(other.adjustorContexts);
			this.eventsToSchedule.addAll(other.eventsToSchedule);
			return this;
		}
	}
}
