package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

public class SumWindowAggregation extends AbstractWindowAggregation {

	public SumWindowAggregation(final int windowSize) {
		super(windowSize);
	}

	@Override
	protected double doAggregation() {
		return valuesToConsider.stream().reduce(0.0d, Double::sum);
	}
	
}
