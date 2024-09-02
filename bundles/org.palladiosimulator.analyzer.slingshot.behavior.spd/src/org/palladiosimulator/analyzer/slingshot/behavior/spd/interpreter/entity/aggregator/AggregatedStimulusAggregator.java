package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.targetgroup.TargetGroupChecker;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.spd.stimulus.AggregatedStimulus;
import org.palladiosimulator.spd.stimulus.ManagedElementsStateStimulus;
import org.palladiosimulator.spd.stimulus.Stimulus;

/**
 * This class implements the base functionality for aggregating {@link ManagedElementsStateStimulus}
 * elements. Measurements for this elements need to be aggregated first, which is done by
 * {@link #aggregateMeasurement(MeasurementMade)}. Afterwards, {@link #getResult()} checks whether
 * enough measurements were made, and which value is aggregated.
 * 
 * The precondition is that the measuring point, where the measurements are coming from, are inside
 * the target group. This is done in the {@link TargetGroupChecker} filter, that should be placed
 * before this filter.
 * 
 * @author Jens Berberich, based on work by Julijan Katic
 *
 * @param <T>
 *            The concrete element the class is checking for.
 */
public class AggregatedStimulusAggregator<T extends AggregatedStimulus> extends AnyStimulusAggregator<Stimulus> {
    public AggregatedStimulusAggregator(final T stimulus, final double windowSize) {
        super(stimulus.getAggregatedStimulus(), windowSize, stimulus.getAggregationMethod());
    }
}
