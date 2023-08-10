package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import java.util.Arrays;

public class MedianWindowAggregation extends AbstractWindowAggregation {

	public MedianWindowAggregation(final int windowSize) {
		super(windowSize);
	}

	@Override
	protected double doAggregation() {
		if (valuesToConsider.size() == 0) {
			return 0;
		}
		
		final Double[] values = valuesToConsider.toArray(new Double[0]);
		Arrays.sort(values);
		final int i = values.length;
		
		if (i % 2 == 0) {
			return (values[i / 2] + values[i / 2 + 1]) / 2;
		} else {
			return (values[(i + 1) / 2]);
		}
	}

}
