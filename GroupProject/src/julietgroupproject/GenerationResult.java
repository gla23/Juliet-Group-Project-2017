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
 * Class to store information about the result of a generation of training
 * 
 * @author George
 */
public class GenerationResult implements Serializable{
    
    //DecimalFormat for converting to readable (3 decimal place) strings
    private static DecimalFormat df = new DecimalFormat(".###");
    
    //the generation number these are the results for
    public int generation;
    //the fitness of the best individual in the generation
    public float fitness;
    //the neural network which achieved the best fitness
    public MLRegression bestGenome;
    
    /**
     * Construct a record of a generation of training.
     * 
     * @param generation the generation number these are the results from
     * @param fitness the fitness achieved by the best member of the population
     * @param bestGenome the neural network which achieved the best fitness
     */
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
