package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

public sealed interface FilterResult {
	
	public static Success success(final Object nextEvent) {
		return new Success(nextEvent);
	}
	
	public static Disregard disregard(final Object reason) {
		return new Disregard(reason);
	}

	public static record Success(Object nextEvent) implements FilterResult {}
	
	public static record Disregard(Object reason) implements FilterResult {}
	

}
