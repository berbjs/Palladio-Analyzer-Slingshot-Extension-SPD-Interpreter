package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;

public abstract class ModelEvaluator {

    public abstract int getDecision() throws Exception;

    public abstract void recordUsage(MeasurementMade measurement);
}
