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

package machine_learning.classifiers;

import de.bwaldvogel.liblinear.*;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Randomizable;

import java.util.Random;

/**
 * liblnear wrapper for WEKA
 *
 * @author Matthew Middlehurst
 */
public class LibLinearClassifier extends AbstractClassifier implements Randomizable {

    private boolean normalise = true;
    private double[] means, stdevs;

    private boolean tuneC = true;
    private int nr_fold = 5;

    private double bias = 0;
    private SolverType solverType = SolverType.L2R_L2LOSS_SVC;
    private int iterations = 1000;
    private double e = 0.01;
    private double p = 0.1;
    private double c = 1;

    private Model linearModel;

    private int seed;

    public void setNormalise(boolean normalise) {
        this.normalise = normalise;
    }

    public void setTuneC(boolean tuneC) {
        this.tuneC = tuneC;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public void setSolverType(SolverType solverType) {
        this.solverType = solverType;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void setE(double e) {
        this.e = e;
    }

    public void setP(double p) {
        this.p = p;
    }

    public void setC(double c) {
        this.c = c;
    }

    @Override
    public void setSeed(int seed) {
        this.seed = seed;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public void buildClassifier(Instances data) throws Exception {
        if (normalise) {
            means = new double[data.numAttributes() - 1];
            stdevs = new double[data.numAttributes() - 1];

            for (int i = 0; i < data.numAttributes() - 1; i++) {
                for (int n = 0; n < data.numInstances(); n++) {
                    means[i] += data.get(n).value(i);
                }
                means[i] /= data.numInstances();

                for (int n = 0; n < data.numInstances(); n++) {
                    double temp = data.get(n).value(i) - means[i];
                    stdevs[i] += temp * temp;
                }
                stdevs[i] = Math.sqrt(stdevs[i] / (data.numInstances() - 1));

                if (stdevs[i] == 0) stdevs[i] = 1;
            }
        }

        FeatureNode[][] features = new FeatureNode[data.numInstances()][];
        double[] labels = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            Instance inst = data.get(i);
            features[i] = toFeatureNodes(inst);
            labels[i] = inst.classValue();
        }

        Problem problem = new Problem();
        problem.bias = bias;
        problem.y = labels;
        problem.n = data.numAttributes() - 1;
        problem.l = features.length;
        problem.x = features;

        Parameter par = new Parameter(solverType, c, e, iterations, p);

        Linear.resetRandom();
        Linear.disableDebugOutput();

        if (tuneC) {
            final int l = problem.l;
            if (nr_fold > l) {
                nr_fold = l;
            }

            final int[] perm = new int[l];
            final int[] fold_start = new int[nr_fold + 1];

            Random rand = new Random(seed);

            int k;
            for (k = 0; k < l; k++) {
                perm[k] = k;
            }
            for (k = 0; k < l; k++) {
                int j = k + rand.nextInt(l - k);
                int temp = perm[k];
                perm[k] = perm[j];
                perm[j] = temp;
            }
            for (k = 0; k <= nr_fold; k++) {
                fold_start[k] = k * l / nr_fold;
            }

            double[] cVals = new double[]{0.001, 0.01, 0.1, 1, 10, 100};
            int mostCorrect = Integer.MIN_VALUE;

            for (double cVal : cVals) {
                Parameter subpar = new Parameter(solverType, cVal, e, iterations, p);
                int correct = 0;

                for (int i = 0; i < nr_fold; i++) {
                    int begin = fold_start[i];
                    int end = fold_start[i + 1];
                    int j, kk;
                    Problem subprob = new Problem();

                    subprob.bias = problem.bias;
                    subprob.n = problem.n;
                    subprob.l = l - (end - begin);
                    subprob.x = new Feature[subprob.l][];
                    subprob.y = new double[subprob.l];

                    kk = 0;
                    for (j = 0; j < begin; j++) {
                        subprob.x[kk] = problem.x[perm[j]];
                        subprob.y[kk] = problem.y[perm[j]];
                        ++kk;
                    }
                    for (j = end; j < l; j++) {
                        subprob.x[kk] = problem.x[perm[j]];
                        subprob.y[kk] = problem.y[perm[j]];
                        ++kk;
                    }

                    Model submodel = Linear.train(subprob, subpar);
                    for (j = begin; j < end; j++) {
                        if (problem.y[perm[j]] == Linear.predict(submodel, problem.x[perm[j]])) correct++;
                    }
                }

                if (correct > mostCorrect) {
                    mostCorrect = correct;
                    par = subpar;
                }
            }
        }

        linearModel = Linear.train(problem, par);
    }

    @Override
    public double classifyInstance(Instance inst) throws Exception {
        FeatureNode[] features = toFeatureNodes(inst);
        return Linear.predict(linearModel, features);
    }

    public double[] distributionForInstance(Instance inst) throws Exception {
        double[] probs;
        if (solverType.isLogisticRegressionSolver()) {
            FeatureNode[] features = toFeatureNodes(inst);
            probs = new double[inst.dataset().numClasses()];
            Linear.predictProbability(linearModel, features, probs);
        } else {
            probs = super.distributionForInstance(inst);
        }
        return probs;
    }

    private FeatureNode[] toFeatureNodes(Instance inst) {
        FeatureNode[] features = new FeatureNode[inst.numAttributes() - 1];
        if (normalise) {
            for (int n = 0; n < features.length; n++) {
                features[n] = new FeatureNode(n + 1, (inst.value(n) - means[n]) / stdevs[n]);
            }
        } else {
            for (int n = 0; n < features.length; n++) {
                features[n] = new FeatureNode(n + 1, inst.value(n));
            }
        }
        return features;
    }
}
