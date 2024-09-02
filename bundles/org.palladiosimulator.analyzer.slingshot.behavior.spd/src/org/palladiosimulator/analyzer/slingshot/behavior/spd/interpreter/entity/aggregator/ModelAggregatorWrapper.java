package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.metricspec.BaseMetricDescription;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.spd.stimulus.Stimulus;

public abstract class ModelAggregatorWrapper<T extends Stimulus> {

    protected MetricSetDescription metricSetDescription;
    protected BaseMetricDescription baseMetricDescription;

    public abstract double getResult() throws Exception; // TODO IMPORTANT think about whether this
                                                         // exception is necessary, how it should be
                                                         // handled

    public abstract void aggregateMeasurement(final MeasurementMade measurementMade);
}
