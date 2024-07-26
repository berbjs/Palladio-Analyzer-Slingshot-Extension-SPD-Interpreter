package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import org.palladiosimulator.spd.triggers.RelationalOperator;
import org.palladiosimulator.spd.triggers.SimpleFireOnOutput;
import org.palladiosimulator.spd.triggers.expectations.ExpectedCount;
import org.palladiosimulator.spd.triggers.expectations.ExpectedPercentage;
import org.palladiosimulator.spd.triggers.expectations.ExpectedTime;
import org.palladiosimulator.spd.triggers.expectations.ExpectedValue;

/*
 * TODO: Compare values for simpleFireOnOutput! This class is just added
 * now for completeness, but is only a slightly modified copy of 
 * SimpleFireOnValueComparator.
 */
public class SimpleFireOnOutputComparator implements ValueComparator {

	private final SimpleFireOnOutput trigger;
	
	public SimpleFireOnOutputComparator(final SimpleFireOnOutput trigger) {
		this.trigger = trigger;
	}

	@Override
	public ComparatorResult compare(final double actualValue, final ExpectedValue expectedValue) {
		final RelationalOperator relationalOperator = RelationalOperator.EQUAL_TO;
		final double expectedValueDouble = getValue(expectedValue);
		
		final boolean result = switch (relationalOperator.getValue()) {
			case RelationalOperator.EQUAL_TO_VALUE -> actualValue == expectedValueDouble;
			case RelationalOperator.GREATER_THAN_OR_EQUAL_TO_VALUE -> actualValue >= expectedValueDouble;
			case RelationalOperator.GREATER_THAN_VALUE -> actualValue > expectedValueDouble;
			case RelationalOperator.LESS_THAN_OR_EQUAL_TO_VALUE -> actualValue <= expectedValueDouble;
			case RelationalOperator.LESS_THAN_VALUE -> actualValue < expectedValueDouble;
			default -> false;
		};
		
		if (result) {
			return ComparatorResult.IN_ACCORDANCE;
		} else {
			return ComparatorResult.DISREGARD;
		}
	}
	
	private double getValue(final ExpectedValue primitive) {
		if (primitive instanceof ExpectedTime) {
			return ((ExpectedTime) primitive).getValue();
		} else if (primitive instanceof ExpectedCount) {
			return ((ExpectedCount) primitive).getCount();
		} else if (primitive instanceof ExpectedPercentage) {
			// Here, we assume that the value is a percentage given as a number between 0 and 100.
			return ((ExpectedPercentage) primitive).getValue() / 100;
		}
		
		throw new IllegalStateException("The proivded ExpectedPrimitive is not defined: " + primitive.getClass().getSimpleName());
	}
}
