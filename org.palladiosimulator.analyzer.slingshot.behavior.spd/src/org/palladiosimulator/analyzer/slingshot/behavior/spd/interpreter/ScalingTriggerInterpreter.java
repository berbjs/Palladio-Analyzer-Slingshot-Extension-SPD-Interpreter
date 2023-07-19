package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SpdBasedEvent;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.LogicalANDComboundFilter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.LogicalORCompoundFilter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.LogicalXORCompoundFilter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger.CPUUtilizationTriggerChecker;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger.OperationResponseTimeTriggerChecker;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger.SimulationTimeChecker;
import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;
import org.palladiosimulator.analyzer.slingshot.eventdriver.entity.Subscriber;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.ScalingPolicy;
import org.palladiosimulator.spd.targets.ElasticInfrastructure;
import org.palladiosimulator.spd.triggers.ComposedTrigger;
import org.palladiosimulator.spd.triggers.LogicalOperator;
import org.palladiosimulator.spd.triggers.SimpleFireOnTrend;
import org.palladiosimulator.spd.triggers.SimpleFireOnValue;
import org.palladiosimulator.spd.triggers.expectations.ExpectedPercentage;
import org.palladiosimulator.spd.triggers.expectations.ExpectedTime;
import org.palladiosimulator.spd.triggers.expectations.ExpectedValue;
import org.palladiosimulator.spd.triggers.stimuli.CPUUtilization;
import org.palladiosimulator.spd.triggers.stimuli.OperationResponseTime;
import org.palladiosimulator.spd.triggers.stimuli.SimulationTime;
import org.palladiosimulator.spd.triggers.stimuli.util.StimuliSwitch;
import org.palladiosimulator.spd.triggers.util.TriggersSwitch;

import com.google.common.base.Preconditions;

public class ScalingTriggerInterpreter extends TriggersSwitch<ScalingTriggerInterpreter.InterpretationResult> {

	private final ScalingPolicy policy;

	public ScalingTriggerInterpreter(final ScalingPolicy policy) {
		super();
		this.policy = policy;
	}

	@Override
	public InterpretationResult caseComposedTrigger(final ComposedTrigger object) {
		return object.getScalingtrigger().stream()
								  .map(this::doSwitch)
								  .reduce((res1, res2) -> res1.addFrom(res2, object.getLogicalOperator()))
								  .orElseGet(InterpretationResult::new);
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

		private Filter triggerChecker;
		private final List<SpdBasedEvent> eventsToSchedule = new ArrayList<>();
		private final List<Subscriber.Builder<? extends DESEvent>> eventsToListen = new ArrayList<>();

		public InterpretationResult triggerChecker(final Filter triggerChecker) {
			this.triggerChecker = triggerChecker;
			return this;
		}

		public InterpretationResult scheduleEvent(final SpdBasedEvent event) {
			this.eventsToSchedule.add(event);
			return this;
		}

		public InterpretationResult listenEvent(final Subscriber.Builder<? extends DESEvent> event) {
			this.eventsToListen.add(event);
			return this;
		}

		public Filter getTriggerChecker() {
			return this.triggerChecker;
		}

		public List<SpdBasedEvent> getEventsToSchedule() {
			return this.eventsToSchedule;
		}

		public List<Subscriber.Builder<? extends DESEvent>> getEventsToListen() {
			return this.eventsToListen;
		}

		// TODO: Please look at code style again..
		public InterpretationResult addFrom(final InterpretationResult other, final LogicalOperator operator) {
			this.eventsToSchedule.addAll(other.eventsToSchedule);
			this.eventsToListen.addAll(other.eventsToListen);

			if (this.triggerChecker == null) {
				/* We then simply set as the other */
				this.triggerChecker = other.triggerChecker;
			} else {
				switch (operator) {
				case AND:
					{
						final Filter temp = this.triggerChecker;
						if (temp instanceof final LogicalANDComboundFilter chain) {
							chain.add(other.triggerChecker);
						} else {
							final LogicalANDComboundFilter comboundFilter = new LogicalANDComboundFilter();
							comboundFilter.add(temp);
							comboundFilter.add(other.triggerChecker);
							this.triggerChecker = comboundFilter;
						}
					}
					break;
				case OR:
					{
						final LogicalORCompoundFilter comboundFilter = new LogicalORCompoundFilter();
						comboundFilter.add(this.triggerChecker);
						comboundFilter.add(other.triggerChecker);
						this.triggerChecker = comboundFilter;
					}
					break;
				case XOR:
					{
						final LogicalXORCompoundFilter comboundFilter = new LogicalXORCompoundFilter();
						comboundFilter.add(this.triggerChecker);
						comboundFilter.add(other.triggerChecker);
						this.triggerChecker = comboundFilter;
					}
					break;
				default:
					break;
				}
			}

			return this;
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
			final ExpectedTime expectedTime = this.checkExpectedValue(ExpectedTime.class);

			final SimulationTimeReached event = new SimulationTimeReached(ScalingTriggerInterpreter.this.policy.getTargetGroup().getId(), expectedTime.getValue());

			return (new InterpretationResult()).scheduleEvent(event)
											   .listenEvent(Subscriber.builder(SimulationTimeReached.class)
													   				  .name("something"))
											   .triggerChecker(new SimulationTimeChecker(this.trigger));
		}



		@Override
		public InterpretationResult caseOperationResponseTime(final OperationResponseTime object) {
			this.checkExpectedValue(ExpectedTime.class);
			return (new InterpretationResult()).listenEvent(Subscriber.builder(MeasurementMade.class)
																	  .name("measurementMade"))
										       .triggerChecker(new OperationResponseTimeTriggerChecker(this.trigger));
		}
		
		@Override
		public InterpretationResult caseCPUUtilization(CPUUtilization object) {
			final ExpectedPercentage expectedPercentage = this.checkExpectedValue(ExpectedPercentage.class);
			Preconditions.checkArgument(0 <= expectedPercentage.getValue() && expectedPercentage.getValue() <= 100, 
										"The expected percentage must be between 0 and 100");
			
			
			return (new InterpretationResult()).listenEvent(Subscriber.builder(MeasurementMade.class)
																	  .name("cpuUtilizationMade"))
											   .triggerChecker(new CPUUtilizationTriggerChecker(
													   				   this.trigger, 
																	   object.getAggregationOverElements(), 
																	   policy.getTargetGroup())
													   		  );
		}

		@SuppressWarnings("unchecked")
		private <T extends ExpectedValue> T checkExpectedValue(final Class<T> expectedType) {
			if (!(expectedType.isAssignableFrom(this.trigger.getExpectedValue().getClass()))) {
				throw new IllegalArgumentException(String.format("It is only possible "
						+ "that the trigger is of type %s, but the given type was %s",
						expectedType.getSimpleName(),
						this.trigger.getExpectedValue().getClass().getSimpleName()));
			}
			
			return (T) this.trigger.getExpectedValue();
		}
	}
}
