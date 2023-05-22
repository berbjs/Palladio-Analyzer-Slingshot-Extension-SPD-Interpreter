package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.util.ModelTransformationCache;
import org.palladiosimulator.commons.eclipseutils.FileHelper;

/**
 * 
 * @author Julijan Katic
 */
public class QVToLoader {
	
	private static final Logger LOGGER = Logger.getLogger(QVToLoader.class);
	
	public static Iterable<QvtoModelTransformation> loadFromFiles(String... files) {
		final URI[] uris = new URI[files.length];
		for (int i = 0; i < files.length; ++i) {
			uris[i] = getUriFromFile(files[i]);
			LOGGER.debug(String.format("Uri is: %s", uris[i].toFileString()));
		}
		return new ModelTransformationCache(uris).getAll();
	}
	
	private static URI getUriFromFile(final String path) {
		final URI uri = URI.createURI(path);
		return uri;
	}
	
}
