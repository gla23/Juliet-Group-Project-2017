/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import java.util.Queue;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.MLRegression;

/**
 *
 * @author Peter
 */
public class AlienEvaluator implements CalculateScore{
    private Queue<SimulationData> simTasks;
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
        SimulationData request = new SimulationData((MLRegression)nn, simTime); 
        
        simTasks.add(request);
        
        return request.getFitness();
    }
    
    
    public AlienEvaluator(Queue<SimulationData> _simTasks)
    {
        simTasks = _simTasks;
        simTime = 5.0;
    }
}
