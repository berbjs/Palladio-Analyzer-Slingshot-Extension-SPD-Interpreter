package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.utils.MedianQueue;

public class MedianWindowAggregation extends AbstractWindowAggregation {

	public MedianWindowAggregation(final int windowSize) {
		super(windowSize, new MedianQueue(windowSize));
	}

	@Override
	protected double doAggregation() {
		return valuesToConsider.peek();
	}

}
