package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.adjustor;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.StepBasedAdjustor;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterChain;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.FilterResult;
import org.palladiosimulator.spd.adjustments.AdjustmentType;
import org.palladiosimulator.spd.adjustments.StepAdjustment;
import org.palladiosimulator.spd.targets.TargetGroup;

public class Adjustor implements Filter {

	private final AdjustmentType adjustmentType;
	private final TargetGroup targetGroup;



	public Adjustor(final AdjustmentType adjustmentType, final TargetGroup targetGroup) {
		super();
		this.adjustmentType = adjustmentType;
		this.targetGroup = targetGroup;
	}

	@Override
	public FilterResult doProcess(final Object event) {
		if (this.adjustmentType instanceof final StepAdjustment stepAdjustment) {
			return FilterResult.success(new StepBasedAdjustor(this.targetGroup, stepAdjustment.getStepValue()));
		}
		return FilterResult.disregard("TODO: the other adjustors");
	}

	/* private ModelAdjusted stepAdjustment(final StepAdjustment stepAdjustment) {
		final ElasticInfrastructure elasticInfrastructure = (ElasticInfrastructure) this.targetGroup;

		if (stepAdjustment.getStepValue() > 0) {
			final List<ResourceContainer> addedContainers = new LinkedList<>();
			for(int i = 0; i < stepAdjustment.getStepValue(); ++i) {
				// TODO: how to know what exactly to copy?
				this.copyResourceContainer(elasticInfrastructure.getPCM_ResourceEnvironment().getResourceContainer_ResourceEnvironment().get(0), addedContainers);
			}
			return new ModelAdjusted(addedContainers, ModelAdjusted.Mode.ADDED);
		}
		if (stepAdjustment.getStepValue() < 0) {
			final List<ResourceContainer> deletedContainers = new LinkedList<>();
			for(int i = 0; i < -stepAdjustment.getStepValue(); ++i) {
				// TODO: how to know what exactly to copy?
				this.deleteResourceContainer(elasticInfrastructure.getPCM_ResourceEnvironment(),
										     elasticInfrastructure.getPCM_ResourceEnvironment().getResourceContainer_ResourceEnvironment().get(0), deletedContainers);
			}
			return new ModelAdjusted(deletedContainers, ModelAdjusted.Mode.DELETED);
		} else {
			return new ModelAdjusted(Collections.emptyList(), ModelAdjusted.Mode.UNCHANGED);
		}
	}

	private void copyResourceContainer(final ResourceContainer container, final List<? super ResourceContainer> addedResourceContainer) {
		// TODO
	}

	private void deleteResourceContainer(final ResourceEnvironment environment, final ResourceContainer resourceContainer, final List<? super ResourceContainer> deletedResourceContainer) {
		// TODO
	} */
}
