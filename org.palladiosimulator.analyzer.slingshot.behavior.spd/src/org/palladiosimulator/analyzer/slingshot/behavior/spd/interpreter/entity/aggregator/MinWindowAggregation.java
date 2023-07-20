package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

public class MinWindowAggregation extends AbstractWindowAggregation {

	public MinWindowAggregation(final int windowSize) {
		super(windowSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double doAggregation() {
		return valuesToConsider.stream().min(Double::compare).orElse(0.0d);
	}

}
