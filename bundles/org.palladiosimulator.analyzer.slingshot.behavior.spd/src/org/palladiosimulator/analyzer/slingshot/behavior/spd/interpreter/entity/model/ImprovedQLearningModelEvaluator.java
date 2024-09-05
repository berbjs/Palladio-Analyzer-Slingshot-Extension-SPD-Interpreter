package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.NotEmittableException;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.models.ImprovedQLearningModel;

public class ImprovedQLearningModelEvaluator extends LearningBasedModelEvaluator {

    private Double state;
    private int action;
    private final double epsilon;
    private int actionCount;
    private Random random;
    private double alpha;
    private ModelAggregatorWrapper<?> responseTimeAggregator;
    private ModelAggregatorWrapper<?> utilizationAggregator;
    private double exponentialSteepness;
    private double targetResponseTime;

    public ImprovedQLearningModelEvaluator(ImprovedQLearningModel model,
            ModelAggregatorWrapper<?> modelAggregatorWrapper) {
        super(Collections.singletonList(modelAggregatorWrapper));
        ModelInterpreter modelInterpreter = new ModelInterpreter();
        this.exponentialSteepness = model.getExponentialSteepness();
        this.targetResponseTime = model.getTargetResponseTime();
        this.responseTimeAggregator = modelInterpreter.getAggregatorForStimulus(model.getResponseTimeStimulus(), model);
        this.utilizationAggregator = modelInterpreter.getAggregatorForStimulus(model.getUtilizationStimulus(), model);
        epsilon = model.getEpsilon();
        actionCount = model.getActionCount();
        alpha = model.getLearningRate();
        random = new Random();
    }

    @Override
    public int getDecision() throws NotEmittableException {
        List<Double> input = new ArrayList<>();
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : this.aggregatorList) {
            input.add(modelAggregatorWrapper.getResult());
        }
        double reward = (1
                - Math.exp(-exponentialSteepness * (1 - (responseTimeAggregator.getResult() / targetResponseTime))))
                / (1 - utilizationAggregator.getResult());
        // TODO IMPORTANT Add model evaluation here
        return 0;
    }

    @Override
    void recordRewardMeasurement(MeasurementMade measurement) {
        // TODO Auto-generated method stub

    }
}
