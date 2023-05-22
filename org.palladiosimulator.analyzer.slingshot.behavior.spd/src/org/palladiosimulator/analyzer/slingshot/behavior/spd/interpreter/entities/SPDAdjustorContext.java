package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.adjustor.Adjustor;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.targetgroup.TargetGroupChecker;
import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;
import org.palladiosimulator.analyzer.slingshot.eventdriver.entity.EventHandler;
import org.palladiosimulator.analyzer.slingshot.eventdriver.entity.Subscriber;
import org.palladiosimulator.analyzer.slingshot.eventdriver.returntypes.Result;
import org.palladiosimulator.spd.ScalingPolicy;

public final class SPDAdjustorContext {

	private static final Logger LOGGER = Logger.getLogger(SPDAdjustorContext.class);
	
	private final FilterChain filterChain;
	private final ScalingPolicy scalingPolicy;
	
	private final List<Subscriber<? extends DESEvent>> associatedHandlers;

	public SPDAdjustorContext(final ScalingPolicy policy, 
			final Filter triggerChecker,
			final List<Subscriber.Builder<? extends DESEvent>> associatedHandlers) {
		this.scalingPolicy = policy;
		
		this.filterChain = new FilterChain(message -> LOGGER.info("Couldn't do it :( " + message));

		this.filterChain.add(new TargetGroupChecker(policy.getTargetGroup()));

		// TODO: Add trigger checker based on composition
		this.filterChain.add(triggerChecker);

		
		this.filterChain.add(new Adjustor(policy.getAdjustmentType(), policy.getTargetGroup(), policy));
		
		final PublishResultingEventFilter publisher = new PublishResultingEventFilter();
		//this.filterChain.add(publisher);
		
		this.associatedHandlers = associatedHandlers.stream()
				.map(builder -> builder.handler(publisher))
				.map(builder -> builder.build())
				.collect(Collectors.toList());
	}
	

	public FilterChain getFilterChain() {
		return filterChain;
	}

	public ScalingPolicy getScalingPolicy() {
		return scalingPolicy;
	}

	public List<Subscriber<? extends DESEvent>> getAssociatedHandlers() {
		return associatedHandlers;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.scalingPolicy.getId());
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof final SPDAdjustorContext otherContext) {
			return Objects.equals(this.scalingPolicy.getId(), otherContext.scalingPolicy.getId());
		}
		return false;
	}
	
	private class PublishResultingEventFilter implements EventHandler<Object> {
		
		@Override
		public Result<?> acceptEvent(Object event) throws Exception {
			filterChain.next(event);
			final FilterResult filterResult = filterChain.getLatestResult();
			
			if (filterResult instanceof final FilterResult.Success success) {
				final Object result = success.nextEvent();
				LOGGER.debug("Got a result after filtering! " + result.getClass().getSimpleName());
				final Result<?> eventResult = Result.of(result);
				//result = null; // Reinitialize for next call
				return eventResult;
			} else {
				return Result.empty();
			}
		}
		
	}
}
