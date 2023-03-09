package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import static org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.EventCardinality.MANY;

import javax.inject.Inject;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.data.SpdBasedEvent;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.SpdInterpreter.InterpretationResult;
import org.palladiosimulator.analyzer.slingshot.core.api.SimulationDriver;
import org.palladiosimulator.analyzer.slingshot.core.events.PreSimulationConfigurationStarted;
import org.palladiosimulator.analyzer.slingshot.core.extension.SimulationBehaviorExtension;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.Subscribe;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.OnEvent;
import org.palladiosimulator.analyzer.slingshot.eventdriver.returntypes.Result;
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
@OnEvent(when = PreSimulationConfigurationStarted.class, then = SpdBasedEvent.class, cardinality = MANY)
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
	public Result<SpdBasedEvent> onPreSimulationConfigurationStarted(final PreSimulationConfigurationStarted configurationStarted) {
		final SpdInterpreter interpreter = new SpdInterpreter();
		final InterpretationResult result = interpreter.doSwitch(this.spdModel);

		result.getSubscribers().forEach(this.driver::registerEventHandler);

		return Result.from(result.getEventsToSchedule());
	}
}
