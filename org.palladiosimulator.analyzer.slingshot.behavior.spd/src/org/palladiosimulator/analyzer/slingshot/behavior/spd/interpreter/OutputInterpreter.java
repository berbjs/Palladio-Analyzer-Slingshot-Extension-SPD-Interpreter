package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.RepeatedSimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ScalingTriggerInterpreter.InterpretationResult;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.OutputInterpreterWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.eventdriver.entity.Subscriber;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.adjustments.AdjustmentType;
import org.palladiosimulator.spd.adjustments.ModelBasedAdjustment;
import org.palladiosimulator.spd.adjustments.models.BaseModel;
import org.palladiosimulator.spd.triggers.SimpleFireOnOutput;
import org.palladiosimulator.spd.triggers.expectations.ExpectedValue;
import org.palladiosimulator.spd.triggers.expectations.NoExpectation;

final class OutputInterpreter {

    private OutputInterpreter() {
        super();
    }

    public static InterpretationResult getInterpretationResult(
            final ScalingTriggerInterpreter scalingTriggerInterpreter, final SimpleFireOnOutput trigger) {
        checkExpectedValue(trigger, NoExpectation.class);
        AdjustmentType adjustment = scalingTriggerInterpreter.policy.getAdjustmentType();
        if (!(adjustment instanceof ModelBasedAdjustment)) {
            throw new IllegalArgumentException(
                    String.format("It is only possible that the adjustment is of type %s, but the given type was %s",
                            ModelBasedAdjustment.class.getSimpleName(), adjustment.getClass()
                                .getSimpleName()));
        }
        BaseModel usedModel = ((ModelBasedAdjustment) adjustment).getUsedModel();
        final RepeatedSimulationTimeReached event = new RepeatedSimulationTimeReached(
                scalingTriggerInterpreter.policy.getTargetGroup()
                    .getId(),
                usedModel.getInitalIntervalDelay() + usedModel.getInterval(), 0.f, usedModel.getInterval());

        final ModelInterpreter modelInterpreter = new ModelInterpreter(trigger.getStimulus(),
                scalingTriggerInterpreter);
        final ModelEvaluator model = modelInterpreter.doSwitch(usedModel);

        return (new InterpretationResult()).scheduleEvent(event)
            .listenEvent(Subscriber.builder(RepeatedSimulationTimeReached.class)
                .name("simulationTimeReached"))
            .listenEvent(Subscriber.builder(MeasurementMade.class)
                .name("measurementMade"))
            .triggerChecker(new OutputInterpreterWrapper(model, trigger.getStimulus()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends ExpectedValue> T checkExpectedValue(final SimpleFireOnOutput trigger,
            final Class<T> expectedType) {
        if (!(expectedType.isAssignableFrom(trigger.getExpectedValue()
            .getClass()))) {
            throw new IllegalArgumentException(
                    String.format("It is only possible that the trigger is of type %s, but the given type was %s",
                            expectedType.getSimpleName(), trigger.getExpectedValue()
                                .getClass()
                                .getSimpleName()));
        }

        return (T) trigger.getExpectedValue();
    }
}