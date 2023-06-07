package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.QvtoModelTransformation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.QvtoReconfigurator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.ModelAdjusted;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.ModelAdjustmentRequested;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment.ResourceEnvironmentChange;
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
import org.palladiosimulator.semanticspd.ElasticInfrastructureCfg;
import org.palladiosimulator.semanticspd.SemanticspdFactory;
import org.palladiosimulator.spd.SPD;
import org.palladiosimulator.spd.targets.ElasticInfrastructure;
import org.palladiosimulator.spd.targets.TargetGroup;

@OnEvent(when = ModelAdjustmentRequested.class, then = ModelAdjusted.class, cardinality = EventCardinality.SINGLE)
public class SpdAdjustmentBehavior implements SimulationBehaviorExtension {
	
	private static final Logger LOGGER = Logger.getLogger(SpdAdjustmentBehavior.class);

	private final boolean activated;
	
	private final SPD spd;
	private final QvtoReconfigurator reconfigurator;
	private final Iterable<QvtoModelTransformation> transformations;
	private final Allocation allocation;
	private final MonitorRepository monitorRepository;
	
	@Inject
	public SpdAdjustmentBehavior(
			final Allocation allocation, 
			final @Nullable MonitorRepository monitorRepository,
			final SPD spd,
			final QvtoReconfigurator reconfigurator,
			@Named(SpdAdjustorModule.MAIN_QVTO) final Iterable<QvtoModelTransformation> transformations) {
		this.activated = monitorRepository != null;
		this.allocation = allocation;
		this.monitorRepository = monitorRepository;
		this.spd = spd;
		this.reconfigurator = reconfigurator;
		this.transformations = transformations;
	}
	
	@Override
	public boolean isActive() {
		return this.activated;
	}

	@Subscribe
	public Result<ModelAdjusted> onModelAdjustmentRequested(final ModelAdjustmentRequested event) {
		final ResourceEnvironment environment = getResourceEnvironmentFromTargetGroup(event.getScalingPolicy().getTargetGroup());
		final List<ResourceContainer> oldContainers = new ArrayList<>(environment.getResourceContainer_ResourceEnvironment());
		
		LOGGER.debug("Number of resource container before: " + environment.getResourceContainer_ResourceEnvironment().size());
		if (LOGGER.isDebugEnabled()) {
			this.transformations.forEach(trans -> LOGGER.debug(trans.toString()));
		}
		
		final Configuration configuration = createConfiguration(event, environment);
		
		this.reconfigurator.getModelCache().storeModel(configuration);
		final boolean result = this.reconfigurator.execute(this.transformations);
		LOGGER.debug("RECONFIGURATION WAS " + result);
		
		if (result) {
			LOGGER.debug("Number of resource container is now: " + environment.getResourceContainer_ResourceEnvironment().size());
			
			final List<ResourceContainer> newResourceContainers = new ArrayList<>(environment.getResourceContainer_ResourceEnvironment());
			newResourceContainers.removeAll(oldContainers);
			
			final List<ResourceContainer> deletedResourceContainers = new ArrayList<>(oldContainers);
			deletedResourceContainers.removeAll(deletedResourceContainers);
			
			return Result.of(new ModelAdjusted(true, List.of(ResourceEnvironmentChange.builder()
																.resourceEnvironment(environment)
																.simulationTime(event.time())
																.oldResourceContainers(oldContainers)
																.newResourceContainers(newResourceContainers)
																.deletedResourceContainers(deletedResourceContainers)
																.build())));
		} else {
			return Result.of(new ModelAdjusted(false, Collections.emptyList()));
		}
		
	}

	private ElasticInfrastructureCfg createElasticInfrastructureCfg(final ResourceEnvironment environment) {
		final ElasticInfrastructureCfg targetGroupConfig = SemanticspdFactory.eINSTANCE.createElasticInfrastructureCfg();
		targetGroupConfig.setResourceEnvironment(environment);
		targetGroupConfig.getElements().addAll(environment.getResourceContainer_ResourceEnvironment());
		return targetGroupConfig;
	}
	
	/**
	 * Helper method for creating the {@link Configuration}
	 */
	private Configuration createConfiguration(final ModelAdjustmentRequested event,
			final ResourceEnvironment environment) {
		final Configuration configuration = SemanticspdFactory.eINSTANCE.createConfiguration();
		configuration.setAllocation(allocation);
		configuration.setResourceEnvironment(environment);
		configuration.setSpd(spd);
		configuration.setSystem(allocation.getSystem_Allocation());
		configuration.setRepository(allocation.getSystem_Allocation().getAssemblyContexts__ComposedStructure().get(0).getEncapsulatedComponent__AssemblyContext().getRepository__RepositoryComponent()); // TODO: What to do here?
		configuration.setEnactedPolicy(event.getScalingPolicy());
		
		final ElasticInfrastructureCfg targetGroupConfig = createElasticInfrastructureCfg(environment);
		configuration.getTargetCfgs().add(targetGroupConfig);
		
		return configuration;
	}
	
	private static ResourceEnvironment getResourceEnvironmentFromTargetGroup(final TargetGroup targetGroup) {
		if (targetGroup instanceof final ElasticInfrastructure ei) {
			return ei.getPCM_ResourceEnvironment();
		}
		
		throw new UnsupportedOperationException("Currently, only elastic infrastructures are supported...");
	}
}
