package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.ModelAdjusted;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment.AdjustmentResult;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment.ModelChange;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment.ResourceContainerChange;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment.ResourceEnvironmentChange;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment.ModelChange.Mode;
import org.palladiosimulator.commons.emfutils.EMFLoadHelper;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.monitorrepository.Monitor;
import org.palladiosimulator.monitorrepository.MonitorRepository;
import org.palladiosimulator.monitorrepository.MonitorRepositoryFactory;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcmmeasuringpoint.ActiveResourceMeasuringPoint;
import org.palladiosimulator.pcmmeasuringpoint.PcmmeasuringpointFactory;
import org.palladiosimulator.spd.targets.ElasticInfrastructure;
import org.palladiosimulator.spd.targets.TargetGroup;
import com.google.common.base.Preconditions;

public abstract class AbstractAdjustmentExecutor {
	
	protected final Allocation allocation;
	protected final MonitorRepository monitorRepository;
	
	private AdjustmentResult.Builder adjustmentResultBuilder;
	private double simulationTime;
	
	public AbstractAdjustmentExecutor(Allocation allocation, MonitorRepository monitorRepository) {
		super();
		this.allocation = allocation;
		this.monitorRepository = monitorRepository;
	}
	
	protected void initializeBuilder(final double time) {
		this.adjustmentResultBuilder = new AdjustmentResult.Builder();
		this.simulationTime = time;
	}
	
	protected ModelAdjusted finalizeBuilder() {
		return new ModelAdjusted(this.adjustmentResultBuilder.build());
	}

	protected static ResourceEnvironment getResourceEnvironmentFromTargetGroup(final TargetGroup targetGroup) {
		if (targetGroup instanceof final ElasticInfrastructure ei) {
			return ei.getPCM_ResourceEnvironment();
		}
		
		throw new UnsupportedOperationException("Currently, only elastic infrastructures are supported...");
	}
	
	protected void copyContainers(final ResourceEnvironment environment,
				final Collection<? super ResourceContainer> destination,
				final int times) {
		Preconditions.checkArgument(times >= 0, String.format("Only a positive number of times is allowed, but is %d", times));
		final ResourceContainer container = this.getRandomContainer(environment);
		final List<ResourceContainerChange> changes = this.copyContainer(container, environment, destination, times);
		adjustmentResultBuilder.change(ResourceEnvironmentChange.builder()
				.container(environment)
				.currentSize(environment.getResourceContainer_ResourceEnvironment().size() + times)
				.simulationTime(this.simulationTime)
				.changedContainers(changes)
				.build());
	}
	
	protected List<ResourceContainerChange> copyContainer(final ResourceContainer containerToCopy, 
			final ResourceEnvironment environment, 
			final Collection<? super ResourceContainer> destination,
			final int times) {
		final List<ResourceContainerChange> containerChange = new ArrayList<>(times);
		for (int i = 0; i < times; ++i) {
			final ResourceContainer copy = EcoreUtil.copy(containerToCopy);
			copy.setId(EcoreUtil.generateUUID());
			
			connectToLinkingResources(environment.getLinkingResources__ResourceEnvironment(), containerToCopy, copy);
			copyAllocationContexts(containerToCopy, copy);
			
			containerToCopy.getActiveResourceSpecifications_ResourceContainer().forEach(spec -> copyActiveResourceSpec(spec, copy));
			
			destination.add(copy);
			
			containerChange.add(ResourceContainerChange.builder()
					.reference(copy)
					.mode(Mode.ADDITION)
					.simulationTime(this.simulationTime)
					.build());
		}
		return containerChange;
	}
	
	// Homogeneous.. so copy first element
	protected ResourceContainer getRandomContainer(final ResourceEnvironment environment) {
		if (environment.getResourceContainer_ResourceEnvironment().isEmpty()) {
			throw new IllegalStateException("The following environment does not contain any containers!: " + environment.getEntityName());
		}
		return environment.getResourceContainer_ResourceEnvironment().get(0);
	}
	
