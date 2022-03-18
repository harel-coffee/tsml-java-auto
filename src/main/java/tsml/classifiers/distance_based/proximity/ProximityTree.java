/*
 * This file is part of the UEA Time Series Machine Learning (TSML) toolbox.
 *
 * The UEA TSML toolbox is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * The UEA TSML toolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the UEA TSML toolbox. If not, see <https://www.gnu.org/licenses/>.
 */
 
package tsml.classifiers.distance_based.proximity;

import com.google.common.collect.Lists;
import experiments.data.DatasetLoading;
import org.junit.Assert;
import tsml.classifiers.distance_based.distances.DistanceMeasure;
import tsml.classifiers.distance_based.distances.IndependentDistanceMeasure;
import tsml.classifiers.distance_based.distances.dtw.spaces.*;
import tsml.classifiers.distance_based.distances.ed.spaces.EDistanceSpace;
import tsml.classifiers.distance_based.distances.erp.spaces.ERPDistanceRestrictedContinuousSpace;
import tsml.classifiers.distance_based.distances.lcss.spaces.LCSSDistanceRestrictedContinuousSpace;
import tsml.classifiers.distance_based.distances.msm.spaces.MSMDistanceSpace;
import tsml.classifiers.distance_based.distances.transformed.TransformDistanceMeasure;
import tsml.classifiers.distance_based.distances.twed.spaces.TWEDistanceSpace;
import tsml.classifiers.distance_based.distances.wdtw.spaces.WDDTWDistanceContinuousSpace;
import tsml.classifiers.distance_based.distances.wdtw.spaces.WDTWDistanceContinuousSpace;
import tsml.classifiers.distance_based.utils.classifiers.configs.Configs;
import tsml.classifiers.distance_based.utils.collections.pruned.PrunedMap;
import tsml.classifiers.distance_based.utils.classifiers.*;
import tsml.classifiers.distance_based.utils.classifiers.checkpointing.CheckpointConfig;
import tsml.classifiers.distance_based.utils.classifiers.checkpointing.Checkpointed;
import tsml.classifiers.distance_based.utils.collections.lists.IndexList;
import tsml.classifiers.distance_based.utils.collections.params.ParamSet;
import tsml.classifiers.distance_based.utils.collections.params.ParamSpace;
import tsml.classifiers.distance_based.utils.collections.params.ParamSpaceBuilder;
import tsml.classifiers.distance_based.utils.collections.params.iteration.RandomSearch;
import tsml.classifiers.distance_based.utils.collections.tree.BaseTree;
import tsml.classifiers.distance_based.utils.collections.tree.BaseTreeNode;
import tsml.classifiers.distance_based.utils.collections.tree.Tree;
import tsml.classifiers.distance_based.utils.collections.tree.TreeNode;
import tsml.classifiers.distance_based.utils.classifiers.contracting.ContractedTest;
import tsml.classifiers.distance_based.utils.classifiers.contracting.ContractedTrain;
import tsml.classifiers.distance_based.utils.classifiers.results.ResultUtils;
import tsml.classifiers.distance_based.utils.stats.scoring.*;
import tsml.classifiers.distance_based.utils.strings.StrUtils;
import tsml.classifiers.distance_based.utils.system.logging.LogUtils;
import tsml.classifiers.distance_based.utils.system.memory.MemoryWatchable;
import tsml.classifiers.distance_based.utils.system.memory.MemoryWatcher;
import tsml.classifiers.distance_based.utils.system.random.RandomUtils;
import tsml.classifiers.distance_based.utils.system.timing.StopWatch;
import tsml.data_containers.TimeSeriesInstance;
import tsml.data_containers.TimeSeriesInstances;
import tsml.transformers.CachedTransformer;
import tsml.transformers.Derivative;
import tsml.transformers.TransformPipeline;
import tsml.transformers.Transformer;
import utilities.ArrayUtilities;
import utilities.ClassifierTools;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static tsml.classifiers.distance_based.utils.collections.checks.Checks.requireReal;

/**
 * Proximity tree
 * <p>
 * Contributors: goastler
 */
public class ProximityTree extends BaseClassifier implements ContractedTest, ContractedTrain, Checkpointed, MemoryWatchable {

    public static void main(String[] args) throws Exception {
//        System.out.println(CONFIGS);
        for(int i = 0; i < 1; i++) {
            int seed = i;
            ProximityTree classifier = CONFIGS.get("PT_R5").build();
            classifier.setSeed(seed);
//            classifier.setCheckpointDirPath("checkpoints");
            classifier.setLogLevel(Level.ALL);
//            classifier.setDebug(true);
//            classifier.setDistanceMode(DistanceMode.DEPENDENT);
//            classifier.setDimensionConversion(DimensionConversionMode.NONE);
//            classifier.setDimensionSamplingMode(DimensionSamplingMode.ALL);
//            classifier.setMultivariateMode(DimensionSamplingMode.CONCAT_TO_UNIVARIATE);
//            classifier.setEarlyAbandonDistances(true);
//            classifier.setEarlyExemplarCheck(true);
//            classifier.setPartitionExaminationReordering(true);
            //            classifier.setTrainTimeLimit(10, TimeUnit.SECONDS);
//            classifier.setCheckpointPath("checkpoints");
//            classifier.setCheckpointInterval(10, TimeUnit.SECONDS);
//            classifier.setTrainTimeLimit(5, TimeUnit.SECONDS);
            ClassifierTools.trainTestPrint(classifier, DatasetLoading.sampleItalyPowerDemand(seed), seed);
        }
    }
    
