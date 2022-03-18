package tsml.classifiers.distance_based.distances.dtw.spaces;

import tsml.classifiers.distance_based.utils.collections.params.ParamMap;
import tsml.classifiers.distance_based.utils.collections.params.ParamSpace;
import tsml.classifiers.distance_based.utils.collections.params.ParamSpaceBuilder;
import tsml.data_containers.TimeSeriesInstances;

import java.util.stream.IntStream;

import static tsml.classifiers.distance_based.distances.dtw.DTW.WINDOW_FLAG;

public class DTWDistanceParams implements ParamSpaceBuilder {
    @Override public ParamSpace build(final TimeSeriesInstances data) {
        return new ParamSpace(new ParamMap()
                       .add(WINDOW_FLAG, IntStream.range(0, 100).mapToDouble(i -> (double) i / 100d).toArray()));
    }
}
