package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.RepeatedSimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models.ModelEvaluator;
import org.palladiosimulator.spd.adjustments.AdjustmentsFactory;
import org.palladiosimulator.spd.adjustments.StepAdjustment;
import org.palladiosimulator.spd.triggers.stimuli.Stimulus;

public class OutputInterpreterWrapper<T extends Stimulus> implements Filter {
    private final ModelEvaluator model;

    private static final Logger LOGGER = Logger.getLogger(OutputInterpreterWrapper.class);

    public OutputInterpreterWrapper(ModelEvaluator model) {
        this.model = model;
    }

    @Override
    public FilterResult doProcess(FilterObjectWrapper event) {
        if (event.getEventToFilter() instanceof RepeatedSimulationTimeReached) {
            // TODO IMPORTANT do the model evaluation (+ update) here
            int value = model.getDecision();
            StepAdjustment newAdjustment = AdjustmentsFactory.eINSTANCE.createStepAdjustment();
            newAdjustment.setStepValue(value);
            event.getState()
                .getScalingPolicy()
                .setAdjustmentType(newAdjustment);
        } else {
            // TODO IMPORTANT do the aggregation here
            LOGGER.debug("Received a datapoint collection event!");
        }
        return FilterResult.success(event.getEventToFilter());
    }

}
