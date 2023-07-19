package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A window-based aggregator with a window size of the last
 * {@code windowSize} measurements.
 * 
 * @author Julijan Katic
 */
public abstract class AbstractWindowAggregation {

	protected final int windowSize;
	protected final Queue<Double> valuesToConsider;
	
	/** The aggregated value so far. */
	private double currentValue;
	
	public AbstractWindowAggregation(final int windowSize) {
		this.windowSize = windowSize;
		this.valuesToConsider = new ArrayDeque<>(windowSize);
	}
	
	/**
	 * Actually aggregates the value based on the
	 * {@link #valuesToConsider} queue.
	 * 
	 * @return Returns the new, aggregated value.
	 */
	protected abstract double doAggregation();
	
	/**
	 * Aggregates the new value into the current value.
	 * This will also update {@link #getCurrentValue()}.
	 * 
	 * @param newValue
	 * @return
	 */
	public final double aggregate(final double newValue) {
		this.consider(newValue);
		this.currentValue = this.doAggregation();
		return this.currentValue;
	}
	
	/**
	 * Adds the new value into the queue such that it
	 * contains at most {@code windowSize} values to consider.
	 * This means that if the queue is already "full",
	 * i.e. there are already {@code windowSize} element in it,
	 * the oldest element will be removed and the new value
	 * added.
	 * 
	 * @param newValue The new value to add.
	 */
	private void consider(final double newValue) {
		if (valuesToConsider.size() == windowSize) {
			valuesToConsider.poll();
		}
		valuesToConsider.add(newValue);
	}
	
	public final double getCurrentValue() {
		return this.currentValue;
	}
}
