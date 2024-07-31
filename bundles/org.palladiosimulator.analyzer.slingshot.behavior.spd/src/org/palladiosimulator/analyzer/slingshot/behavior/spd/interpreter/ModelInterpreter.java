package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models.RandomModelEvaluator;
import org.palladiosimulator.spd.adjustments.models.QThresholdsModel;
import org.palladiosimulator.spd.adjustments.models.RandomModel;
import org.palladiosimulator.spd.adjustments.models.util.ModelsSwitch;

public class ModelInterpreter extends ModelsSwitch<ModelEvaluator> {

    @Override
    public ModelEvaluator caseRandomModel(RandomModel object) {
        return new RandomModelEvaluator();
    }

    @Override
    public ModelEvaluator caseQThresholdsModel(QThresholdsModel object) {
        // TODO Auto-generated method stub
        // TODO IMPORTANT return a model
        return QThresholdsModelEvaluator(object);
    }

}
