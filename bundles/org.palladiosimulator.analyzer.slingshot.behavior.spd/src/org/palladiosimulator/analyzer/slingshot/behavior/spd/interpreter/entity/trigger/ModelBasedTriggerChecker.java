package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.RepeatedSimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.NotEmittableException;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.ModelBasedScalingPolicy;

/**
 * This class listens to events of the type {@link RepeatedSimulationTimeReached} and
 * {@link MeasurementMade} and handles them accordingly:
 * 
 * - Events of type {@link RepeatedSimulationTimeReached} will be forwarded to the given
 * {@link ModelEvaluator} {@link #model} where a scaling decision is made and then returned. If
 * necessary, a new adjustment with the returned adjustment value is created.
 * 
 * - Events of type {@link MeasurementMade} will also be forwarded to
 * {@link ModelEvaluator#recordUsage(MeasurementMade)}, where they will be recorded (both for input
 * aggregation and for the reward)
 * 
 * @author Jens Berberich
 *
 */
public class ModelBasedTriggerChecker implements Filter {
    private final ModelEvaluator model;

    private static final Logger LOGGER = Logger.getLogger(ModelBasedTriggerChecker.class);

    public ModelBasedTriggerChecker(ModelEvaluator model) {
        this.model = model;
    }

    @Override
    public FilterResult doProcess(FilterObjectWrapper event) {
        if (event.getEventToFilter() instanceof RepeatedSimulationTimeReached) {
            int value;
            try {
                value = model.getDecision();
                LOGGER.info("Model scaling decision: " + model.getDecision());
            } catch (NotEmittableException e) {
                LOGGER.info(e.getMessage());
                return FilterResult.disregard(event.getEventToFilter());
            }
            if (event.getState()
                .getScalingPolicy() instanceof ModelBasedScalingPolicy modelBasedScalingPolicy) {
                modelBasedScalingPolicy.setAdjustment(value);
            }
        } else if (event.getEventToFilter() instanceof MeasurementMade measurementMade) {
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