    public final static Configs<ProximityTree> CONFIGS = buildConfigs().immutable();
    
    public static Configs<ProximityTree> buildConfigs() {
        final Configs<ProximityTree> configs = new Configs<>();
        configs.add("PT_R1", "Proximity tree with a single split per node", ProximityTree::new,
               pt -> {
                        pt.setDistanceMeasureSpaceBuilders(Lists.newArrayList(
                                new EDistanceSpace(),
                                new DTWDistanceFullWindowSpace(),
                                new DTWDistanceRestrictedContinuousSpace(),
                                new DDTWDistanceFullWindowSpace(),
                                new DDTWDistanceRestrictedContinuousSpace(),
                                new WDTWDistanceContinuousSpace(),
                                new WDDTWDistanceContinuousSpace(),
                                new LCSSDistanceRestrictedContinuousSpace(),
                                new ERPDistanceRestrictedContinuousSpace(),
                                new TWEDistanceSpace(),
                                new MSMDistanceSpace()
                        ));
                        pt.setSplitScorer(new GiniEntropy());
                        pt.setR(1);
                        pt.setTrainTimeLimit(-1);
                        pt.setTestTimeLimit(-1);
                        pt.setBreadthFirst(false);
                        pt.setPartitionExaminationReordering(false);
                        pt.setEarlyExemplarCheck(false);
                        pt.setEarlyAbandonDistances(false);
                        pt.setDimensionConversion(DimensionConversionMode.NONE);
                        pt.setDistanceMode(DistanceMode.DEPENDENT);
                        pt.setDimensionSamplingMode(DimensionSamplingMode.SINGLE);
                        pt.setCacheTransforms(false);
                });
        
        configs.add("PT_R5", "5 random splits per node", "PT_R1", pt -> pt.setR(5));
        
        configs.add("PT_R10", "10 random splits per node", "PT_R1", pt -> pt.setR(10));
        
        for(DimensionSamplingMode samplingMode : DimensionSamplingMode.values()) {
            for(DimensionConversionMode conversionMode : DimensionConversionMode.values()) {
                for(DistanceMode distanceMode : DistanceMode.values()) {
                    String base = "PT_R5";
                    String name = base
                                          + "_" + (samplingMode.equals(DimensionSamplingMode.SINGLE) ? '1' :
                                          samplingMode.name().charAt(0)) 
                                          + "_" + StrUtils.join("", Arrays.stream(conversionMode.name().split("_")).map(s -> s.substring(0, 1)).toArray(String[]::new))
                                          + "_" + distanceMode.name().charAt(0);
                    configs.add(name, "", "PT_R5", pt -> {
                        pt.setDimensionSamplingMode(samplingMode);
                        pt.setDimensionConversion(conversionMode);
                        pt.setDistanceMode(distanceMode);
                    });
                }
            }
        }
        
        return configs;
    }

    public ProximityTree() {
        CONFIGS.get("PT_R1").configure(this);
    }

    private static final long serialVersionUID = 1;
    // train timer
    private final StopWatch runTimer = new StopWatch();
    // test / predict timer
    private final StopWatch testTimer = new StopWatch();
    // method of tracking memory
    private final MemoryWatcher memoryWatcher = new MemoryWatcher();
    // the tree of splits
    private Tree<Split> tree;
    // the train time limit / contract
    private long trainTimeLimit;
    // the test time limit / contract
    private long testTimeLimit;
    // the longest time taken to build a node / split
    private long longestTrainStageTime;
    // the queue of nodes left to build
    private Deque<TreeNode<Split>> nodeBuildQueue;
    // the list of distance function space builders to produce distance functions in splits
    private List<ParamSpaceBuilder> distanceMeasureSpaceBuilders;
    // the number of splits to consider for this split
    private int r;
    // a method of scoring the split of data into partitions
    private SplitScorer splitScorer;
    // checkpoint config
    private final CheckpointConfig checkpointConfig = new CheckpointConfig();
    // whether to build the tree depth first or breadth first
    private boolean breadthFirst = false;
    // whether to use early abandon in the distance computations
    private boolean earlyAbandonDistances;
    // whether to use a quick check for exemplars
    private boolean earlyExemplarCheck;
    // enhanced early abandon distance computation via ordering partition examination to hit the most likely closest exemplar sooner
    private boolean partitionExaminationReordering;
    // cache certain transformers to avoid repetition
    private Map<Transformer, CachedTransformer> transformerCache;

    public DistanceMode getDistanceMode() {
        return distanceMode;
    }

    public boolean withinTrainContract(long time) {
        return insideTrainTimeLimit(time);
    }
    
    public void setDistanceMode(
            final DistanceMode distanceMode) {
        this.distanceMode = Objects.requireNonNull(distanceMode);
    }

    public DimensionConversionMode getDimensionConversion() {
        return dimensionConversionMode;
    }

    public void setDimensionConversion(
            final DimensionConversionMode dimensionConversionMode) {
        this.dimensionConversionMode = Objects.requireNonNull(dimensionConversionMode);
    }

    // what strategy to use for handling multivariate data
    private DimensionSamplingMode dimensionSamplingMode;
    // multivariate conversion mode to convert multivariate data into an alternate form
    private DimensionConversionMode dimensionConversionMode;
    // multivariate distance can be interpreted as several isolated univariate pairings or delegated to the distance measure to manage
    private DistanceMode distanceMode;
    
    public DimensionSamplingMode getDimensionSamplingMode() {
        return dimensionSamplingMode;
    }

