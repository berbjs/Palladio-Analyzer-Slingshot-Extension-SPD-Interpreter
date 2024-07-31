package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.RepeatedSimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ScalingTriggerInterpreter.InterpretationResult;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.OutputInterpreterWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.eventdriver.entity.Subscriber;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.adjustments.AdjustmentType;
import org.palladiosimulator.spd.adjustments.ModelBasedAdjustment;
import org.palladiosimulator.spd.adjustments.models.BaseModel;
import org.palladiosimulator.spd.triggers.SimpleFireOnOutput;
import org.palladiosimulator.spd.triggers.expectations.ExpectedValue;
import org.palladiosimulator.spd.triggers.expectations.NoExpectation;
import org.palladiosimulator.spd.triggers.stimuli.OperationResponseTime;
import org.palladiosimulator.spd.triggers.stimuli.SimulationTime;
import org.palladiosimulator.spd.triggers.stimuli.Stimulus;
import org.palladiosimulator.spd.triggers.stimuli.util.StimuliSwitch;

final class OutputInterpreter extends StimuliSwitch<InterpretationResult> {

    private final ScalingTriggerInterpreter scalingTriggerInterpreter;
    private final SimpleFireOnOutput trigger;

    public OutputInterpreter(final ScalingTriggerInterpreter scalingTriggerInterpreter,
            final SimpleFireOnOutput trigger) {
        super();
        this.scalingTriggerInterpreter = scalingTriggerInterpreter;
        this.trigger = trigger;
    }

    private <T extends Stimulus> InterpretationResult getScheduledRepeatedEvent() {
        this.checkExpectedValue(NoExpectation.class);
        AdjustmentType adjustment = this.scalingTriggerInterpreter.policy.getAdjustmentType();
        if (!(adjustment instanceof ModelBasedAdjustment)) {
            throw new IllegalArgumentException(
                    String.format("It is only possible that the adjustment is of type %s, but the given type was %s",
                            ModelBasedAdjustment.class.getSimpleName(), adjustment.getClass()
                                .getSimpleName()));
        }
        BaseModel usedModel = ((ModelBasedAdjustment) adjustment).getUsedModel();
        final RepeatedSimulationTimeReached event = new RepeatedSimulationTimeReached(
                this.scalingTriggerInterpreter.policy.getTargetGroup()
                    .getId(),
                usedModel.getInitalIntervalDelay() + usedModel.getInterval(), 0.f, usedModel.getInterval());

        final ModelInterpreter modelInterpreter = new ModelInterpreter();
        final ModelEvaluator model = modelInterpreter.doSwitch(usedModel);

        return (new InterpretationResult()).scheduleEvent(event)
            .listenEvent(Subscriber.builder(RepeatedSimulationTimeReached.class)
                .name("simulationTimeReached"))
            .triggerChecker(new OutputInterpreterWrapper<T>(model));
    }

    @Override
    public InterpretationResult caseSimulationTime(final SimulationTime object) {
        // TODO this case is not really relevant for the ML trigger
        return getScheduledRepeatedEvent();
    }

    @Override
    public InterpretationResult caseOperationResponseTime(final OperationResponseTime object) {
        this.checkExpectedValue(NoExpectation.class);
        return getScheduledRepeatedEvent().listenEvent(Subscriber.builder(MeasurementMade.class)
            .name("measurementMade"));
    }

//    @Override
//    public InterpretationResult caseCPUUtilization(final CPUUtilization object) {
//        this.checkExpectedValue(NoExpectation.class);
//        final ExpectedPercentage expectedPercentage = this.checkExpectedValue(ExpectedPercentage.class);
//        Preconditions.checkArgument(0 <= expectedPercentage.getValue() && expectedPercentage.getValue() <= 100,
//                "The expected percentage must be between 0 and 100");
//
//        return getScheduledRepeatedEvent(new CPUUtilizationTriggerChecker(this.trigger, object,
//                this.scalingTriggerInterpreter.policy.getTargetGroup()))
//                    .listenEvent(Subscriber.builder(MeasurementMade.class)
//                        .name("cpuUtilizationMade"));
//    }
//
////    @Override
//    public InterpretationResult caseTaskCount(final TaskCount object) {
//        this.checkExpectedValue(NoExpectation.class);
//        return getScheduledRepeatedEvent(new TaskCountTriggerChecker(this.trigger, object,
//                this.scalingTriggerInterpreter.policy.getTargetGroup()))
//                    .listenEvent(Subscriber.builder(MeasurementMade.class)
//                        .name("taskCount"));
//    }
//
//    @Override
//    public InterpretationResult caseQueueLength(final QueueLength object) {
//        if (!(this.scalingTriggerInterpreter.policy.getTargetGroup() instanceof CompetingConsumersGroup)) {
//            throw new IllegalArgumentException("The QueueLength trigger is only for CompetingConsumersGroup");
//        }
//        this.checkExpectedValue(NoExpectation.class);
//
//        return getScheduledRepeatedEvent(new QueueLengthTriggerChecker(this.trigger, object))
//            .listenEvent(Subscriber.builder(MeasurementMade.class)
//                .name("queueLength"));
//    }
//
    @SuppressWarnings("unchecked")
    private <T extends ExpectedValue> T checkExpectedValue(final Class<T> expectedType) {
        if (!(expectedType.isAssignableFrom(this.trigger.getExpectedValue()
            .getClass()))) {
            throw new IllegalArgumentException(
                    String.format("It is only possible that the trigger is of type %s, but the given type was %s",
                            expectedType.getSimpleName(), this.trigger.getExpectedValue()
                                .getClass()
                                .getSimpleName()));
        }

        return (T) this.trigger.getExpectedValue();
    }
}