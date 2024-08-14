package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;

public class RandomModelEvaluator extends ModelEvaluator {

    public RandomModelEvaluator() {
        super();
    }

    @Override
    public int getDecision() {
        final double rand = Math.random();
        if (rand < (1.f / 3.f)) {
            return 1;
        } else if (rand < (2.f / 3.f)) {
            return -1;
        }
        return 0;
    }

    @Override
    public void recordUsage(MeasurementMade measurement) {
        // the random model evaluator does not record any usage
    }

}
