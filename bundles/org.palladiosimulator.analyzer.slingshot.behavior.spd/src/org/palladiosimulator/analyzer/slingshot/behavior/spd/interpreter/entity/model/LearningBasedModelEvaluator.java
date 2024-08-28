package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward.RewardEvaluator;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;

public abstract class LearningBasedModelEvaluator extends ModelEvaluator {

    protected final List<ModelAggregatorWrapper<?>> aggregatorList;
    protected final RewardEvaluator rewardEvaluator;

    LearningBasedModelEvaluator(List<ModelAggregatorWrapper<?>> stimuliListeners, RewardEvaluator rewardEvaluator) {
        super();
        this.aggregatorList = stimuliListeners;
        this.rewardEvaluator = rewardEvaluator;
    }

    @Override
    public void recordUsage(MeasurementMade measurement) {
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : aggregatorList) {
            modelAggregatorWrapper.aggregateMeasurement(measurement);
        }
        this.rewardEvaluator.recordMeasurement(measurement);
    }
}
