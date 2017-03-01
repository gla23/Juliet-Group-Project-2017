/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import java.io.Serializable;
import java.text.DecimalFormat;
import org.encog.ml.MLRegression;

/**
 *
 * @author George
 */
public class GenerationResult implements Serializable{
    
    private static DecimalFormat df = new DecimalFormat(".###");
            
    public int generation;
    public float fitness;
    public MLRegression bestGenome;
    
    public GenerationResult(int generation, float fitness, MLRegression bestGenome) {
        this.generation = generation;
        this.fitness = fitness;
        this.bestGenome = bestGenome;
    }
    
    @Override
    public String toString() {
        return " Generation #" + generation + ", best fitness " + df.format(fitness);
    } 
}
