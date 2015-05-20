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
 * SchwefelsProblemObjectiveFunction.java
 *
 * Created on 4 de julio de 2007, 19:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package es.udc.gii.common.eaf.benchmark.real_param;

import es.udc.gii.common.eaf.benchmark.BenchmarkObjectiveFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * El optimo de esta funcion objetivo es f(1,...,1) = 0.   
 *
 * @author Grupo Integrado de Ingeniería (<a href="http://www.gii.udc.es">www.gii.udc.es</a>) 
 * @since 1.0
 */
public class GeneralizedRosenbrocksObjectiveFunction extends BenchmarkObjectiveFunction {
    
    /** Creates a new instance of SchwefelsProblemObjectiveFunction */
    public GeneralizedRosenbrocksObjectiveFunction() {
    }
    
    @Override
    public double evaluate(double[] values) {
        
        double fitness = 0.0;        
        double[] x;
        
        x = values;
        
        for (int i = 0; i<x.length-1; i++) {
            
            //x ~ [-30.0, 30.0]
            
            fitness += 100.0*Math.pow((x[i+1]*30.0-Math.pow(
                    x[i]*30.0,2.0)),2.0) + Math.pow((x[i]*30.0)-1,2.0);
            
        }
        
        
        return fitness;
        
    }
    
    
    @Override
    public void reset() {
    }
       
    @Override
    public double[][] getOptimum(int dim) {

        double[][] optimums = new double[1][];
        double[] optimum = new double[dim];

        Arrays.fill(optimum, 1.0/30.0);

        optimums[0] = optimum;
        return optimums;

    }
    
    @Override
    public String toString() {
        
        return "Generalized Rosenbrock's Objective Function";
        
    }
    
}
