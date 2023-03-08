package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entities;

/**
 *
 * @author Julijan Katic
 *
 */
@FunctionalInterface
public interface Filter {

	public void doProcess(final Object event, final FilterChain chain);

}
