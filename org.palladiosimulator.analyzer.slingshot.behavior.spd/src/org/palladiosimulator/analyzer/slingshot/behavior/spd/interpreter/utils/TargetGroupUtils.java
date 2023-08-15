package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.utils;

import java.util.stream.Stream;

import org.palladiosimulator.analyzer.slingshot.core.Slingshot;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.semanticspd.Configuration;
import org.palladiosimulator.semanticspd.ServiceGroupCfg;
import org.palladiosimulator.spd.targets.ElasticInfrastructure;
import org.palladiosimulator.spd.targets.ServiceGroup;
import org.palladiosimulator.spd.targets.TargetGroup;

public class TargetGroupUtils {
	
	private static final Allocation allocation = Slingshot.getInstance().getInstance(Allocation.class);
	private static final Configuration configuration = Slingshot.getInstance().getInstance(Configuration.class);
	
	public static boolean isContainerInElasticInfrastructure(final ResourceContainer container, final ElasticInfrastructure targetGroup) {
		return targetGroup.getPCM_ResourceEnvironment()
						  .getResourceContainer_ResourceEnvironment()
						  .stream()
						  .anyMatch(rc -> rc.getId().equals(container.getId()));
	}
	
	public static boolean isContainerInServiceGroup(final ResourceContainer container, final ServiceGroup serviceGroup) {
		final Stream<AssemblyContext> contextsToConsider = getAllContextsToConsider(serviceGroup);
		
		return allocation.getAllocationContexts_Allocation()
						 .stream()
						 .filter(ac -> contextsToConsider.anyMatch(asc -> ac.getAssemblyContext_AllocationContext().getId().equals(asc.getId())))
						 .map(ac -> ac.getResourceContainer_AllocationContext())
						 .anyMatch(rc -> rc.getId().equals(container.getId()));
	}

	public static boolean isContainerInTargetGroup(final ResourceContainer container, 
												   final TargetGroup targetGroup) {
		if (targetGroup instanceof final ElasticInfrastructure elasticInfrastructure) {
			return isContainerInElasticInfrastructure(container, elasticInfrastructure);
		} else if (targetGroup instanceof final ServiceGroup serviceGroup) {
			return isContainerInServiceGroup(container, serviceGroup);
		}
		
		return false;
	}
	
	/**
	 * Helper method to retrieve the assembly contexts that need to be considered
	 * when checking whether the container is part of the target group. This includes
	 * the replicated contexts as well.
	 */
	private static Stream<AssemblyContext> getAllContextsToConsider(final ServiceGroup serviceGroup) {
		return configuration.getTargetCfgs().stream()
					.filter(ServiceGroupCfg.class::isInstance)
					.map(ServiceGroupCfg.class::cast)
					.filter(sgc -> sgc.getUnit().getId().equals(serviceGroup.getUnitAssembly().getId()))
					.flatMap(sgc -> sgc.getElements().stream());
	}
}
