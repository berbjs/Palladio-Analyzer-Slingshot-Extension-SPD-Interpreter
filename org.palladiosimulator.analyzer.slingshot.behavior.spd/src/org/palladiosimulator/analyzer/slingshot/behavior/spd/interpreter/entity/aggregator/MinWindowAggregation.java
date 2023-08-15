package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import java.util.NoSuchElementException;

public class MinWindowAggregation extends FixedLengthWindowAggregation {

	public MinWindowAggregation(final int windowSize) {
		super(windowSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double doAggregation() {
		return valuesToConsider.stream()
					.min(Double::compare)
					.orElseThrow(() -> new NoSuchElementException("There are no values to aggregate yet."));
	}

}
