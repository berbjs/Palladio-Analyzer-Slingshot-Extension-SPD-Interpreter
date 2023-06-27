package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class MedianWindowAggregation extends AbstractWindowAggregation {

	public MedianWindowAggregation(int windowSize) {
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