	protected void copyMonitor(final Monitor monitor, final MeasuringPoint measuringPoint) {
		final Monitor copy = MonitorRepositoryFactory.eINSTANCE.createMonitor();
		
		copy.setMeasuringPoint(measuringPoint);
		copy.setActivated(monitor.isActivated());
		copy.setId(EcoreUtil.generateUUID());
		copy.setEntityName(monitor.getEntityName());
		
		copy.setMonitorRepository(monitorRepository);
		monitorRepository.getMonitors().add(copy);
		
		copy.getMeasurementSpecifications().addAll(EcoreUtil.copyAll(monitor.getMeasurementSpecifications()));
		copy.getMeasurementSpecifications().forEach(spec -> spec.setMonitor(copy));
		
		// adjustment result
	}
	
	protected void copyActiveResourceSpec(final ProcessingResourceSpecification spec, final ResourceContainer copy) {
		final ProcessingResourceSpecification specCopy = EcoreUtil.copy(spec);
		specCopy.setId(EcoreUtil.generateUUID());
		
		copy.getActiveResourceSpecifications_ResourceContainer().stream()
						 .filter(s -> s.getId().equals(spec.getId()))
						 .findFirst()
						 .ifPresent(s -> copy.getActiveResourceSpecifications_ResourceContainer().remove(s));
		
		copy.getActiveResourceSpecifications_ResourceContainer().add(specCopy);
		
		monitorRepository.getMonitors().stream()
					.filter(monitor -> monitor.getMeasuringPoint().getResourceURIRepresentation().equals(EMFLoadHelper.getResourceURI(spec)))
					.findAny()
					.ifPresent(monitor -> {
						final ActiveResourceMeasuringPoint copyMeasuringPoint = PcmmeasuringpointFactory.eINSTANCE.createActiveResourceMeasuringPoint();
						copyMeasuringPoint.setActiveResource(specCopy);
						copyMeasuringPoint.setMeasuringPointRepository(monitor.getMeasuringPoint().getMeasuringPointRepository());
						monitor.getMeasuringPoint().getMeasuringPointRepository().getMeasuringPoints().add(copyMeasuringPoint);
						
						copyMonitor(monitor, copyMeasuringPoint);
					});
	}
	
	protected void copyAllocationContexts(final ResourceContainer originalContainer, final ResourceContainer copy) {
		final List<AllocationContext> newAllocationContexts = new ArrayList<>(allocation.getAllocationContexts_Allocation().size());
		allocation.getAllocationContexts_Allocation().stream()
					.filter(allocationContext -> allocationContext.getResourceContainer_AllocationContext().getId().equals(originalContainer.getId()))
					.forEach(allocationContext -> copyAllocationAndAssemblyContext(allocationContext, copy, newAllocationContexts));
		allocation.getAllocationContexts_Allocation().addAll(newAllocationContexts);
	}
	
	protected void copyAllocationAndAssemblyContext(final AllocationContext allocationContext, final ResourceContainer copy, final Collection<? super AllocationContext> newAllocationContexts) {
		final AllocationContext copiedContext = EcoreUtil.copy(allocationContext);
		copiedContext.setId(EcoreUtil.generateUUID());
		final AssemblyContext copiedAssemblyContext = EcoreUtil.copy(allocationContext.getAssemblyContext_AllocationContext());
		copiedAssemblyContext.setId(EcoreUtil.generateUUID());
		
		allocationContext.getAssemblyContext_AllocationContext().getParentStructure__AssemblyContext()
				.getAssemblyContexts__ComposedStructure()
				.add(copiedAssemblyContext);
		
		copiedContext.setAssemblyContext_AllocationContext(copiedAssemblyContext);
		copiedContext.setResourceContainer_AllocationContext(copy);
		newAllocationContexts.add(copiedContext);
		
		// result
	}
	
	protected void connectToLinkingResources(final Collection<? extends LinkingResource> linkingResources, final ResourceContainer container, final ResourceContainer copy) {
		linkingResources.stream()
						.map(LinkingResource::getConnectedResourceContainers_LinkingResource)
						.filter(containers -> containers.contains(container))
						.forEach(containers -> containers.add(copy));
	}
	
	protected void deleteContainers(final ResourceEnvironment environment, final int times) {
		for (int i = 0; i < times && times - i < environment.getResourceContainer_ResourceEnvironment().size(); ++i) {
			final ResourceContainer container = this.getLastFrom(environment);
			
		}
	}
	
	private ResourceContainer getLastFrom(final ResourceEnvironment environment) {
		return environment.getResourceContainer_ResourceEnvironment().remove(environment.getResourceContainer_ResourceEnvironment().size() - 1);
	}
}
