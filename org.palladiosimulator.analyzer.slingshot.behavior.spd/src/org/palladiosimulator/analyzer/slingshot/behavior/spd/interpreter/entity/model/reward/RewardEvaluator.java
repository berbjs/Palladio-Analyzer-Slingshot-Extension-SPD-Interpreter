package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import javax.measure.Measure;

public abstract class RewardEvaluator {
    public abstract double getReward(Measure measure);
}