    public void setDimensionSamplingMode(
            final DimensionSamplingMode dimensionSamplingMode) {
        this.dimensionSamplingMode = Objects.requireNonNull(dimensionSamplingMode);
    }

    public CheckpointConfig getCheckpointConfig() {
        return checkpointConfig;
    }

    public boolean isCacheTransforms() {
        return transformerCache != null;
    }

    public void setCacheTransforms(final boolean cacheTransforms) {
        if(cacheTransforms) {
            transformerCache = new HashMap<>();
        } else {
            transformerCache = null;
        }
    }

    /**
     * Set the cache to an external cache
     * @param cache
     */
    public void setCacheTransforms(final Map<Transformer, CachedTransformer> cache) {
        transformerCache = cache;
    }

    public enum DimensionSamplingMode {
        SINGLE, // randomly pick a single dimension, discarding others
        SUBSET, // randomly pick a subset of dimensions (between 1 and all dimensions) and discard others
        ALL, // retain all dimensions
        ;
    }
    
    public enum DimensionConversionMode {
        NONE, // don't convert dimensions whatsoever
        CONCAT, // concatenate dimensions into a single, long univariate time series
        STRATIFY, // stratify dimensions into a single, long univariate time series
        SHUFFLE_CONCAT, // shuffle, then concat
        SHUFFLE_STRATIFY, // shuffle, then stratify
        RANDOM, // random pick between the above conversions
        ;
    }
    
    public enum DistanceMode {
        DEPENDENT, // let the distance measure consider all dimensions to compute distance
        INDEPENDENT, // independently compute distance on each dimension, then sum for final distance
        RANDOM, // randomly choose independent or dependent
        ;
    }

    @Override public boolean isFullyBuilt() {
        return nodeBuildQueue != null && nodeBuildQueue.isEmpty() && tree != null && tree.getRoot() != null;
    }

    public boolean isBreadthFirst() {
        return breadthFirst;
    }

    public void setBreadthFirst(final boolean breadthFirst) {
        this.breadthFirst = breadthFirst;
    }

    public List<ParamSpaceBuilder> getDistanceMeasureSpaceBuilders() {
        return distanceMeasureSpaceBuilders;
    }

    public void setDistanceMeasureSpaceBuilders(final List<ParamSpaceBuilder> distanceMeasureSpaceBuilders) {
        this.distanceMeasureSpaceBuilders = Objects.requireNonNull(distanceMeasureSpaceBuilders);
        Assert.assertFalse(distanceMeasureSpaceBuilders.isEmpty());
    }

    @Override public long getTrainTime() {
        return getRunTime() - getCheckpointingTime();
    }

    @Override public long getRunTime() {
        return runTimer.elapsedTime();
    }

    @Override public long getTestTime() {
        return testTimer.elapsedTime();
    }

    @Override
    public long getTestTimeLimit() {
        return testTimeLimit;
    }

    @Override
    public void setTestTimeLimit(final long nanos) {
        testTimeLimit = nanos;
    }

    @Override
    public long getTrainTimeLimit() {
        return trainTimeLimit;
    }

    @Override
    public void setTrainTimeLimit(long nanos) {
        trainTimeLimit = nanos;
    }

    @Override
    public void buildClassifier(TimeSeriesInstances trainData) throws Exception {
        // timings:
            // train time tracks the time spent processing the algorithm. This should not be used for contracting.
            // run time tracks the entire time spent processing, whether this is work towards the algorithm or otherwise (e.g. saving checkpoints to disk). This should be used for contracting.
            // evaluation time tracks the time spent evaluating the quality of the classifier, i.e. producing an estimate of the train data error.
            // checkpoint time tracks the time spent loading / saving the classifier to disk.
        // record the start time
        final long timeStamp = System.nanoTime();
        memoryWatcher.start();
        checkpointConfig.setLogger(getLogger());
        // several scenarios for entering this method:
            // 1) from scratch: isRebuild() is true
                // 1a) checkpoint found and loaded, resume from wherever left off
                // 1b) checkpoint not found, therefore initialise classifier and build from scratch
            // 2) rebuild off, i.e. buildClassifier has been called before and already handled 1a or 1b. We can safely continue building from current state. This is often the case if a smaller contract has been executed (e.g. 1h), then the contract is extended (e.g. to 2h) and we enter into this method again. There is no need to reinitialise / discard current progress - simply continue on under new constraints.
        if(isRebuild()) {
            // case (1)
            // load from a checkpoint
            if(loadCheckpoint()) {
                memoryWatcher.start();
                checkpointConfig.setLogger(getLogger());
            } else {
                // case (1b)
                memoryWatcher.reset();
                // let super build anything necessary (will handle isRebuild accordingly in super class)
                super.buildClassifier(trainData);
                // if rebuilding
                // then init vars
                // build timer is already started so just clear any time already accrued from previous builds. I.e. keep the time stamp of when the timer was started, but clear any record of accumulated time
                runTimer.reset();
                checkpointConfig.resetCheckpointingTime();
                // setup the tree vars
                tree = new BaseTree<>();
                nodeBuildQueue = new LinkedList<>();
                longestTrainStageTime = 0;
                if(isCacheTransforms()) {
                    // clear out any old cached versions
                    transformerCache = new HashMap<>();
                }
                // setup the root node
                final TreeNode<Split> root = new BaseTreeNode<>(new Split(trainData, new IndexList(trainData.numInstances())), null);
                // add the root node to the tree
                tree.setRoot(root);
                // add the root node to the build queue
                nodeBuildQueue.add(root);
            }  // else case (1a)

        } // else case (2)
        
        // update the run timer with the start time of this session 
        // as the runtimer has been overwritten with the one from the checkpoint (if loaded)
        // or the classifier has been initialised from scratch / resumed and can just start from the timestamp
        runTimer.start(timeStamp);
        
        LogUtils.logTimeContract(runTimer.elapsedTime(), trainTimeLimit, getLogger(), "train");
        boolean workDone = false;
        // maintain a timer for how long nodes take to build
        final StopWatch trainStageTimer = new StopWatch();
        while(
                // there's remaining nodes to be built
                !nodeBuildQueue.isEmpty()
                &&
                // there is enough time for another split to be built
                insideTrainTimeLimit( runTimer.elapsedTime() + longestTrainStageTime)
        ) {
            // time how long it takes to build the node
            trainStageTimer.resetAndStart();
            // get the next node to be built
            final TreeNode<Split> node = nodeBuildQueue.removeFirst();
            // partition the data at the node
            Split split = node.getValue();
            // find the best of R partitioning attempts
            split = buildSplit(split);
            node.setValue(split);
            // for each partition of data build a child node
            final List<TreeNode<Split>> children = buildChildNodes(node);
            // add the child nodes to the build queue
            enqueueNodes(children);
            // done building this node
            trainStageTimer.stop();
            workDone = true;
            // checkpoint if necessary
            saveCheckpoint();
            // update the train timer
            LogUtils.logTimeContract(runTimer.elapsedTime(), trainTimeLimit, getLogger(), "train");
            // calculate the longest time taken to build a node given
            longestTrainStageTime = Math.max(longestTrainStageTime, trainStageTimer.elapsedTime());
        }
        // stop resource monitoring
        memoryWatcher.stop();
        runTimer.stop();
        // save the final checkpoint / info
        if(workDone) {
            ResultUtils.setInfo(trainResults, this, trainData);
            forceSaveCheckpoint();
        }
    }

