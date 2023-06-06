package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.util.ModelTransformationCache;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.util.QVToModelCache;

import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.MDSDBlackboard;

public class QvtoReconfigurator {

	private final MDSDBlackboard blackboard;
	private QVToExecutor qvtoExecutor;
	private QVToModelCache modelCache;
	private ModelTransformationCache transformationCache;
	
	@Inject
	public QvtoReconfigurator(
			final MDSDBlackboard pcmPartitionManager
	) {
		this.blackboard = pcmPartitionManager;
		this.getQVToExecutor();
	}
	
	private QVToExecutor getQVToExecutor() {
		if (this.qvtoExecutor == null) {
			this.modelCache = new QVToModelCache(this.blackboard);
			this.transformationCache = new ModelTransformationCache();
			this.qvtoExecutor = new QVToExecutor(this.transformationCache, this.modelCache);
		}
		return this.qvtoExecutor;
	}
	
	public boolean execute(final Iterable<QvtoModelTransformation> actions) {
		return getQVToExecutor().executeTransformations(actions);
	}

	public QVToModelCache getModelCache() {
		return modelCache;
	}

	public void setModelCache(QVToModelCache modelCache) {
		this.modelCache = modelCache;
	}

	public ModelTransformationCache getTransformationCache() {
		return transformationCache;
	}

	public void setTransformationCache(ModelTransformationCache transformationCache) {
		this.transformationCache = transformationCache;
	}
	
	
}
