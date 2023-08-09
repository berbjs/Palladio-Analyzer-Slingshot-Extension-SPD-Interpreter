package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.utils;

import org.palladiosimulator.analyzer.slingshot.core.Slingshot;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.spd.targets.ElasticInfrastructure;
import org.palladiosimulator.spd.targets.ServiceGroup;
import org.palladiosimulator.spd.targets.TargetGroup;

public class TargetGroupUtils {
	
	private static final Allocation allocation = Slingshot.getInstance().getInstance(Allocation.class);
	
	public static boolean isContainerInElasticInfrastructure(final ResourceContainer container, final ElasticInfrastructure targetGroup) {
		return targetGroup.getPCM_ResourceEnvironment()
						  .getResourceContainer_ResourceEnvironment()
						  .stream()
						  .anyMatch(rc -> rc.getId().equals(container.getId()));
	}
	
	public static boolean isContainerInServiceGroup(final ResourceContainer container, final ServiceGroup serviceGroup) {
		return allocation.getAllocationContexts_Allocation()
						 .stream()
						 .filter(ac -> ac.getAssemblyContext_AllocationContext().getId().equals(serviceGroup.getUnitAssembly().getId()))
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
	
	
}
