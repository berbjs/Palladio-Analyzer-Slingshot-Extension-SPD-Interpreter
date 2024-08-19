package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.functions;

import java.util.Collection;
import java.util.function.Function;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class RateOfChangeAggregation implements Function<Collection<Double>, Double> {

    public RateOfChangeAggregation() {
        super();
    }

    @Override
    public Double apply(Collection<Double> t) {

        if (t.size() == 0) {
            return 0.0;
        }

        final Double[] values = t.toArray(new Double[0]);
        SimpleRegression regression = new SimpleRegression();
        return 0.0;
    }
}
