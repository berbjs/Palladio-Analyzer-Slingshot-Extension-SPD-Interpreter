package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

import java.util.ArrayDeque;
import java.util.Queue;

import org.palladiosimulator.spd.triggers.AGGREGATIONMETHOD;

/**
 * A window-based aggregator with a window size of the last
 * {@code windowSize} measurements.
 * 
 * @author Julijan Katic
 */
public abstract class FixedLengthWindowAggregation {
	
	public static final int DEFAULT_WINDOW_SIZE = 10;

	protected final int windowSize;
	protected final Queue<Double> valuesToConsider;
	
	/** The aggregated value so far. */
	private double currentValue;
	
	public FixedLengthWindowAggregation(final int windowSize) {
		this(windowSize, new ArrayDeque<>(windowSize));
	}
	
	public FixedLengthWindowAggregation(final int windowSize, final Queue<Double> container) {
		this.windowSize = windowSize;
		this.valuesToConsider = container;
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
		valuesToConsider.offer(newValue);
	}
	
	public final double getCurrentValue() {
		return this.currentValue;
	}
	
	
	public static FixedLengthWindowAggregation getFromAggregationMethod(final AGGREGATIONMETHOD aggregationMethod, final int windowSize) {
		return switch (aggregationMethod) {
			case MIN -> new MinWindowAggregation(windowSize);
			case AVERAGE -> new MeanWindowAggregation(windowSize);
			case MAX -> new MaxWindowAggregation(windowSize);
			case MEDIAN -> new MedianWindowAggregation(windowSize);
			case SUM -> new SumWindowAggregation(windowSize);
			default -> throw new IllegalArgumentException("Unexpected value: " + aggregationMethod);
		};
	}
	
	public static FixedLengthWindowAggregation getFromAggregationMethod(final AGGREGATIONMETHOD aggregationMethod) {
		return getFromAggregationMethod(aggregationMethod, DEFAULT_WINDOW_SIZE);
	}

	/**
	 * Returns whether the window is full or whether some
	 * measurements are still missing.
	 * 
	 * @return true iff the window is full.
	 */
	public final boolean isWindowFull() {
		return this.valuesToConsider.size() == this.windowSize;
	}
	
	public final int getSize() {
		return this.valuesToConsider.size();
	}
}
