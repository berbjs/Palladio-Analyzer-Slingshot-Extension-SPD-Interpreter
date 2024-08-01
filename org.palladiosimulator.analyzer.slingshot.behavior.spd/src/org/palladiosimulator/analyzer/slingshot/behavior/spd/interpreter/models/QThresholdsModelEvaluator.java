package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.adjustments.models.QThresholdsModel;

public class QThresholdsModelEvaluator extends ModelEvaluator {

    private List<ModelAggregatorWrapper> aggregatorList;

    public QThresholdsModelEvaluator(QThresholdsModel model, List<ModelAggregatorWrapper> stimuliListeners) {
        super();
        this.aggregatorList = stimuliListeners;
    }

    @Override
    public int getDecision() throws Exception {
        List<Double> input = new ArrayList<>();
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : aggregatorList) {
            input.add(modelAggregatorWrapper.getResult());
        }
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void recordUsage(MeasurementMade measurement) {
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : aggregatorList) {
            modelAggregatorWrapper.aggregateMeasurement(measurement);
        }
    }

}
