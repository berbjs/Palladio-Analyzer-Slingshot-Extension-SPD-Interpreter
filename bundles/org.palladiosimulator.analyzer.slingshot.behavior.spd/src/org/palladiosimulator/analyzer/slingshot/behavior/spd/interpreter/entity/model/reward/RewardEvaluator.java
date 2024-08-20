package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;

public abstract class RewardEvaluator {
    public abstract double getReward() throws Exception;

    public abstract void addMeasurement(MeasurementMade measurementMade);
}
