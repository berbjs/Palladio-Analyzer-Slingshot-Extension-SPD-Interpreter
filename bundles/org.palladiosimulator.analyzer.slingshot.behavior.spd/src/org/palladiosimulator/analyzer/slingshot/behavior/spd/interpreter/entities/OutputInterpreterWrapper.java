package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.RepeatedSimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.adjustments.AdjustmentsFactory;
import org.palladiosimulator.spd.adjustments.StepAdjustment;
import org.palladiosimulator.spd.triggers.stimuli.Stimulus;

public class OutputInterpreterWrapper implements Filter {
    private final ModelEvaluator model;

    private static final Logger LOGGER = Logger.getLogger(OutputInterpreterWrapper.class);

    public OutputInterpreterWrapper(ModelEvaluator model, EList<Stimulus> stimuli) {
        this.model = model;
    }

    @Override
    public FilterResult doProcess(FilterObjectWrapper event) {
        if (event.getEventToFilter() instanceof RepeatedSimulationTimeReached) {
            // TODO IMPORTANT do the model evaluation (+ update) here
            int value;
            try {
                value = model.getDecision();
            } catch (Exception e) {
                LOGGER.info("Some aggregator was unable to be aggregated");
                return FilterResult.disregard(event.getEventToFilter());
            }
            StepAdjustment newAdjustment = AdjustmentsFactory.eINSTANCE.createStepAdjustment();
            newAdjustment.setStepValue(value);
            event.getState()
                .getScalingPolicy()
                .setAdjustmentType(newAdjustment);
        } else if (event.getEventToFilter() instanceof MeasurementMade measurementMade) {
            // TODO IMPORTANT do the aggregation here
            LOGGER.debug("Received a datapoint collection event!");
            this.model.recordUsage(measurementMade);
            return FilterResult.disregard(event.getEventToFilter());
        } else {
            LOGGER.debug("Received an unexpected event");
            return FilterResult.disregard(event.getEventToFilter());
        }
        return FilterResult.success(event.getEventToFilter());
    }

}
