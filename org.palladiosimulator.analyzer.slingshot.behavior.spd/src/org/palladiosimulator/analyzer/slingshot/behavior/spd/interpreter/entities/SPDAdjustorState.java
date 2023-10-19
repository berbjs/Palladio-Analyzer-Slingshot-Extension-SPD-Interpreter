package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the state for each scaling policy definition.
 * The state include information about when the latest adjustment
 * has happened, as well as how many scaling has happened.
 * 
 * @author Julijan Katic, Floriment Klinaku
 */
public final class SPDAdjustorState {

	private double latestAdjustmentAtSimulationTime = 0;
	private int numberScales = 0;
	
	// state for evaluating cooldown constraints
	private double coolDownEnd = 0;
	private int numberOfScalesInCooldown = 0;
	
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

	public double getCoolDownEnd() {
		return coolDownEnd;
	}

	public void setCoolDownEnd(double coolDownEnd) {
		this.coolDownEnd = coolDownEnd;
	}

	public int getNumberOfScalesInCooldown() {
		return numberOfScalesInCooldown;
	}

	public void setNumberOfScalesInCooldown(int numberOfScalesInCooldown) {
		this.numberOfScalesInCooldown = numberOfScalesInCooldown;
	}
	
	public void incrementNumberOfAdjustmentsInCooldown() {
		this.numberOfScalesInCooldown++;
	}
	
}
