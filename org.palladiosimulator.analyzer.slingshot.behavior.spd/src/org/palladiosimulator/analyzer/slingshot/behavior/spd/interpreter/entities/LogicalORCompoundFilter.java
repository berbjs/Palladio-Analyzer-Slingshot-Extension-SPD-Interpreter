package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

public class LogicalORCompoundFilter extends ComboundFilter {

	private FilterResult result = null;
	private Object eventToProcess;
	private int numberDisregarded;

	@Override
	public FilterResult doProcess(final Object event) {
		result = null;
		this.eventToProcess = event;
		this.numberDisregarded = 0;
		this.next(event);
		return result;
	}

	@Override
	public void next(final Object event) {
		/* The whole filter was successful, delegate to parent */
		//this.currentParentChain.next(event);

		/* Reinitialize */
		this.initialize();
	}

	@Override
	public void disregard(final Object message) {
		this.numberDisregarded++;
		if (this.numberDisregarded < this.size()) {
			super.next(this.eventToProcess);
		} else {
		//	this.currentParentChain.disregard(message);
			this.initialize();
		}

	}

	private void initialize() {
	//	this.currentParentChain = null;
		this.eventToProcess = null;
		this.numberDisregarded = 0;
	}

}
