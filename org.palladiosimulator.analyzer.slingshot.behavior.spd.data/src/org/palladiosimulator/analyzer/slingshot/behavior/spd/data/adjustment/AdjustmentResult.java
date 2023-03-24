package org.palladiosimulator.analyzer.slingshot.behavior.spd.data.adjustment;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class AdjustmentResult {

	private final List<ModelChange<?>> changes;
	
	public AdjustmentResult(Builder builder) {
		this.changes = Optional.ofNullable(builder.changes).orElse(Collections.emptyList());
	}
	
	public List<ModelChange<?>> getChanges() {
		return changes;
	}
	
	public static final class Builder {
		private List<ModelChange<?>> changes;
		
		public Builder change(ModelChange<?> modelChange) {
			if (changes == null) {
				this.changes = new LinkedList<>();
			}
			this.changes.add(modelChange);
			return this;
		}
		
		public AdjustmentResult build() {
			return new AdjustmentResult(this);
		}
	}
}
