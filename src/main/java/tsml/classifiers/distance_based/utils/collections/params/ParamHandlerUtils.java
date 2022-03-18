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
 
package tsml.classifiers.distance_based.utils.collections.params;

import tsml.classifiers.distance_based.utils.strings.StrUtils;
import weka.core.OptionHandler;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class ParamHandlerUtils {

    /**
     * set a parameter to a ParamSet. Parameters are propogated through that object to children, if any parameters
     * are specified for the children.
     * @param object
     * @param paramSet
     */
    public static void setParams(Object object, ParamSet paramSet) {
        try {
            if(object instanceof ParamHandler) {
                ((ParamHandler) object).setParams(paramSet);
            } else if(object instanceof OptionHandler) {
                ((OptionHandler) object).setOptions(paramSet.getOptions());
            } else {
                throw new IllegalArgumentException("params not settable");
            }
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public static ParamSet getParams(Object object) {
        try {
            if(object instanceof ParamHandler) {
                return ((ParamHandler) object).getParams();
            } else if(object instanceof OptionHandler) {
                return new ParamSet(((OptionHandler) object).getOptions());
            } else {
                // not a paramhandler so return empty params
                return new ParamSet();
            }
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    

}
