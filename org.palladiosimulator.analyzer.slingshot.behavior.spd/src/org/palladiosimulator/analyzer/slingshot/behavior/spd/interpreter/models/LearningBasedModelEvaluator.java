package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models;

import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;

public abstract class LearningBasedModelEvaluator extends ModelEvaluator {

    protected final List<ModelAggregatorWrapper<?>> aggregatorList;

    public LearningBasedModelEvaluator(List<ModelAggregatorWrapper<?>> stimuliListeners) {
        super();
        this.aggregatorList = stimuliListeners;
    }

    @Override
    public void recordUsage(MeasurementMade measurement) {
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : aggregatorList) {
            modelAggregatorWrapper.aggregateMeasurement(measurement);
        }
    }
}
