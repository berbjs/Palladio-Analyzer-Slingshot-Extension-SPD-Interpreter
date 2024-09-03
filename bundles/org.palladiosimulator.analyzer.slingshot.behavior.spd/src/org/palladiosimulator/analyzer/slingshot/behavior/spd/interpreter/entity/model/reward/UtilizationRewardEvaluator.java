package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.NotEmittableException;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.models.LearningBasedModel;
import org.palladiosimulator.spd.stimulus.Stimulus;
import org.palladiosimulator.spdmodelreward.UtilizationReward;

public class UtilizationRewardEvaluator extends RewardEvaluator {

    @SuppressWarnings("rawtypes")
    private ModelAggregatorWrapper aggregator;
    private Stimulus stimulus;

    public UtilizationRewardEvaluator(UtilizationReward object, ModelInterpreter modelInterpreter,
            LearningBasedModel model) {
        this.stimulus = object.getStimulus();
        this.aggregator = modelInterpreter.getAggregatorForStimulus(this.stimulus, model);
    }

    @Override
    public double getReward() throws NotEmittableException {
        try {
            return this.aggregator.getResult();
        } catch (Exception e) {
            throw new NotEmittableException("Values for Aggregator of " + this.stimulus.getClass()
                .getSimpleName() + " not emittable.");
        }
    }

    @Override
    public void recordMeasurement(MeasurementMade measurementMade) {
        this.aggregator.aggregateMeasurement(measurementMade);
    }

}
