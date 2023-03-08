package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import javax.inject.Inject;

import org.palladiosimulator.analyzer.slingshot.core.api.SimulationDriver;
import org.palladiosimulator.analyzer.slingshot.core.api.SimulationScheduling;
import org.palladiosimulator.analyzer.slingshot.core.events.PreSimulationConfigurationStarted;
import org.palladiosimulator.analyzer.slingshot.core.extension.SimulationBehaviorExtension;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.Subscribe;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.OnEvent;
import org.palladiosimulator.spd.SPD;

/**
 * The behavior where the interpretation of SPD starts. The interpreter might
 * return new events that could be of the following kind:
 * 
 * <ul>
 * <li> Events that are directly scheduled at a certain time, such as {@link SimulationTimeReached}
 * </ul>
 * 
 * @author Julijan Katic
 */
@OnEvent(when = PreSimulationConfigurationStarted.class)
public class SpdBehavior implements SimulationBehaviorExtension {

	private final SimulationDriver driver;
	private final SPD spdModel;
	
	@Inject
	public SpdBehavior(
			final SimulationDriver driver,
			final SPD spdModel) {
		this.spdModel = spdModel;
		this.driver = driver;
	}
	
	@Subscribe
	public void onPreSimulationConfigurationStarted(final PreSimulationConfigurationStarted configurationStarted) {
		final SpdInterpreter interpreter = new SpdInterpreter(driver);
		interpreter.doSwitch(spdModel);
	}
}
