package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward.RewardEvaluator;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;

public abstract class LearningBasedModelEvaluator extends ModelEvaluator {

    protected final List<ModelAggregatorWrapper<?>> aggregatorList;

    public LearningBasedModelEvaluator(List<ModelAggregatorWrapper<?>> stimuliListeners,
            RewardEvaluator rewardEvaluator) {
        super();
        this.aggregatorList = stimuliListeners;
    }

    @Override
    public void recordUsage(MeasurementMade measurement) {
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : aggregatorList) {
            modelAggregatorWrapper.aggregateMeasurement(measurement);
        }
<<<<<<< Upstream, based on origin/master
=======
        this.rewardEvaluator.addMeasurement(measurement);
>>>>>>> 21ff2c8 Improved Reward Evaluation + Model Interpretation
    }
}
