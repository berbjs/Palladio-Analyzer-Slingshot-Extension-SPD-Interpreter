package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;

public abstract class LearningBasedModelEvaluator extends ModelEvaluator {

    final List<ModelAggregatorWrapper<?>> aggregatorList;

    LearningBasedModelEvaluator(List<ModelAggregatorWrapper<?>> stimuliListeners, boolean changeOnStimulus,
            boolean changeOnInterval) {
        super();
        this.aggregatorList = stimuliListeners;
        this.changeOnStimulus = changeOnStimulus;
        this.changeOnInterval = changeOnInterval;
    }

    @Override
    public void recordUsage(MeasurementMade measurement) {
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : aggregatorList) {
            modelAggregatorWrapper.aggregateMeasurement(measurement);
        }
        this.recordRewardMeasurement(measurement);
    }

    abstract void recordRewardMeasurement(MeasurementMade measurement);
}
