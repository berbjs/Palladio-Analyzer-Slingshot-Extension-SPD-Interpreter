package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.metricspec.BaseMetricDescription;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.spd.targets.TargetGroup;

public abstract class ModelAggregatorWrapper<T> {

    protected TargetGroup targetGroup;
    protected T managedElementsStateStimulus;
    protected MetricSetDescription metricSetDescription;
    protected BaseMetricDescription baseMetricDescription;

    @SuppressWarnings("unchecked")
    public ModelAggregatorWrapper(final T stimulus, final TargetGroup targetGroup,
            final MetricSetDescription metricSetDescription, final BaseMetricDescription baseMetricDescription) {

        this.targetGroup = targetGroup;
        this.managedElementsStateStimulus = stimulus;
        this.metricSetDescription = metricSetDescription;
        this.baseMetricDescription = baseMetricDescription;
    }

    public abstract double getResult() throws Exception; // TODO IMPORTANT think about whether this
                                                         // exception is necessary, how it should be
                                                         // handled

    public abstract void aggregateMeasurement(final MeasurementMade measurementMade);
}
