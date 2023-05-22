package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment;

import javax.inject.Named;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.QVToLoader;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.QvtoModelTransformation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.QvtoReconfigurator;
import org.palladiosimulator.analyzer.slingshot.core.extension.AbstractSlingshotExtension;

import com.google.inject.Provides;

public class SpdAdjustorModule extends AbstractSlingshotExtension {

	private static final String MAIN_QVTO_FILE = "platform:/plugin/org.palladiosimulator.spd.semantic.transformations/transformations/spd/MainTransformation.qvto";
	public static final String MAIN_QVTO = "mainqvto";
	
	@Override
	protected void configure() {
		install(StepAdjustmentBehavior.class);

		bind(QvtoReconfigurator.class);
	}

	@Provides
	@Named(MAIN_QVTO)
	public String mainQvtoFile() {
		return MAIN_QVTO_FILE;
	}
	
	@Provides
	@Named(MAIN_QVTO)
	public Iterable<QvtoModelTransformation> getTransformations() {
		return QVToLoader.loadFromFiles(MAIN_QVTO_FILE);
	}
	
}
