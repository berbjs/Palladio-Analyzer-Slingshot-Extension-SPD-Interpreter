package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianWindowAggregation extends AbstractWindowAggregation {

	public MedianWindowAggregation(final int windowSize) {
		super(windowSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double doAggregation() {
		final List<Double> sortedQueue = new ArrayList<>(valuesToConsider);
		Collections.sort(sortedQueue);
		return sortedQueue.get(sortedQueue.size() / 2);
	}

}
