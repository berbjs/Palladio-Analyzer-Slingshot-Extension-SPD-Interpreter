package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.model.ImprovedQLearningModelEvaluator.IntervalMapping;

class ImprovedQLearningModelEvaluatorTest {

    @Test
    void testIntervalMapping() {
        IntervalMapping intervalMapping = new IntervalMapping();
        Assertions.assertEquals(0, intervalMapping.getMapping(0.5));
        intervalMapping.adjustMapping(0.2, -1);
        Assertions.assertEquals(-1, intervalMapping.getMapping(0.15));
        Assertions.assertEquals(-1, intervalMapping.getMapping(0.2));
        Assertions.assertEquals(0, intervalMapping.getMapping(Math.nextAfter(0.2, 1.0)));
        intervalMapping.adjustMapping(0.7, 1);
        Assertions.assertEquals(-1, intervalMapping.getMapping(0.19));
        Assertions.assertEquals(0, intervalMapping.getMapping(Math.nextAfter(0.7, 0.0)));
        Assertions.assertEquals(1, intervalMapping.getMapping(0.7));
        Assertions.assertEquals(1, intervalMapping.getMapping(0.71));
        intervalMapping.adjustMapping(0.65, 1);
        Assertions.assertEquals(-1, intervalMapping.getMapping(0.19));
        Assertions.assertEquals(0, intervalMapping.getMapping(Math.nextAfter(0.65, 0.0)));
        Assertions.assertEquals(1, intervalMapping.getMapping(0.69));
        Assertions.assertEquals(1, intervalMapping.getMapping(0.71));
    }
}
