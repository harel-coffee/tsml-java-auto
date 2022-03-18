package tsml.classifiers.distance_based.utils.classifiers.contracting;

public interface TimedTrain {

    /**
     * The total time spent training (i.e. not including checkpointing time / time spent estimating train error)
     * @return
     */
    default long getTrainTime() {
        return getRunTime();
    }

    /**
     * The total run time of the build, including everything and anything
     * @return
     */
    long getRunTime();
}