    public Tree<Split> getTree() {
        return tree;
    }

    @Override public long getMaxMemoryUsage() {
        return memoryWatcher.getMaxMemoryUsage();
    }

    /**
     * setup the child nodes given the parent node
     *
     * @param parent
     * @return
     */
    private List<TreeNode<Split>> buildChildNodes(TreeNode<Split> parent) {
        final Split split = parent.getValue();
        List<TreeNode<Split>> children = new ArrayList<>(split.numPartitions());
        for(int i = 0; i < split.numPartitions(); i++) {
            final TimeSeriesInstances data = split.getPartitionData(i);
            final List<Integer> dataIndicesInTrainData = split.getPartitionDataIndicesInTrainData(i);
            final Split child = new Split(data, dataIndicesInTrainData);
            children.add(new BaseTreeNode<>(child, parent));
        }

        return children;
    }

    /**
     * add nodes to the build queue if they fail the stopping criteria
     *
     * @param nodes
     */
    private void enqueueNodes(List<TreeNode<Split>> nodes) {
        // for each node
        for(int i = 0; i < nodes.size(); i++) {
            TreeNode<Split> node;
            if(breadthFirst) {
                // get the ith node if breath first
                node = nodes.get(i);
            } else {
                // get the nodes in reverse order if depth first (as we add to the front of the build queue, so need
                // to lookup nodes in reverse order here)
                node = nodes.get(nodes.size() - i - 1);
            }
            // check the data at the node is not pure
            final List<Integer> uniqueClassLabelIndices =
                    node.getValue().getData().stream().map(TimeSeriesInstance::getLabelIndex).distinct()
                            .collect(Collectors.toList());
            if(uniqueClassLabelIndices.size() > 1) {
                // if not hit the stopping condition then add node to the build queue
                if(breadthFirst) {
                    nodeBuildQueue.addLast(node);
                } else {
                    nodeBuildQueue.addFirst(node);
                }
            }
        }
    }

    @Override
    public double[] distributionForInstance(final TimeSeriesInstance instance) throws Exception {
        // enable resource monitors
        testTimer.resetAndStart();
        long longestPredictTime = 0;
        // start at the tree node
        TreeNode<Split> node = tree.getRoot();
        if(node.isEmpty()) {
            // root node has not been built, just return random guess
            return ArrayUtilities.uniformDistribution(getNumClasses());
        }
        int index = -1;
        int i = 0;
        Split split = node.getValue();
        final StopWatch testStageTimer = new StopWatch();
        // traverse the tree downwards from root
        while(
                !node.isLeaf()
                &&
                insideTestTimeLimit(testTimer.elapsedTime() + longestPredictTime)
        ) {
            testStageTimer.resetAndStart();
            // work out which branch to go to next
            index = split.findPartitionIndexFor(instance);
            // make this the next node to visit
            node = node.get(index);
            // get the split at that node
            split = node.getValue();
            // finish up this test stage
            testStageTimer.stop();
            longestPredictTime = testStageTimer.elapsedTime();
        }
        // hit a leaf node
        // the split defines the distribution for this test inst. If the split is pure, this will be a one-hot dist.
        double[] distribution = split.distributionForInstance(instance);
        // disable the resource monitors
        testTimer.stop();
        return distribution;
    }

    public int height() {
        return tree.height();
    }

    public int size() {
        return tree.size();
    }

    public int getR() {
        return r;
    }

    public void setR(final int r) {
        Assert.assertTrue(r > 0);
        this.r = r;
    }

