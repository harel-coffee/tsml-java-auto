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
 
package tsml.transformers.shapelet_tools.search_functions.aaron_search;

import java.util.ArrayList;
import java.util.BitSet;
import static utilities.GenericTools.randomRange;

import tsml.transformers.shapelet_tools.search_functions.ShapeletSearchOptions;
import weka.core.Instance;
import weka.core.Instances;
import tsml.transformers.shapelet_tools.Shapelet;
/**
 *
 * @author raj09hxu
 */
public class MagnifySearch extends ImprovedRandomSearch {

    int initialNumShapeletsPerSeries;
    
    
    //how many times do we want to make our search area smaller.
    int maxDepth = 3;

    public MagnifySearch(ShapeletSearchOptions ops) {
        super(ops);
        
        proportion = ops.getProportion();
    }
    
    float proportion = 1.0f;
    BitSet seriesToConsider;
    
    @Override
    public void init(Instances input){       
        //we need to detect whether it's multivariate or univariate.
        //this feels like a hack. BOO.
        //one relational and a class att.
        maxDepth = 3;
        inputData = input;
        seriesLength = setSeriesLength();

        
        float subsampleSize = (float) inputData.numInstances() * proportion;
        initialNumShapeletsPerSeries = (int) ((float) numShapeletsPerSeries / subsampleSize);
        seriesToConsider = new BitSet(inputData.numInstances());
        
        //if we're looking at less than root(m) shapelets per series. sample to root n.
        if(initialNumShapeletsPerSeries < Math.sqrt(inputData.numAttributes()-1)){
            //recalc prop and subsample size.
            proportion =  ((float) Math.sqrt(inputData.numInstances()) / (float)inputData.numInstances());
            subsampleSize = (float) inputData.numInstances() * proportion;
            initialNumShapeletsPerSeries = (int) ((float) numShapeletsPerSeries / subsampleSize);
            System.out.println("sampling");
        }
        
        initialNumShapeletsPerSeries /= (float) maxDepth;
        
        //if proportion is 1.0 enable all series.
        if(proportion >= 1.0){
            seriesToConsider.set(0, inputData.numInstances(), true); //enable all
        }
        else{
            for(int i=0; i< subsampleSize; i++){
                seriesToConsider.set(random.nextInt((int) inputData.numInstances()));
            }
            System.out.println(seriesToConsider);
        }
        
        if(initialNumShapeletsPerSeries < 1)
            System.err.println("Too Few Starting shapelets");
    }
    
    @Override
    public ArrayList<Shapelet> searchForShapeletsInSeries(Instance timeSeries, ProcessCandidate checkCandidate){
        ArrayList<Shapelet> candidateList = new ArrayList<>();
        
        if(!seriesToConsider.get(currentSeries++)) return candidateList;
        
        //we want to iteratively shrink our search area.
        int minLength = minShapeletLength;
        int maxLength = maxShapeletLength;
        int minPos = 0;
        //if the series is m = 100. our max pos is 97, when min is 3.
        int maxPos = maxShapeletLength - minShapeletLength + 1;
        
        int lengthWidth = (maxLength - minLength)  /  2;
        int posWidth = (maxPos + minPos) / 2;
        
        for(int depth = 0; depth < maxDepth; depth++){
            Shapelet bsf = null;

            //we divide the numShapeletsPerSeries by maxDepth.
            for(int i = 0; i< initialNumShapeletsPerSeries; i++){
                CandidateSearchData sh = createRandomShapelet(seriesLength-1, minLength, maxLength, minPos, maxPos);
                Shapelet shape = checkCandidate.process(timeSeries, sh.getStartPosition(), sh.getLength());
                
                
                if(bsf == null) {
                    bsf = shape;
                }
                
                if(shape == null || bsf == null) continue;
                
                //if we're at the bottom level we should start compiling the list.
                if(depth == maxDepth-1)
                    candidateList.add(shape);
                
                
                if(comparator.compare(bsf, shape) > 0){
                    bsf = shape;
                }
            }
            
            //add each best so far.
            //should give us a bit of a range of improving shapelets and a really gone one.
            //do another trial. -- this is super unlikely.
            if(bsf==null) {
                continue;
            }
            
            
            //change the search parameters based on the new best.
            //divide by 2.
            lengthWidth >>= 1;
            posWidth >>= 1;
            minLength = bsf.length - lengthWidth;
            maxLength = bsf.length + lengthWidth;
            minPos = bsf.startPos - posWidth;
            maxPos = bsf.startPos + posWidth;
        }

        return candidateList;
    }
    
    private CandidateSearchData createRandomShapelet(int totalLength, int minLen, int maxLen, int minPos, int maxPos){
        //clamp the lengths.
        //never let the max length go lower than 3.
        int maxL = Math.min(totalLength, Math.max(maxLen, minShapeletLength));
        int minL = Math.max(minShapeletLength, minLen);
        
        int length = randomRange(random, minL, maxL);
        

        //calculate max and min clamp based on length.
        //can't have max position > m-l+1
        //choose the smaller of the two.
        int maxP = Math.min(totalLength-length, maxPos);
        int minP = Math.max(0, minPos);
        int position = randomRange(random, minP, maxP);
        
        return new CandidateSearchData(position,length);   
    }
    
    
    public static void main(String[] args){
        ShapeletSearchOptions magnifyOptions = new ShapeletSearchOptions.Builder().setMin(3).setMax(100).setNumShapeletsToEvaluate(1000).setSeed(0).build();
        MagnifySearch ps = new MagnifySearch(magnifyOptions);
        
        for(int i=0; i<100; i++)
            System.out.println(ps.createRandomShapelet(100, 3, 100, 0, 100));
        
    }
    
}
