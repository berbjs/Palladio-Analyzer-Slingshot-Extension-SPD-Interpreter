package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old;

import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.util.ModelTransformationCache;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.util.QVToModelCache;

import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;

/**
 * QVTo executor helper class that supports executing QVTo reconfiguration rules.
 * 
 * @author Matthias Becker
 * @author Sebastian Lehrig
 * @author Florian Rosenthal
 */
public class QVToExecutor extends AbstractQVToExecutor {
	
	private static final Logger LOGGER = Logger.getLogger(QVToExecutor.class);
	
	public QVToExecutor(final ModelTransformationCache knownTransformationCache,
						final QVToModelCache knownModels) {
		super(knownTransformationCache, knownModels);
	}
	
	public boolean executeTransformations(final Iterable<QvtoModelTransformation> transformations) {
		boolean result = true;
		for (final QvtoModelTransformation transformation : transformations) {

			result &= executeTransformation(transformation);
		}
		return result;
	}
	
}
