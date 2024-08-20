package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.functions;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.DataPoint;

public class MedianAggregation implements Function<Collection<DataPoint>, Double> {

    public MedianAggregation() {
        super();
    }

    @Override
    public Double apply(Collection<DataPoint> t) {

        if (t.size() == 0) {
            return 0.0;
        }

        final Double[] values = t.stream()
            .map(DataPoint::getValue)
            .toList()
            .toArray(new Double[0]);
        Arrays.sort(values);
        final int i = values.length;

        if (i % 2 == 0) {
            return (values[i / 2] + values[i / 2 - 1]) / 2;
        } else {
            return (values[(i - 1) / 2]);
        }
    }

}
