package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SpdBasedEvent;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger.TriggerChecker;
import org.palladiosimulator.analyzer.slingshot.core.api.SimulationDriver;
import org.palladiosimulator.analyzer.slingshot.eventdriver.entity.EventHandler;
import org.palladiosimulator.spd.ScalingPolicy;
import org.palladiosimulator.spd.triggers.ComposedTrigger;
import org.palladiosimulator.spd.triggers.SimpleFireOnTrend;
import org.palladiosimulator.spd.triggers.SimpleFireOnValue;
import org.palladiosimulator.spd.triggers.expectations.ExpectedTime;
import org.palladiosimulator.spd.triggers.stimuli.SimulationTime;
import org.palladiosimulator.spd.triggers.stimuli.util.StimuliSwitch;
import org.palladiosimulator.spd.triggers.util.TriggersSwitch;

public class ScalingTriggerInterpreter extends TriggersSwitch<ScalingTriggerInterpreter.InterpretationResult> {

	private final SimulationDriver driver;
	private final ScalingPolicy policy;

	public ScalingTriggerInterpreter(final SimulationDriver driver, final ScalingPolicy policy) {
		super();
		this.driver = driver;
		this.policy = policy;
	}

	@Override
	public InterpretationResult caseComposedTrigger(final ComposedTrigger object) {
		// TODO Auto-generated method stub
		return super.caseComposedTrigger(object);
	}

	@Override
	public InterpretationResult caseSimpleFireOnValue(final SimpleFireOnValue object) {
		final StimuliInterpreter stimuliInterpreter = new StimuliInterpreter(object);
		return stimuliInterpreter.doSwitch(object.getStimulus());
	}

	@Override
	public InterpretationResult caseSimpleFireOnTrend(final SimpleFireOnTrend object) {
		// TODO Auto-generated method stub
		return super.caseSimpleFireOnTrend(object);
	}


	static final class InterpretationResult {

		private TriggerChecker triggerChecker;
		private final List<SpdBasedEvent> eventsToSchedule = new ArrayList<>();
		private final List<EventListenerWithId<?>> eventsToListen = new ArrayList<>();

		public InterpretationResult triggerChecker(final TriggerChecker triggerChecker) {
			this.triggerChecker = triggerChecker;
			return this;
		}

		public InterpretationResult scheduleEvent(final SpdBasedEvent event) {
			this.eventsToSchedule.add(event);
			return this;
		}

		public InterpretationResult listenEvent(final EventListenerWithId<?> event) {
			this.eventsToListen.add(event);
			return this;
		}

		public TriggerChecker getTriggerChecker() {
			return this.triggerChecker;
		}

		public List<SpdBasedEvent> getEventsToSchedule() {
			return this.eventsToSchedule;
		}

		public List<EventListenerWithId<?>> getEventsToListen() {
			return this.eventsToListen;
		}

		static record EventListenerWithId<T>(String name, Class<T> eventToListen, EventHandler<T> listener) {
		}
	}

	final class StimuliInterpreter extends StimuliSwitch<InterpretationResult> {


		private final SimpleFireOnValue trigger;

		public StimuliInterpreter(final SimpleFireOnValue trigger) {
			super();
			this.trigger = trigger;
		}



		@Override
		public InterpretationResult caseSimulationTime(final SimulationTime object) {
			if (!(this.trigger.getExpectedValue() instanceof ExpectedTime)) {
				throw new IllegalArgumentException("In case of the stimuli being SimulationTime, it is only possible "
						+ "that the trigger is of type ExpectedTime, but the given type was "
						+ this.trigger.getExpectedValue().getClass().getSimpleName());
			}
			final ExpectedTime expectedTime = (ExpectedTime) this.trigger.getExpectedValue();

			final SimulationTimeReached event = new SimulationTimeReached(ScalingTriggerInterpreter.this.policy.getTargetGroup().getId(), expectedTime.getValue());

			return (new InterpretationResult()).scheduleEvent(event)
											   .triggerChecker(new TriggerChecker.SimulationTimeChecker(this.trigger));
		}

	}
}
