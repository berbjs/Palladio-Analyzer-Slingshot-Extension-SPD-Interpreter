package org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

import de.uka.ipd.sdq.identifier.Identifier;


public abstract class ModelChange<T extends EObject> {

	private final T reference;
	private final ModelChange<T> previousState;
	private final String id;
	private final double simulationTime;
	private final Mode mode;
	
	public ModelChange(final T reference, 
			final ModelChange<T> previousState, 
			final String id, 
			final double simulationTime, 
			final Mode mode) {
		super();
		this.reference = reference;
		this.previousState = previousState;
		this.id = id;
		this.simulationTime = simulationTime;
		this.mode = mode;
	}
	
	public ModelChange(final T reference,
			final ModelChange<T> previousState,
			final double simulationTime,
			final Mode mode) {
		this(reference, previousState, deriveIdFromReference(reference), simulationTime, mode);
	}
	
	

	public T getReference() {
		return reference;
	}

	public ModelChange<T> getPreviousState() {
		return previousState;
	}

	public String getId() {
		return id;
	}

	public double getSimulationTime() {
		return simulationTime;
	}

	public Mode getMode() {
		return mode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, simulationTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelChange<?> other = (ModelChange<?>) obj;
		return Objects.equals(id, other.id) && simulationTime == other.simulationTime;
	}
	
	private static String deriveIdFromReference(final EObject reference) {
		if (reference instanceof final Identifier id) {
			return id.getId();
		}
		return reference.toString();
	}
	
	public static enum Mode {
		ADDITION, DELETION, LOCAL_CHANGE, UNCHANGED
	}
}
