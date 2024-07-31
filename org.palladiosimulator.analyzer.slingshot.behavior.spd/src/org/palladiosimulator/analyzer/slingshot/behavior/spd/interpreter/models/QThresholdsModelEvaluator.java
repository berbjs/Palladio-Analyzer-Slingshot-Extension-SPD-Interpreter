package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models;

import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.AbstractWindowAggregation;
import org.palladiosimulator.spd.adjustments.models.QThresholdsModel;

public class QThresholdsModelEvaluator extends ModelEvaluator {

    private List<AbstractWindowAggregation> windowAggregationList;

    public QThresholdsModelEvaluator(QThresholdsModel model) {
        super();
        this.windowAggregationList = windowAggregationList;
    }

    @Override
    public int getDecision() {
        // TODO Auto-generated method stub
        return 0;
    }

}
