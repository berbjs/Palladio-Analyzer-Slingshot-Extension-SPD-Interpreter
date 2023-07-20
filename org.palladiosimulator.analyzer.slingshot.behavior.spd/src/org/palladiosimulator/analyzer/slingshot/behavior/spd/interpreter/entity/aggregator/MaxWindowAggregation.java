package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

public class MaxWindowAggregation extends AbstractWindowAggregation {

	public MaxWindowAggregation(final int windowSize) {
		super(windowSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double doAggregation() {
		return valuesToConsider.stream().max(Double::compare).orElse(0.0d);
	}

}