    public SplitScorer getSplitScorer() {
        return splitScorer;
    }

    public void setSplitScorer(final SplitScorer splitScorer) {
        this.splitScorer = splitScorer;
    }

    @Override public String toString() {
        return "ProximityTree{tree=" + tree + "}";
    }

    private Split buildSplit(Split unbuiltSplit) {
        Split bestSplit = null;
        final TimeSeriesInstances data = unbuiltSplit.getData();
        final List<Integer> dataIndices = unbuiltSplit.getDataIndicesInTrainData();
        // need to find the best of R splits
        // linearly go through r splits and select the best
        for(int i = 0; i < r; i++) {
            // construct a new split
            final Split split = new Split(data, dataIndices);
            split.buildSplit();
            final double score = split.getScore();
            if(bestSplit == null || score > bestSplit.getScore()) {
                bestSplit = split;
            }
        }
        return Objects.requireNonNull(bestSplit);
    }

    public boolean isEarlyExemplarCheck() {
        return earlyExemplarCheck;
    }

    public void setEarlyExemplarCheck(final boolean earlyExemplarCheck) {
        this.earlyExemplarCheck = earlyExemplarCheck;
    }

    public boolean isEarlyAbandonDistances() {
        return earlyAbandonDistances;
    }

    public void setEarlyAbandonDistances(final boolean earlyAbandonDistances) {
        this.earlyAbandonDistances = earlyAbandonDistances;
    }

    public boolean isPartitionExaminationReordering() {
        return partitionExaminationReordering;
    }

    public void setPartitionExaminationReordering(final boolean partitionExaminationReordering) {
        this.partitionExaminationReordering = partitionExaminationReordering;
    }    

    private class Split implements Serializable, Iterator<Integer> {

        public Split(TimeSeriesInstances data, List<Integer> dataIndicesInTrainData) {
            setData(data, dataIndicesInTrainData);
        }
        
        // the distance function for comparing instances to exemplars
        private DistanceMeasure distanceMeasure;
        // the data at this split (i.e. before being partitioned)
        private TimeSeriesInstances data; // the split data
        private List<Integer> dataIndicesInTrainData; // the indices of the split data in the train data
        // the partitions of the data, each containing data for the partition and exemplars representing the partition
        // store pairwise set of data in the partition and corresponding exemplar
        private List<Integer> exemplarIndicesInSplitData;
        private List<TimeSeriesInstance> exemplars;
        private List<List<Integer>> partitionedDataIndicesInSplitData; // each list is a partition containing indices of insts in that partition. I.e. [[1,2,3],[4,5,6]] means partition 0 contains the 1,2,3rd inst at this split while partition 1 contains 4,5,6th inst at this split
        
        // partitionIndices houses all the partitions to look at when partitioning. This obviously stays consistent (i.e. look at all partitions in order) when not using early abandon
        private List<Integer> partitionIndices = null;
        
        // maintain a list of desc partition sizes per class. This ensures (when enabled) partitions are examined in
        // most likely first order
        private List<List<Integer>> partitionOrderByClass;
        
        // exemplars are normally checked ad-hoc during distance computation. Obviously checking which partition and exemplar belongs to is a waste of computation, as the distance will be zero and trump all other exemplars distances for other partitions. Therefore, it is important to check first. Original pf checked for exemplars as it went along, meaning for partition 5 it would compare exemplar 5 to exemplar 1..4 before realising it's an exemplar. Therefore, we can store the exemplar mapping to partition index and do a quick lookup before calculating distances. This is likely to only save a small amount of time, but increases as the breadth of trees / classes increases. I.e. for a 100 class problem, looking through 99 exemplars before realising we're examining the exemplar for the 100th partition is a large waste.
        private Map<Integer, Integer> exemplarIndexInSplitDataToPartitionIndex = null;

        // cache the scores
        private boolean findScore = true;
        // the score of this split
        private double score = -1;
        
        // track the stage of building
        private int instIndexInSplitData = -1;

        private TransformPipeline pipeline;
        private TimeSeriesInstances transformedDataAtSplit;
        
        private double[] distribution;
        
        public double[] distributionForInstance(TimeSeriesInstance testInst) {
            // report the prediction as the same as the data distribution at this split
            if(distribution == null) {
                distribution = new double[data.numClasses()];
                for(TimeSeriesInstance inst : data) {
                    distribution[inst.getLabelIndex()]++;
                }
                ArrayUtilities.normalise(distribution);
            }
            return distribution;
        }
        
        public List<Integer> getPartitionIndices() {
            return partitionIndices;
        }

        public List<List<Integer>> getPartitionOrderByClass() {
            return partitionOrderByClass;
        }

        private Labels<Integer> getParentLabels() {
            return new Labels<>(new AbstractList<Integer>() {
                @Override public Integer get(final int i) {
                    return data.get(i).getLabelIndex();
                }

                @Override public int size() {
                    return data.numInstances();
                }
            }); // todo weights
        }

        public double getScore() {
            if(findScore) {
                findScore = false;
                score = splitScorer.score(getParentLabels(), partitionedDataIndicesInSplitData.stream().map(partition -> new Labels<>(new AbstractList<Integer>() {
                    @Override public Integer get(final int i) {
                        final Integer instIndexInSplitData = partition.get(i);
                        return data.get(instIndexInSplitData).getLabelIndex();
                    }

                    @Override public int size() {
                        return partition.size();
                    }
                })).collect(Collectors.toList()));
                requireReal(score);
            }
            return score;
        }

