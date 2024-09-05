package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.Arrays;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.NotEmittableException;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.models.QThresholdsModel;

public class QThresholdsModelEvaluator extends LearningBasedModelEvaluator {

    private ModelAggregatorWrapper<?> stimulusListener;
    private ModelAggregatorWrapper<?> utilizationStimulus;
    private ModelAggregatorWrapper<?> responseTimeAggregator;
    private ModelAggregatorWrapper<?> utilizationAggregator;
    private double exponentialSteepness;
    private double targetResponseTime;

    public QThresholdsModelEvaluator(QThresholdsModel model, ModelAggregatorWrapper<?> stimulusListener,
            ModelAggregatorWrapper<?> utilizationStimulus) {
        super(Arrays.asList(stimulusListener, utilizationStimulus));
        this.stimulusListener = stimulusListener;
        this.utilizationStimulus = utilizationStimulus;
        ModelInterpreter modelInterpreter = new ModelInterpreter();
        this.exponentialSteepness = model.getExponentialSteepness();
        this.targetResponseTime = model.getTargetResponseTime();
        this.responseTimeAggregator = modelInterpreter.getAggregatorForStimulus(model.getResponseTimeStimulus(), model);
        this.utilizationAggregator = modelInterpreter.getAggregatorForStimulus(model.getUtilizationStimulus(), model);
    }

    @Override
    public int getDecision() throws NotEmittableException {
        Double input = aggregatorList.get(0)
            .getResult();
        double exponentialFactor = 1;
        if (responseTimeAggregator.getResult() > targetResponseTime) {
            exponentialFactor -= responseTimeAggregator.getResult() / targetResponseTime;
        }
        double reward = (1 - Math.exp(-exponentialSteepness * exponentialFactor))
                / (1 - utilizationAggregator.getResult());
        // TODO IMPORTANT Add model evaluation here
        return 0;
    }

    @Override
    void recordRewardMeasurement(MeasurementMade measurement) {
        this.responseTimeAggregator.aggregateMeasurement(measurement);
        this.utilizationAggregator.aggregateMeasurement(measurement);
    }
}
