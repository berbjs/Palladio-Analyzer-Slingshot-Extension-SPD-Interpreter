package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;

import org.palladiosimulator.analyzer.slingshot.monitor.data.entities.SlingshotMeasuringValue;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.monitorrepository.MeasurementSpecification;
import org.palladiosimulator.servicelevelobjective.ServiceLevelObjective;
import org.palladiosimulator.spd.adjustments.models.rewards.SLOReward;

/**
 * 
 * @author Jens Berberich, based on the SLOViolationEDP2DatasourceFilter
 *
 */
public class SloRewardEvaluator extends RewardEvaluator {
    private final ServiceLevelObjective serviceLevelObjective;
    private final double factor;
    private Measure<Object, Quantity> aggregatedMeasure;

    public SloRewardEvaluator(SLOReward reward) {
        this.factor = reward.getFactor();
        this.serviceLevelObjective = reward.getServicelevelobjective();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean violatesObjective(Measure measure) {
        if (serviceLevelObjective.getLowerThreshold() != null) {
            final Measure lowerThreshold = serviceLevelObjective.getLowerThreshold()
                .getThresholdLimit();
            if (measure.compareTo(lowerThreshold) < 0) {
                return true;
            }
        }
        if (serviceLevelObjective.getUpperThreshold() != null) {
            final Measure upperThreshold = serviceLevelObjective.getUpperThreshold()
                .getThresholdLimit();
            if (measure.compareTo(upperThreshold) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getReward() {
        if (violatesObjective(this.aggregatedMeasure)) {
            return this.factor;
        } else {
            return 0;
        }
    }

    @Override
    public void addMeasurement(MeasurementMade measurementMade) {
        // TODO IMPORTANT do some more sophisticated aggregation here, perhaps average with reset
        // after each interval? However, this will need to be done as measurement
        SlingshotMeasuringValue measure = measurementMade.getEntity();
        MeasurementSpecification measurementSpecification = this.serviceLevelObjective.getMeasurementSpecification();
        if (measurementSpecification.getMonitor()
            .getMeasuringPoint()
            .getResourceURIRepresentation()
            .equals(measure.getMeasuringPoint()
                .getResourceURIRepresentation())) {
            this.aggregatedMeasure = measure.getMeasureForMetric(measurementSpecification.getMetricDescription());
        }
    }
}
