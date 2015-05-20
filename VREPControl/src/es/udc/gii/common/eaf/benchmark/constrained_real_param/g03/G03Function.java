/*
* Copyright (C) 2010 Grupo Integrado de Ingeniería
* 
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/ 


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.udc.gii.common.eaf.benchmark.constrained_real_param.g03;

import es.udc.gii.common.eaf.benchmark.constrained_real_param.g02.*;
import es.udc.gii.common.eaf.benchmark.constrained_real_param.g01.*;

/**
 *
 * @author Grupo Integrado de Ingeniería (<a href="http://www.gii.udc.es">www.gii.udc.es</a>) 
 * @since 1.0
 */
public class G03Function {

    /**
     * xi ~ [0, 1]; i = 1 ... n
     * @param values
     */
    public static double[] normalize(double[] values) {

        double[] norm_values = new double[values.length];

        for (int i = 0; i<values.length; i++) {
            norm_values[i] = (values[i]+1.0)*0.5;
        }

        return norm_values;

    }

}
