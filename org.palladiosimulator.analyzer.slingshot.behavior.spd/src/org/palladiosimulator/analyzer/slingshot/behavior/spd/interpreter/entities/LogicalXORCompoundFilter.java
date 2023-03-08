package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

public class LogicalXORCompoundFilter extends FilterChain implements Filter {

	private FilterChain currentParentChain;
	private Object eventToProcess;
	private Object outputEvent;

	private int numberContinued;
	private int numberDisregarded;

	@Override
	public void doProcess(final Object event, final FilterChain chain) {
		this.currentParentChain = chain;
		this.eventToProcess = event;
		this.numberContinued = 0;
		this.numberDisregarded = 0;
	}

	@Override
	public void next(final Object event) {
		if (this.numberContinued == 0) {
			/* We allow one call, but we need to check that any other filter was called. */
			this.outputEvent = event;
			this.numberContinued++;
			super.next(this.eventToProcess);
		} else {
			/* We called it to often. */
			this.currentParentChain.disregard("More than one filter through XOR");
			this.initialize();
		}
	}

	@Override
	public void disregard(final String message) {
		this.numberDisregarded++;
		if (this.numberDisregarded + this.numberContinued < this.size()) {
			super.next(this.eventToProcess);
		} else if (this.numberContinued == 1) {
			this.currentParentChain.next(this.outputEvent);
		} else {
			this.currentParentChain.disregard("No filter went through. Last message: " + message);
			this.initialize();
		}

	}

	private void initialize() {
		this.currentParentChain = null;
		this.eventToProcess = null;
		this.outputEvent = null;
		this.numberContinued = 0;
		this.numberDisregarded = 0;
	}

}
