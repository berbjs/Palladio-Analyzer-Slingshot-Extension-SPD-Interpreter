package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator;

public class MeanWindowAggregation extends AbstractWindowAggregation {

	public MeanWindowAggregation(int windowSize) {
		super(windowSize);
	}

	@Override
	protected double doAggregation() {
		return valuesToConsider.stream()
							   .mapToDouble(Double::doubleValue)
							   .average()
							   .orElse(0.0);
	}

}
