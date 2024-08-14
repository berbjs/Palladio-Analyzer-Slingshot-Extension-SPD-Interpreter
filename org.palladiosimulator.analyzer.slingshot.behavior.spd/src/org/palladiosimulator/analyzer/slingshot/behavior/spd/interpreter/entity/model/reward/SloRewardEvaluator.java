package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.reward;

import javax.measure.Measure;

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
    public double getReward(Measure measure) {
        if (violatesObjective(measure)) {
            return 0;
        } else {
            return this.factor;
        }
    }
}
