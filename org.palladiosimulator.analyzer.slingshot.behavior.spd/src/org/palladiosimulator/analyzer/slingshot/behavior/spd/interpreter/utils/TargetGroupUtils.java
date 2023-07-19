package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.utils;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.spd.targets.ElasticInfrastructure;
import org.palladiosimulator.spd.targets.ServiceGroup;
import org.palladiosimulator.spd.targets.TargetGroup;

public class TargetGroupUtils {
	
	public static boolean isContainerInElasticInfrastructure(final ResourceContainer container, final ElasticInfrastructure targetGroup) {
		return targetGroup.getPCM_ResourceEnvironment()
						  .getResourceContainer_ResourceEnvironment()
						  .stream()
						  .anyMatch(rc -> rc.getId().equals(container.getId()));
	}

	public static boolean isContainerInTargetGroup(final ResourceContainer container, 
												   final TargetGroup targetGroup) {
		if (targetGroup instanceof final ElasticInfrastructure elasticInfrastructure) {
			return isContainerInElasticInfrastructure(container, elasticInfrastructure);
		} else if (targetGroup instanceof final ServiceGroup serviceGroup) {
			return true; // TODO: How do we check this?
		}
		
		return false;
	}
	
	
}
