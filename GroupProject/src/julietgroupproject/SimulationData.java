/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.encog.ml.MLRegression;

/**
 *
 * @author Peter
 */
public class SimulationData {
    private MLRegression toEvaluate;
    private double simTime;
    private double fitness;
    private boolean fitnessCalculated;
    
    
    public SimulationData(MLRegression _toEvaluate, double _simTime)
    {
        toEvaluate = _toEvaluate;
        simTime = _simTime;
        fitnessCalculated = false;
    }
    
    public synchronized double getFitness()
    {
        while (!fitnessCalculated)
        {
            try {
                this.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(SimulationData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return fitness;
    }
    
    public synchronized void setFitness(double _fitness)
    {
        fitness = _fitness;
        fitnessCalculated = true;
        this.notifyAll();
    }
    
    public MLRegression getToEvaluate()
    {
        return toEvaluate;
    }
    
    public double getSimTime()
    {
        return simTime;
    }
}
