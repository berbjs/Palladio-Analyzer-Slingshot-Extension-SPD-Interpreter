package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import java.util.PrimitiveIterator.OfInt;
import java.util.Random;

import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.models.RandomModel;

public class RandomModelEvaluator extends ModelEvaluator {

    private OfInt scalingDecisions;
    private double probability;

    public RandomModelEvaluator(RandomModel model) {
        super();
        this.scalingDecisions = new Random().ints(model.getMinAdjustment(), model.getMaxAdjustment() + 1)
            .iterator();
        this.probability = model.getProbability();
    }

    @Override
    public int getDecision() {
        if (Math.random() >= this.probability) {
            return 0;
        }
        return scalingDecisions.nextInt();
    }

    @Override
    public void recordUsage(MeasurementMade measurement) {
        // the random model evaluator does not record any usage
    }

}
