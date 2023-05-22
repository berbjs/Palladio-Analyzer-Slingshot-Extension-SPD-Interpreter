package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.QvtoModelTransformation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.QvtoReconfigurator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.ModelAdjusted;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.StepBasedAdjustor;
import org.palladiosimulator.analyzer.slingshot.common.annotations.Nullable;
import org.palladiosimulator.analyzer.slingshot.core.extension.SimulationBehaviorExtension;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.Subscribe;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.EventCardinality;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.OnEvent;
import org.palladiosimulator.analyzer.slingshot.eventdriver.returntypes.Result;
import org.palladiosimulator.monitorrepository.MonitorRepository;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.semanticspd.Configuration;
import org.palladiosimulator.semanticspd.SemanticspdFactory;
import org.palladiosimulator.spd.SPD;

@OnEvent(when = StepBasedAdjustor.class, then = ModelAdjusted.class, cardinality = EventCardinality.SINGLE)
public class StepAdjustmentBehavior extends AbstractAdjustmentExecutor implements SimulationBehaviorExtension {
	
	private static final Logger LOGGER = Logger.getLogger(StepAdjustmentBehavior.class);

	private final boolean activated;
	
	private final SPD spd;
	private final QvtoReconfigurator reconfigurator;
	private final Iterable<QvtoModelTransformation> transformations;
	
	@Inject
	public StepAdjustmentBehavior(
			final Allocation allocation, 
			final @Nullable MonitorRepository monitorRepository,
			final SPD spd,
			final QvtoReconfigurator reconfigurator,
			@Named(SpdAdjustorModule.MAIN_QVTO) final Iterable<QvtoModelTransformation> transformations) {
		super(allocation, monitorRepository);
		this.activated = monitorRepository != null;
		this.spd = spd;
		this.reconfigurator = reconfigurator;
		this.transformations = transformations;
	}
	
	@Override
	public boolean isActive() {
		return this.activated;
	}

	@Subscribe
	public Result<ModelAdjusted> onStepBasedAdjustor(final StepBasedAdjustor event) {
		//this.initializeBuilder(event.time());
		final ResourceEnvironment environment = getResourceEnvironmentFromTargetGroup(event.getTargetGroup());
		
		// for debugging only!
		/* final int oldSize = environment.getResourceContainer_ResourceEnvironment().size();
		
		if (event.getStepCount() > 0) {
			final List<ResourceContainer> newContainers = new ArrayList<>(event.getStepCount());
			copyContainers(environment, newContainers, event.getStepCount());
			environment.getResourceContainer_ResourceEnvironment().addAll(newContainers);
		} else if (event.getStepCount() < 0) {
			deleteContainers(environment, -event.getStepCount());
		} else {
			LOGGER.info("The step count was 0");
			return Result.empty();
		}
		
		LOGGER.debug(String.format("ResourceEnvironment CHANGE: Old size was %d, but is now %d", oldSize, environment.getResourceContainer_ResourceEnvironment().size()));
		*/
		
		this.transformations.forEach(trans -> LOGGER.debug(trans.toString()));
		
		// 1. Create Configuration
		final Configuration configuration = SemanticspdFactory.eINSTANCE.createConfiguration();
		configuration.setAllocation(allocation);
		configuration.setResourceEnvironment(environment);
		configuration.setSpd(spd);
		configuration.setSystem(allocation.getSystem_Allocation());
		configuration.setRepository(allocation.getSystem_Allocation().getAssemblyContexts__ComposedStructure().get(0).getEncapsulatedComponent__AssemblyContext().getRepository__RepositoryComponent()); // TODO: What to do here?

		// 2. Now execute
		this.reconfigurator.getModelCache().storeModel(configuration);
		final boolean result = this.reconfigurator.execute(this.transformations, null, null);
		
		LOGGER.info("RECONFIGURATION WAS " + result);
		//return Result.of(finalizeBuilder());
		
		
		
		return Result.empty();
	}
	
}
