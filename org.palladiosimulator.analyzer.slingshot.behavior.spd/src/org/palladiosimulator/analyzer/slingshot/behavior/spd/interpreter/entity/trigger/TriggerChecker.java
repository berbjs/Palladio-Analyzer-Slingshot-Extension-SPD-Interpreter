package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.trigger;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities.Filter;
import org.palladiosimulator.spd.triggers.RelationalOperator;
import org.palladiosimulator.spd.triggers.SimpleFireOnValue;
import org.palladiosimulator.spd.triggers.expectations.ExpectedValue;
import org.palladiosimulator.spd.triggers.stimuli.Stimulus;

import com.google.common.base.Preconditions;


public abstract class TriggerChecker<T extends Stimulus> implements Filter {

	protected final SimpleFireOnValue trigger;
	
	TriggerChecker(final SimpleFireOnValue trigger, final Class<T> stimulusType) {
		this.trigger = trigger;
		Preconditions.checkArgument(stimulusType.isAssignableFrom(trigger.getStimulus().getClass()), String.format("Trigger must be of type %s, but is %s", stimulusType.getSimpleName(), trigger.getStimulus().getClass().getSimpleName()));
	}
	
	@SuppressWarnings("unchecked")
	protected T getStimulus() {
		return (T) trigger.getStimulus();
	}
	
	@SuppressWarnings("unchecked")
	protected <E extends ExpectedValue> E getExpectedValueAs(Class<E> clazz) {
		if (!clazz.isAssignableFrom(this.trigger.getExpectedValue().getClass())) {
			throw new IllegalStateException(String.format("The expected value should be of type %s but is %s", clazz.getSimpleName(), this.trigger.getExpectedValue().getClass().getSimpleName()));
		}
		
		return (E) this.trigger.getExpectedValue();
	}
	
	/**
	 * Compares two values with each other according to {@link SimpleFireOnValue#getRelationalOperator()}.
	 * 
	 * @param firstValue
	 * @param secondValue
	 * @return
	 */
	protected boolean compareValues(final double firstValue, final double secondValue) {
		final RelationalOperator relationalOperator = this.trigger.getRelationalOperator();
		
		return switch (relationalOperator.getValue()) {
			case RelationalOperator.EQUAL_TO_VALUE -> firstValue == secondValue;
			case RelationalOperator.GREATER_THAN_OR_EQUAL_TO_VALUE -> firstValue >= secondValue;
			case RelationalOperator.GREATER_THAN_VALUE -> firstValue > secondValue;
			case RelationalOperator.LESS_THAN_OR_EQUAL_TO_VALUE -> firstValue <= secondValue;
			case RelationalOperator.LESS_THAN_VALUE -> firstValue < secondValue;
			default -> false;
		};
	}

}
