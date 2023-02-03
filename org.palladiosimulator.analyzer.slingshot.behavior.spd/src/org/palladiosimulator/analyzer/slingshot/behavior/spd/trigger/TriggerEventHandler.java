package org.palladiosimulator.analyzer.slingshot.behavior.spd.trigger;

import java.util.function.Function;

import org.palladiosimulator.analyzer.slingshot.eventdriver.returntypes.Result;

public abstract class TriggerEventHandler<Event> implements Function<Event, Result<?>> {

	@Override
	public Result<?> apply(final Event event) {
		return Result.empty(); // TODO
	}
	
	
}