        @Override public boolean hasNext() {
            return instIndexInSplitData + 1 < data.numInstances();
        }
        
        public void setup() {
            setupTransform();
            setupDistanceMeasure();
            setupExemplars();
            setupMisc();
        }

        @Override public Integer next() {
            
            // go through every instance and find which partition it should go into. This should be the partition
            // with the closest exemplar associate

            // shift i along to look at next inst
            instIndexInSplitData++;
            // mark that scores need recalculating, as we'd have added a new inst to a partition by the end of this method
            findScore = true;
            // get the inst to be partitioned
            final TimeSeriesInstance inst = data.get(instIndexInSplitData);
            Integer closestPartitionIndex = null;
            if(earlyExemplarCheck) {
                // check for exemplars. If the inst is an exemplar, we already know what partition it represents and therefore belongs to
                closestPartitionIndex = exemplarIndexInSplitDataToPartitionIndex.get(instIndexInSplitData);
            }

            // if null then not exemplar / not doing quick exemplar checking
            List<Integer> partitionIndicesOrder = null;
            int closestPartitionIndexIndex = -1;
            if(closestPartitionIndex == null) {
                if(partitionExaminationReordering) {
                    // use the desc order of partition size for the given class
                    partitionIndicesOrder = partitionOrderByClass.get(inst.getLabelIndex());
                } else {
                    // otherwise just loop through all partitions in order looking for the closest. Order is static and never changed
                    partitionIndicesOrder = partitionIndices;
                }
                closestPartitionIndexIndex = findPartitionIndexIndexFor(inst, instIndexInSplitData, partitionIndicesOrder);
                closestPartitionIndex = partitionIndicesOrder.get(closestPartitionIndexIndex);
            }

            final List<Integer> partition = partitionedDataIndicesInSplitData.get(closestPartitionIndex);
            partition.add(instIndexInSplitData);
            
            // if using partition reordering and order has been set
            if(partitionExaminationReordering && partitionIndicesOrder != null) {
                // we know the partition which the inst will be allocated to
                // need to update the partition order to maintain desc size
                partitionIndicesOrder.set(closestPartitionIndexIndex, partition.size());

                // continue shifting up the current partition until it is in the correct ascending order
                // e.g. index: [2,4,0,3,1]
                //      sizes: [3,2,2,2,1]
                //      would become (after incrementing size of partition 3, the closest partition, say):
                //      index: [2,4,0,3,1]
                //      sizes: [3,2,2,3,1]
                //      shift the partition 3 upwards until desc order restored:
                //      index: [2,3,4,0,1]
                //      sizes: [3,3,2,2,1]
                int i = closestPartitionIndexIndex - 1;
                while(i >= 1 && partitionIndicesOrder.get(i - 1) > partitionIndicesOrder.get(i)) {
                    Collections.swap(partitionIndicesOrder, i - 1, i);
                }

            }
            
            return closestPartitionIndex;
        }
        
        public void cleanup() {
            transformedDataAtSplit = null;

            // quick check that partitions line up with num insts
            if(isDebug()) {
                final HashSet<Integer> set = new HashSet<>();
                partitionedDataIndicesInSplitData.forEach(set::addAll);
                if(!new HashSet<>(new IndexList(data.numInstances())).containsAll(set)) {
                    throw new IllegalStateException("data indices mismatch");
                }
            }
        }

        /**
         * Get the cached version of a transformer. The cached version can persist transforms to avoid repetition.
         * @param transformer
         * @return
         */
        private Transformer getCachedTransformer(Transformer transformer) {
            if(transformerCache != null) {
                // get from internal source
                return transformerCache.computeIfAbsent(transformer, x -> new CachedTransformer(transformer));
            } else {
                return transformer;
            }
        }
        
        private void setupTransform() {
            
            pipeline = new TransformPipeline();

            if(data.isMultivariate()) {

                // if sampling dimensions, then wrap distance measure in a sampler
                int numDimensions = data.getMaxNumDimensions();
                if(DimensionSamplingMode.SINGLE.equals(dimensionSamplingMode) || DimensionSamplingMode.SUBSET.equals(dimensionSamplingMode)) {
                    final int numChoices;
                    if(DimensionSamplingMode.SUBSET.equals(dimensionSamplingMode)) {
                        // select anywhere between 1..all dimensions
                        numDimensions = RandomUtils.choiceIndex(numDimensions, getRandom()) + 1;
                    } else {
                        // select only 1 dimension
                        numDimensions = 1;
                    }
                    final List<Integer> dimensionIndices = RandomUtils.choiceIndex(data.getMaxNumDimensions(), getRandom(), numDimensions);
                    // build a hSlicer to slice insts to the specified dimensions
                    final HSlicer hSlicer = new HSlicer(dimensionIndices);
                    // add the hslice to the transform pipeline
                    pipeline.add(hSlicer);
                } else if(DimensionSamplingMode.ALL.equals(dimensionSamplingMode)) {
                    // do nothing
                } else {
                    throw new IllegalStateException("unknown dimension sampling mode");
                }

                // if converting multivariate data somehow, then do so
                // if shuffling before the convert, then do so
                if(DimensionConversionMode.SHUFFLE_STRATIFY.equals(dimensionConversionMode) ||
                           DimensionConversionMode.SHUFFLE_CONCAT.equals(dimensionConversionMode)) {
                    if(numDimensions > 1) {
                        final HReorderer reorderer = new HReorderer();
                        reorderer.setIndices(new ArrayList<>(new IndexList(numDimensions)));
                        pipeline.add(reorderer);
                    }
                }
                // then apply conversion from multivariate to univariate if need be
                if(DimensionConversionMode.CONCAT.equals(dimensionConversionMode)) {
                    // concat the dimensions into one (i.e. concat all the hSlices in turn)
                    pipeline.add(new HConcatenator());
                } else if(DimensionConversionMode.STRATIFY.equals(dimensionConversionMode)) {
                    // stratify the dimensions into one (i.e. concat all the vSlices in turn)
                    pipeline.add(new VConcatenator());
                } else if(DimensionConversionMode.NONE.equals(dimensionConversionMode)
                                  || DimensionConversionMode.SHUFFLE_CONCAT.equals(dimensionConversionMode)
                                  || DimensionConversionMode.SHUFFLE_STRATIFY.equals(dimensionConversionMode)) {
                    // do nothing, each mode already handled
                } else {
                    throw new IllegalStateException("unknown dimension conversion mode");
                }

            }
            
            transformedDataAtSplit = pipeline.fitTransform(data);
        }
        
