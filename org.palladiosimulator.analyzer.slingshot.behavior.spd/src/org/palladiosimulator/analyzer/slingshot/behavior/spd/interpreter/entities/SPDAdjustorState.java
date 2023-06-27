package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

public final class SPDAdjustorState {

	private double latestAdjustmentAtSimulationTime = 0;
	private int numberScales = 0;

	public double getLatestAdjustmentAtSimulationTime() {
		return latestAdjustmentAtSimulationTime;
	}

	public void setLatestAdjustmentAtSimulationTime(double latestAdjustmentAtSimulationTime) {
		this.latestAdjustmentAtSimulationTime = latestAdjustmentAtSimulationTime;
	}
	
	public void incrementNumberScales() {
		this.numberScales++;
	}
	
	public int numberOfScales() {
		return this.numberScales;
	}
	
}
