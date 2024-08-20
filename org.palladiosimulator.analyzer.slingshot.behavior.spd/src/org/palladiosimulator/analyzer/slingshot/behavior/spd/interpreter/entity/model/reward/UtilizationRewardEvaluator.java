package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.ModelInterpreter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.adjustments.models.rewards.UtilizationReward;

public class UtilizationRewardEvaluator extends RewardEvaluator {

    @SuppressWarnings("rawtypes")
    private ModelAggregatorWrapper aggregator;

    public UtilizationRewardEvaluator(UtilizationReward object, ModelInterpreter modelInterpreter) {
        this.aggregator = modelInterpreter.extracted(object.getStimulus());
    }

    @Override
    public double getReward() throws Exception {
        try {
            return this.aggregator.getResult();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new Exception();
    }

    @Override
    public void addMeasurement(MeasurementMade measurementMade) {
        this.aggregator.aggregateMeasurement(measurementMade);
    }

}