        private void setupDistanceMeasure() {
            
            // pick the distance function
            // pick a random space
            ParamSpaceBuilder distanceMeasureSpaceBuilder = RandomUtils.choice(distanceMeasureSpaceBuilders, getRandom());
            // built that space
            ParamSpace distanceMeasureSpace = distanceMeasureSpaceBuilder.build(transformedDataAtSplit);
            // randomly pick the distance function / parameters from that space
            final ParamSet paramSet = RandomSearch.choice(distanceMeasureSpace, getRandom());
            // there is only one distance function in the ParamSet returned
            distanceMeasure = Objects.requireNonNull((DistanceMeasure) paramSet.get(DistanceMeasure.DISTANCE_MEASURE_FLAG));
            
            // if we can cache the transforms
            if(isCacheTransforms()) {
                // check whether the distance measure involves a transform
                if(distanceMeasure instanceof TransformDistanceMeasure) {
                    Transformer transformer = ((TransformDistanceMeasure) distanceMeasure).getTransformer();
                    // check if transformer is of a type which can be cached
                    if(transformer instanceof Derivative) {
                        // cache all der transforms as they're simple, pure functions
                        transformer = getCachedTransformer(transformer);
                    }
                    // update the transformer with the cached version
                    ((TransformDistanceMeasure) distanceMeasure).setTransformer(transformer);
                }
            }

            // wrap distance measure in multivariate handling capabilities (i.e. dependent / indep dist, etc)
            if(transformedDataAtSplit.isMultivariate()) {

                // apply the distance mode
                DistanceMode distanceMode = ProximityTree.this.distanceMode;
                // if randomly picking distance mode
                if(distanceMode.equals(DistanceMode.RANDOM)) {
                    // then random pick from the remaining modes
                    final Integer index = RandomUtils
                                                  .choiceIndexExcept(DistanceMode.values().length, getRandom(),
                                                          DistanceMode.RANDOM.ordinal());
                    distanceMode = DistanceMode.values()[index];
                }
                // if in independent mode
                if(distanceMode.equals(DistanceMode.INDEPENDENT)) {
                    // then wrap the distance measure to evaluate each dimension in isolation
                    distanceMeasure = new IndependentDistanceMeasure(distanceMeasure);
                } else if(distanceMode.equals(DistanceMode.DEPENDENT)) {
                    // do nothing, dependent is the default in distance measures
                } else {
                    throw new IllegalStateException("unknown distance mode");
                }

            }
            
            // setup the distance function (note this JUST sets up the distance measure, not the transformed distance measure)
            distanceMeasure.buildDistanceMeasure(transformedDataAtSplit);
                        
        }
        
        private void setupExemplars() {

            // pick the exemplars
            Objects.requireNonNull(transformedDataAtSplit);
            Assert.assertTrue(transformedDataAtSplit.iterator().hasNext());
            // change the view of the data into per class
            final List<List<Integer>> instIndicesByClass = data.getInstIndicesByClass();
            // pick exemplars per class
            partitionedDataIndicesInSplitData = new ArrayList<>(data.numClasses());
            exemplars = new ArrayList<>(data.numClasses());
            exemplarIndicesInSplitData = new ArrayList<>(data.numClasses());
            // generate a partition per class
            for(final List<Integer> sameClassInstIndices : instIndicesByClass) {
                // avoid empty classes, no need to create partition / exemplars from them
                if(!sameClassInstIndices.isEmpty()) {
                    // get the indices of all instances with the specified class
                    // random pick exemplars from this 
                    final Integer exemplarIndexInSplitData = RandomUtils.choice(sameClassInstIndices, getRandom());
                    exemplarIndicesInSplitData.add(exemplarIndexInSplitData);
                    // generate the partition with empty data and the chosen exemplar instances
                    final ArrayList<Integer> partition = new ArrayList<>();
                    partitionedDataIndicesInSplitData.add(partition);
                    // find the index of the exemplar in the dataIndices (i.e. the exemplar may be the 5th instance 
                    // in the data but the 5th instance may have index 33 in the train data)
                    TimeSeriesInstance exemplar = transformedDataAtSplit.get(exemplarIndexInSplitData);
                    exemplars.add(exemplar);
                }
            }
        }
        
