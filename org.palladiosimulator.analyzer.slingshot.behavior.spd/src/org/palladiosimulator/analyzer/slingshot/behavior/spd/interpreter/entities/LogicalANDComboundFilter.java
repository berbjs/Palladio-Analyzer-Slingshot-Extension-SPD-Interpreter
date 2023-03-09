package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

public class LogicalANDComboundFilter extends ComboundFilter {

	private FilterChain parentChain;

	@Override
	public void doProcess(final Object event, final FilterChain chain) {
		this.parentChain = chain;
		this.next(event);
	}

	@Override
	public void next(final Object event) {
		super.next(event);
		if (!this.filterIsBeingUsed()) {
			/* This means that no filter was used. */
			this.parentChain.next(event);
		}
	}

	@Override
	public void disregard(final String message) {
		this.parentChain.disregard(message);
	}


}
