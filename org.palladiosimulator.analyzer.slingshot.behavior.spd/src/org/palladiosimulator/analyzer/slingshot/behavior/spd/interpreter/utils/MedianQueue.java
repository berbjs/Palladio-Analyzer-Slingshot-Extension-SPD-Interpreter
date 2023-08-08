package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.utils;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * This class implements a heap-based approach of finding a median in a (bounded)
 * queue. This queue has a maximum size, and only the last {@code n} values (according
 * to the insertion order) will be regarded.
 * 
 * Here, the {@link #peek()} returns the current median, but {@link #poll()} returns
 * the oldest element.
 * 
 * @author Julijan Katic
 */
public class MedianQueue extends AbstractQueue<Double> {
	
	/** Heap containing the lower half of the numbers with the maximum on the top. */
	private final Queue<Double> maxHeap;
	
	/** Heap containing the higher half of the numbers with the minimum on the top. */
	private final Queue<Double> minHeap;
	
	/** All the numbers added sofar in insertion order. */
	private final Deque<Double> insertionOrderQueue;
	
	private final int window;
	
	public MedianQueue(final int windowSize) {
		this.minHeap = new PriorityQueue<>();
		this.maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
		this.insertionOrderQueue = new ArrayDeque<>(windowSize);
		this.window = windowSize;
	}

	@Override
	public boolean offer(Double e) {
		final Double removedNumber = this.addNumber(e);
		this.removeNumber(removedNumber);
		
		if (minHeap.size() == maxHeap.size()) {
			return maxHeap.offer(e) && minHeap.offer(maxHeap.poll());
		} else {
			return minHeap.offer(e) && maxHeap.offer(maxHeap.poll());
		}
	}

	@Override
	public Double poll() {
		if (this.size() == 0) {
			throw new NoSuchElementException();
		}
		
		final Double number = this.insertionOrderQueue.poll();
		this.removeNumber(number);
		return number;
	}
	
	/**
	 * Removes the number from the queue and one of the heaps,
	 * if {@code number} is not {@code null}
	 * 
	 * @param number The number to remove if not {@code null}.
	 */
	private void removeNumber(Double number) {
		if (number != null) {
			if (number >= this.minHeap.peek()) {
				this.minHeap.remove(number);
			} else if (number <= this.maxHeap.peek()) {
				this.maxHeap.remove(number);
			}
		}
	}
	
	/**
	 * Adds a new number. If the size already reached the window size,
	 * then the oldest number is removed and returned here.
	 * 
	 * @param value Number to add to the insertion order queue.
	 * @return The oldest number if the queue was already full, otherwise
	 * 		   {@code null}.
	 */
	private Double addNumber(Double value) {
		if (this.insertionOrderQueue.size() < window) {
			this.insertionOrderQueue.offer(value);
			return null;
		} else {
			final Double d = this.insertionOrderQueue.poll();
			this.insertionOrderQueue.offer(value);
			return d;
		}
	}

	/**
	 * Returns the median of this queue.
	 * @return The median.
	 */
	@Override
	public Double peek() {
		return this.getMedian();
	}
	
	public Double getMedian() {
		if (this.size() % 2 == 0) {
			return (this.minHeap.peek() + this.maxHeap.peek()) / 2;
		} else {
			return (this.minHeap.peek());
		}
	}

	@Override
	public Iterator<Double> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return this.minHeap.size() + this.maxHeap.size();
	}
}
