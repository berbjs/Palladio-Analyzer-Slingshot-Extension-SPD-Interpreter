package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models;

public class RandomModelEvaluator extends ModelEvaluator {

    public RandomModelEvaluator() {
        super();
    }

    @Override
    public int getDecision() {
        final double rand = Math.random();
        if (rand < (1 / 3)) {
            return 1;
        } else if (rand < (2 / 3)) {
            return -1;
        }
        return 0;
    }

}
