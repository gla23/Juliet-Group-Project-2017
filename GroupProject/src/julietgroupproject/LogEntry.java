/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import java.text.DecimalFormat;

/**
 *
 * @author George
 */
public class LogEntry {
    
    private static DecimalFormat df = new DecimalFormat(".###");
            
    public int generation;
    public float fitness;
    
    public LogEntry(int generation, float fitness) {
        this.generation = generation;
        this.fitness = fitness;
    }
    
    @Override
    public String toString() {
        return " Generation #" + generation + ", best fitness " + df.format(fitness);
    } 
}
