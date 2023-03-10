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
	
	private final FilterChain filterChain = new FilterChain();
	private final ScalingPolicy scalingPolicy;
	
	private final List<Subscriber<? extends DESEvent>> associatedHandlers;

	public SPDAdjustorContext(final ScalingPolicy policy, 
			final Filter triggerChecker,
			final List<Subscriber.Builder<? extends DESEvent>> associatedHandlers) {
		this.scalingPolicy = policy;

		this.filterChain.add(new TargetGroupChecker(policy.getTargetGroup()));

		// TODO: Add trigger checker based on composition
		this.filterChain.add(triggerChecker);

		
		this.filterChain.add(new Adjustor(policy.getAdjustmentType(), policy.getTargetGroup()));
		
		final PublishResultingEventFilter publisher = new PublishResultingEventFilter();
		this.filterChain.add(publisher);
		
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
	
	private class PublishResultingEventFilter implements Filter, EventHandler<Object> {
		
		private Object result;

		@Override
		public void doProcess(Object event, FilterChain chain) {
			this.result = event;
		}

		@Override
		public Result<?> acceptEvent(Object event) throws Exception {
			filterChain.next(event);
			if (this.result == null) {
				throw new IllegalStateException("After the next() call, the result shouldn´t be null..");
			}
			LOGGER.debug("Got a result after filtering! " + this.result.getClass().getSimpleName());
			final Result<?> result = Result.of(this.result);
			this.result = null; // Reinitialize for next call
			return result;
		}
		
	}
}
