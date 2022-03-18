package tsml.classifiers.distance_based.utils.stats.scoring;

import java.util.List;

public class ChiSquared implements SplitScorer {
    @Override public <A> double score(final Labels<A> parent, final List<Labels<A>> children) {
        final List<Double> parentDistribution = parent.getDistribution();
        double sum = 0;
        
        for(final Labels<A> labels : children) {
            labels.setLabelSet(parent.getLabelSet());
            final double childSum = labels.getWeights().stream().mapToDouble(d -> d).sum();
            final List<Double> childCounts = labels.getCountsList();
            for(int i = 0; i < parentDistribution.size(); i++) {
                final double observed = childCounts.get(i);
                final double expected = parentDistribution.get(i) * childSum;
                double v = Math.pow(observed - expected, 2);
                v /= expected;
                sum += v;
            }
        }
        
        return sum;
    }
}
