package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

/**
 * This class holds the state for each scaling policy definition.
 * The state include information about when the latest adjustment
 * has happened, as well as how many scaling has happened.
 * 
 * @author Julijan Katic
 */
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
