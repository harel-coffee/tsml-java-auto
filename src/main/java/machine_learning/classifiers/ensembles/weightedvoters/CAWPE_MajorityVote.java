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
 
package machine_learning.classifiers.ensembles.weightedvoters;

import machine_learning.classifiers.ensembles.voting.MajorityVote;
import machine_learning.classifiers.ensembles.weightings.EqualWeighting;
import machine_learning.classifiers.ensembles.CAWPE;

/**
 * Implemented as separate classifier for explicit comparison, from Kuncheva and Rodríguez (2014)
 * 
 * @author James Large (james.large@uea.ac.uk)
 */
public class CAWPE_MajorityVote extends CAWPE {
    public CAWPE_MajorityVote() { 
        super(); //sets default classifiers etc 
        
        //overwriting relevant parts 
        ensembleName = "CAWPE_MajorityVote"; 
        weightingScheme = new EqualWeighting();
        votingScheme = new MajorityVote();
    }   
}
