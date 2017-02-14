/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.MLRegression;

/**
 *
 * @author Peter
 */
public class AlienEvaluator implements CalculateScore{
    private SimulationHandler simHandler;
    double simTime;
    
    @Override
    public boolean requireSingleThreaded()
    {
        return false;
    }
    
    @Override
    public boolean shouldMinimize()
    {
        return false;
    }
    
    @Override
    public double calculateScore(MLMethod nn)
    {
        return simHandler.startSimulation((MLRegression)nn, simTime);
    }
    
    
    public AlienEvaluator()
    {
        simHandler = new SimulationHandler();
        simTime = 5.0;
    }
}
