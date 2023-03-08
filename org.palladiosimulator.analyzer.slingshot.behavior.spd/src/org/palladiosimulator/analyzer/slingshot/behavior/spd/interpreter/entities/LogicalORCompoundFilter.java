package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

public class LogicalORCompoundFilter extends FilterChain implements Filter {

	private FilterChain currentParentChain;
	private Object eventToProcess;
	private int numberDisregarded;

	@Override
	public void doProcess(final Object event, final FilterChain chain) {
		this.currentParentChain = chain;
		this.eventToProcess = event;
		this.numberDisregarded = 0;
	}

	@Override
	public void next(final Object event) {
		/* The whole filter was successful, delegate to parent */
		this.currentParentChain.next(event);

		/* Reinitialize */
		this.initialize();
	}

	@Override
	public void disregard(final String message) {
		this.numberDisregarded++;
		if (this.numberDisregarded < this.size()) {
			super.next(this.eventToProcess);
		} else {
			this.currentParentChain.disregard(message);
			this.initialize();
		}

	}

	private void initialize() {
		this.currentParentChain = null;
		this.eventToProcess = null;
		this.numberDisregarded = 0;
	}

}
