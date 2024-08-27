package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.RepeatedSimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ScalingTriggerInterpreter.InterpretationResult;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger.ModelBasedTriggerChecker;
import org.palladiosimulator.analyzer.slingshot.eventdriver.entity.Subscriber;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.models.BaseModel;
import org.palladiosimulator.spd.targets.TargetGroup;

final class OutputInterpreter {

    private OutputInterpreter() {
        super();
    }

    public static InterpretationResult getInterpretationResult(TargetGroup targetGroup, final BaseModel model) {
        final RepeatedSimulationTimeReached event = new RepeatedSimulationTimeReached(targetGroup.getId(),
                model.getInitalIntervalDelay() + model.getInterval(), 0.f, model.getInterval());

        final ModelInterpreter modelInterpreter = new ModelInterpreter();
        final ModelEvaluator modelEvaluator = modelInterpreter.doSwitch(model);

        return (new InterpretationResult()).scheduleEvent(event)
            .listenEvent(Subscriber.builder(RepeatedSimulationTimeReached.class)
                .name("simulationTimeReached"))
            .listenEvent(Subscriber.builder(MeasurementMade.class)
                .name("measurementMade"))
            .triggerChecker(new ModelBasedTriggerChecker(modelEvaluator));
    }
}