/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.encog.ml.MLRegression;

/**
 * A simulation task to be performed by simulator.
 * It contains the neural network and some other
 * parameters for simulation, as well as the
 * setFitness method to retain the simulation result.
 * 
 * @author Peter
 */
public class SimulationData {
    private MLRegression toEvaluate;
    private double simTime;
    private double fitness;
    private boolean fitnessCalculated;
    private boolean simTerminated;
    
    
    public SimulationData(MLRegression _toEvaluate, double _simTime)
    {
        toEvaluate = _toEvaluate;
        simTime = _simTime;
        fitnessCalculated = false;
    }
    
    /**
     * Get the fitness of the neural network to evaluate.
     * This method should be called by AlienEvaluator,
     * which will be woken up after a result is obtained.
     * 
     * @return the fitness value of evaluated neural network
     */
    public synchronized double getFitness()
    {
        while (!fitnessCalculated && !simTerminated)
        {
            try {
                this.wait();
                System.out.println("Thread woken");
            } catch (InterruptedException ex) {
                return Double.MIN_VALUE;
            }
        }
        if (fitnessCalculated)
            return fitness;
        else
        {
            return Double.MIN_VALUE;
        }
    }
    
    /**
     * Set the resultant fitness of current neural network.
     * This method should be called by simulator after simulation
     * for this task completes.
     * 
     * @param _fitness fitness value of neural network evaluated
     */
    public synchronized void setFitness(double _fitness)
    {
        System.out.println("Fitness set");
        fitness = _fitness;
        fitnessCalculated = true;
        this.notifyAll();
    }
    
    public synchronized void terminate()
    {
        simTerminated = true;
        this.notifyAll();
    }
    
    /**
     * Get the neural network to evaluate for this task.
     * @return the neural network to evaluate
     */
    public MLRegression getToEvaluate()
    {
        return toEvaluate;
    }
    
    public double getSimTime()
    {
        return simTime;
    }
}
