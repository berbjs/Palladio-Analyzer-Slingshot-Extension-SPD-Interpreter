package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment;

import org.palladiosimulator.analyzer.slingshot.core.extension.AbstractSlingshotExtension;

public class SpdAdjustorModule extends AbstractSlingshotExtension {

	@Override
	protected void configure() {
		install(StepAdjustmentBehavior.class);
	}

}