        private void setupMisc() {
            // the list of partition indices to browse through when allocating an inst to a partition
           partitionIndices = new IndexList(partitionedDataIndicesInSplitData.size());
            if(partitionExaminationReordering) {
                // init the desc order of partitions for each class
                
                // for each class, make a list holding the partition indices in desc order of size
                // this order will be maintained as insts are allocated to partitions, hence maintaining a list of
                // the most likely partition to end up in given an inst is of a certain class
                partitionOrderByClass = new ArrayList<>(data.numClasses());
                for(int i = 0; i < data.numClasses(); i++) {
                    partitionOrderByClass.add(new ArrayList<>(partitionIndices));
                }
            }


            if(earlyExemplarCheck) {
                exemplarIndexInSplitDataToPartitionIndex = new HashMap<>();

                // chuck all exemplars in a map to check against before doing distance computation
                for(int i = 0; i < exemplarIndicesInSplitData.size(); i++) {
                    final Integer exemplarIndexInSplitData = exemplarIndicesInSplitData.get(i);
                    exemplarIndexInSplitDataToPartitionIndex.put(exemplarIndexInSplitData, i);
                }
                
            }
        }
        
        /**
         * Partition the data and derive score for this split.
         */
        public void buildSplit() {
            setup();
            while(hasNext()) {
                next();
            }
            cleanup();
        }

        public DistanceMeasure getDistanceMeasure() {
            return distanceMeasure;
        }
        
        public int findPartitionIndexFor(final TimeSeriesInstance inst, int instIndexInSplitData, List<Integer> partitionIndices) {
            final int partitionIndexIndex = findPartitionIndexIndexFor(inst, instIndexInSplitData, partitionIndices);
            return partitionIndices.get(partitionIndexIndex);
        }

        /**
         * get the partition of the given instance. The returned partition is the set of data the given instance belongs to based on its proximity to the exemplar instances representing the partition.
         *
         * @param inst
         * @param instIndexInSplitData the index of the inst in the data at this node. If the inst is not in the data at this node then set this to -1
         * @return
         */
        public int findPartitionIndexIndexFor(TimeSeriesInstance inst, int instIndexInSplitData, List<Integer> partitionIndicesIterator) {
            // replace inst with transformed version (i.e. apply the multivariate strategy)
            if(instIndexInSplitData >= 0) {
                inst = transformedDataAtSplit.get(instIndexInSplitData);
            } else {
                inst = pipeline.transform(inst);
            }
            // a map to maintain the closest partition indices
            final PrunedMap<Double, Integer> filter = PrunedMap.asc(1);
            // maintain a limit on distance computation
            double limit = Double.POSITIVE_INFINITY;
            // loop through exemplars / partitions
            for(int i = 0; i < partitionIndicesIterator.size(); i++) {
                final Integer exemplarIndexInSplitData = exemplarIndicesInSplitData.get(i);
                // check the instance isn't an exemplar
                if(!earlyExemplarCheck && instIndexInSplitData == exemplarIndexInSplitData) {
                    return i;
                }
                final TimeSeriesInstance exemplar = exemplars.get(i);
                // find the distance
                final double distance = distanceMeasure.distance(exemplar, inst, limit);
                // add the distance and partition to the map
                if(filter.add(distance, i)) {
                    // new min dist
                    // set new limit if early abandon enabled
                    if(earlyAbandonDistances) {
                        limit = distance;
                    }
                }
            }
            
            // random pick the best partition for the instance
            return RandomUtils.choice(filter.valuesList(), rand);
        }

        /**
         * Find the partition index of an unseen instance (i.e. a test inst)
         * @param inst
         * @return
         */
        public int findPartitionIndexFor(final TimeSeriesInstance inst) {
            return findPartitionIndexFor(inst, -1, partitionIndices);
        }

        public TimeSeriesInstances getData() {
            return data;
        }

        public void setData(TimeSeriesInstances data, List<Integer> dataIndices) {
            this.dataIndicesInTrainData = Objects.requireNonNull(dataIndices);
            this.data = Objects.requireNonNull(data);
            Assert.assertEquals(data.numInstances(), dataIndices.size());
        }

        public List<Integer> getDataIndicesInTrainData() {
            return dataIndicesInTrainData;
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            if(distanceMeasure != null) {
                // then split has been built
                sb.append("df=").append(distanceMeasure);
                sb.append(", partitionedDataIndices=").append(getPartitionedDataIndicesInTrainData());
                sb.append(", ");
            }
            sb.append("dataIndices=").append(dataIndicesInTrainData);
                    
            return sb.toString();
        }
        
        public int numPartitions() {
            return partitionedDataIndicesInSplitData.size();
        }
        
        public List<List<Integer>> getPartitionedDataIndicesInTrainData() {
            List<List<Integer>> indices = new ArrayList<>();
            for(int i = 0; i < numPartitions(); i++) {
                indices.add(getPartitionDataIndicesInTrainData(i));
            }
            return indices;
        }
        
        public List<TimeSeriesInstances> getPartitionedData() {
            List<TimeSeriesInstances> data = new ArrayList<>();
            for(int i = 0; i < numPartitions(); i++) {
                data.add(getPartitionData(i));
            }
            return data;
        }
        
        public TimeSeriesInstances getPartitionData(int i) {
            final List<TimeSeriesInstance> data =
                    partitionedDataIndicesInSplitData.get(i).stream().map(this.data::get)
                            .collect(Collectors.toList());
            return new TimeSeriesInstances(data, this.data.getClassLabels());
        }
        
        public List<Integer> getPartitionDataIndicesInTrainData(int i) {
            return partitionedDataIndicesInSplitData.get(i).stream().map(dataIndicesInTrainData::get).collect(Collectors.toList());
        }

    }

}
