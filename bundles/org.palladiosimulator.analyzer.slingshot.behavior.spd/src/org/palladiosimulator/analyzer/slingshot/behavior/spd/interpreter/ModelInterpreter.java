package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.AnyStimulusAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ManagedElementAggregator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ImprovedQLearningModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.QThresholdsModelEvaluator;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.RandomModelEvaluator;
import org.palladiosimulator.spd.models.ImprovedQLearningModel;
import org.palladiosimulator.spd.models.LearningBasedModel;
import org.palladiosimulator.spd.models.QThresholdsModel;
import org.palladiosimulator.spd.models.RandomModel;
import org.palladiosimulator.spd.models.util.ModelsSwitch;
import org.palladiosimulator.spd.triggers.AGGREGATIONMETHOD;
import org.palladiosimulator.spd.triggers.stimuli.ManagedElementsStateStimulus;
import org.palladiosimulator.spd.triggers.stimuli.Stimulus;

public class ModelInterpreter extends ModelsSwitch<ModelEvaluator> {

    @SuppressWarnings("rawtypes")
    public ModelAggregatorWrapper getAggregatorForStimulus(Stimulus stimulus, LearningBasedModel model) {
        double windowSize = model.getInterval();
        if (stimulus instanceof ManagedElementsStateStimulus managedElementsStateStimulus) {
            return new ManagedElementAggregator<>(managedElementsStateStimulus, windowSize);
        } else {
            // TODO IMPORTANT currently using average aggregation by default for non-aggregated
            // stimuli, this might need to be changed
            return new AnyStimulusAggregator<>(stimulus, windowSize, AGGREGATIONMETHOD.AVERAGE);
        }
    }

    @Override
    public ModelEvaluator caseRandomModel(RandomModel model) {
        return new RandomModelEvaluator(model);
    }

    @Override
    public ModelEvaluator caseQThresholdsModel(QThresholdsModel model) {
        return new QThresholdsModelEvaluator(model, getAggregatorForStimulus(model.getInput(), model),
                getAggregatorForStimulus(model.getUtilizationStimulus(), model));
    }

    @Override
    public ModelEvaluator caseImprovedQLearningModel(ImprovedQLearningModel model) {
        return new ImprovedQLearningModelEvaluator(model, getAggregatorForStimulus(model.getInput(), model));
    }
}
