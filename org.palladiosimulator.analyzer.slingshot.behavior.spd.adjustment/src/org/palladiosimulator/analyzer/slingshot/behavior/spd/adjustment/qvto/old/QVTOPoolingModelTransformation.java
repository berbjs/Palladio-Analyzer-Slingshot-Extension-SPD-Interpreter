package org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old;

import java.util.Collection;
import java.util.function.Supplier;

import org.eclipse.m2m.internal.qvt.oml.expressions.OperationalTransformation;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.adjustment.qvto.old.util.QVToTransformationExecutorPool;

@SuppressWarnings("restriction")
public class QVTOPoolingModelTransformation extends QvtoModelTransformation {

	protected final QVToTransformationExecutorPool executorPool;
	
	public QVTOPoolingModelTransformation(
			final OperationalTransformation transformation,
			final Supplier<QVToTransformationExecutor> executorSupplier,
			final Collection<TransformationParameterInformation> paramInfo
	) {
		super(transformation, null, paramInfo);
		this.executorPool = new QVToTransformationExecutorPool(executorSupplier);
	}
	
	@Override
	public QVToTransformationExecutor getTransformationExecutor() {
		return executorPool.getExecutor();
	}
	
}
