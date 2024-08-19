package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import org.palladiosimulator.analyzer.slingshot.monitor.data.entities.SlingshotMeasuringValue;

public abstract class RewardEvaluator {
    public abstract double getReward();

    public abstract void addMeasurement(SlingshotMeasuringValue slingshotMeasuringValue);
}
