package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.models.QThresholdsModel;

public class QThresholdsModelEvaluator extends LearningBasedModelEvaluator {

    private ModelAggregatorWrapper<?> responseTimeAggregator;
    private ModelAggregatorWrapper<?> utilizationAggregator;
    private double exponentialSteepness;
    private double targetResponseTime;

    public QThresholdsModelEvaluator(QThresholdsModel model, List<ModelAggregatorWrapper<?>> stimuliListeners) {
        super(stimuliListeners);
        ModelInterpreter modelInterpreter = new ModelInterpreter();
        this.exponentialSteepness = model.getExponentialSteepness();
        this.targetResponseTime = model.getTargetResponseTime();
        this.responseTimeAggregator = modelInterpreter.getAggregatorForStimulus(model.getResponseTimeStimulus(), model);
        this.utilizationAggregator = modelInterpreter.getAggregatorForStimulus(model.getUtilizationStimulus(), model);
    }

    @Override
    public int getDecision() throws Exception {
        List<Double> input = new ArrayList<>();
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : aggregatorList) {
            input.add(modelAggregatorWrapper.getResult());
        }
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
