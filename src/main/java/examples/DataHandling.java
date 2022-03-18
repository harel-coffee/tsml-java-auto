/*
 * Copyright (C) 2019 xmw13bzu
 *
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

package examples;

import experiments.data.DatasetLists;
import experiments.data.DatasetLoading;
import tsml.data_containers.TimeSeries;
import tsml.data_containers.TimeSeriesInstance;
import tsml.data_containers.TimeSeriesInstances;
import tsml.data_containers.utilities.TimeSeriesResampler;
import utilities.InstanceTools;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.util.Arrays;

/**
 * Examples to show different ways of loading and basic handling of datasets
 * 
 * @author James Large (james.large@uea.ac.uk)
 */
public class DataHandling {

    public static void main(String[] args) throws Exception {
        /*
         * Uncomment which function is needed depending on data file type.
         */
        // dataHandlingWithARFF(); // .arff

        // dataHandlingWithTS(); // .ts


        String whereTheDataIs = DatasetLoading.BAKED_IN_TSC_DATA_PATH;
        String whereToPutTheData = "C:\\Temp\\";
        String[] problems = DatasetLists.tscProblems112;
        problems = new String[]{"Chinatown"};
        int resamples = 30;
        for (String str : problems) {
            for (int i = 0; i < 30; i++)
                resamplingData(whereTheDataIs, whereToPutTheData, str, i);
        }
    }

    public static void resamplingData(String source, String dest, String problem, int resample) throws IOException {
        Instances train = DatasetLoading.loadData(source + problem + "/" + problem + "_TRAIN.arff");
        Instances test = DatasetLoading.loadData(source + problem + "/" + problem + "_TEST.arff");

        // We could then resample these, while maintaining train/test distributions, using this

        Instances[] trainTest = InstanceTools.resampleTrainAndTestInstances(train, test, resample);
        train = trainTest[0];
        test = trainTest[1];

        DatasetLoading.saveDataset(train, dest + problem + "/" + problem + "_" + resample + "_TRAIN" + ".arff");
        DatasetLoading.saveDataset(test, dest + problem + "/" + problem + "_" + resample + "_TEST" + ".arff");
    }

    private static void dataHandlingWithARFF() throws Exception {
        // We'll be loading the ItalyPowerDemand dataset which is distributed with this codebase
        String basePath = "src/main/java/experiments/data/tsc/";
        String dataset = "ItalyPowerDemand";
        int seed = 1;

        Instances train;
        Instances test;
        Instances[] trainTest;




        ///////////// Loading method 1: loading individual files
        // DatasetLoading.loadData...(...)
        // For loading in a single arff without performing any kind of sampling. Class value is
        // assumed to be the last attribute

        train = DatasetLoading.loadDataThrowable(basePath + dataset + "/" + dataset + "_TRAIN.arff");
        test = DatasetLoading.loadDataThrowable(basePath + dataset + "/" + dataset + "_TEST.arff");

        // We could then resample these, while maintaining train/test distributions, using this

        trainTest = InstanceTools.resampleTrainAndTestInstances(train, test, seed);
        train = trainTest[0];
        test = trainTest[1];






        ///////////// Loading method 2: sampling directly
        // DatasetLoading.sampleDataset(...)
        // Wraps the data loading and sampling performed above. Read in a dataset either
        // from a single complete file (e.g. uci data) or a predefined split (e.g. ucr/tsc data)
        // and resamples it according to the seed given. If the resampled fold can already
        // be found in the read location ({dsetname}{foldid}_TRAIN and _TEST) then it will
        // load those. See the sampleDataset(...) javadoc

        trainTest = DatasetLoading.sampleDataset(basePath, dataset, seed);
        train = trainTest[0];
        test = trainTest[1];






        ///////////// Loading method 3: sampling the built in dataset
        // DatasetLoading.sampleDataset(...)
        // Because ItalyPowerDemand is distributed with the codebase, there's a wrapper
        // to sample it directly for quick testing

        trainTest = DatasetLoading.sampleItalyPowerDemand(seed);
        train = trainTest[0];
        test = trainTest[1];






        //////////// Data inspection and handling:
        // We can look at the basic meta info

        System.out.println("train.relationName() = " + train.relationName());
        System.out.println("train.numInstances() = " + train.numInstances());
        System.out.println("train.numAttributes() = " + train.numAttributes());
        System.out.println("train.numClasses() = " + train.numClasses());

        // And the individual instances

        for (Instance inst : train)
            System.out.print(inst.classValue() + ", ");
        System.out.println("");









        // Often for speed we just want the data in a primitive array
        // We can go to and from them using this sort of procedure

        // Lets keeps the class labels separate in this example
        double[] classLabels = train.attributeToDoubleArray(train.classIndex()); // aka y_train

        boolean removeLastVal = true;
        double[][] data = InstanceTools.fromWekaInstancesArray(train, removeLastVal); // aka X_train

        // We can then do whatever fast array-optimised stuff, and shove it back into an instances object
        Instances reformedTrain = InstanceTools.toWekaInstances(data, classLabels);
    }

    private static void dataHandlingWithTS() throws IOException {
        // We'll be loading the ItalyPowerDemand dataset which is distributed with this codebase
        String basePath = "src/main/java/experiments/data/tsc/";
        String dataset = "ItalyPowerDemand";
        int seed = 1;

        TimeSeriesInstances train;
        TimeSeriesInstances test;
        TimeSeriesResampler.TrainTest trainTest;
        TimeSeriesInstances[] trainTestSplit;



        /*
         * Loading method 1: loading individual files.
         *
         * For loading in a single ts file without performing any kind of sampling.
         */
        train = DatasetLoading.loadTSData(basePath + dataset + "/" + dataset + "_TRAIN.ts");
        test = DatasetLoading.loadTSData(basePath + dataset + "/" + dataset + "_TEST.ts");

        // We could then resample these, while maintaining train/test distributions, using this
        trainTest = TimeSeriesResampler.resampleTrainTest(train, test, seed);
        train = trainTest.train;
        test = trainTest.test;



        /*
         * Loading method 2: sampling directly.
         *
         * For loading in a single ts file and resampling the data
         */
        trainTestSplit = DatasetLoading.sampleTSDataset(basePath, dataset, seed);
        train = trainTestSplit[0];
        test = trainTestSplit[1];



        /*
         * Loading method 3: sampling the build in dataset.
         *
         * Because ItalyPowerDemand is distributed with the codebase, there's a wrapper
         * to sample it directly for quick testing
         */
        trainTestSplit = DatasetLoading.sampleItalyPowerDemandTS(seed);
        train = trainTestSplit[0];
        test = trainTestSplit[1];



        /*
         * Data inspection and handling. We can look at the basic meta info.
         */
        System.out.println("train.getProblemName() = " + train.getProblemName());
        System.out.println("train.getDescription() = " + train.getDescription());
        System.out.println("train.numInstances() = " + train.numInstances());
        System.out.println("train.numClasses() = " + train.numClasses());
        System.out.println("train.getClassLabels() = " + Arrays.toString(train.getClassLabels()));
        System.out.println("train.getClassCounts() = " + Arrays.toString(train.getClassCounts()));
        System.out.println("train.getClassIndexes() = " + Arrays.toString(train.getClassIndexes()));



        /*
         * Example of the data structure format
         */
        // for each instance
        for (TimeSeriesInstance instance : train) {
            // for each dimension in the instance (multivariate support)
            for (TimeSeries series : instance) {
                System.out.println(Arrays.toString(series.toValueArray()));
            }
        }
    }
}
