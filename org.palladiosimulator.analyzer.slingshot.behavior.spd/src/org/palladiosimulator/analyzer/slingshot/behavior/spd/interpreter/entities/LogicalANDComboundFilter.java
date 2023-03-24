package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

public class LogicalANDComboundFilter extends ComboundFilter {

	private FilterResult result;

	@Override
	public FilterResult doProcess(final Object event) {
		result = null;
		this.next(event);
		return result;
	}

	@Override
	public void next(final Object event) {
		super.next(event);
		if (!this.filterIsBeingUsed()) {
			/* This means that no filter was used. */
			result = FilterResult.success(event);
		}
	}

	@Override
	public void disregard(final Object message) {
		result = FilterResult.disregard(message);
	}

}
