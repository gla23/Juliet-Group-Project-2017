/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import java.util.Random;
import org.encog.ml.MLRegression;
import org.encog.ml.data.basic.BasicMLData;

/**
 *
 * @author Peter
 */
public class SimulationHandler {
    private Random rng = new Random();
    static int i = 0;
    public double startSimulation(MLRegression nn, double time)  //TODO implement
    {
        i ++;
        //System.out.println(i);
        /*try
        {
            Thread.sleep(40);
        }
        catch (InterruptedException e)
        {
            //do nothing
        }*/
        double input = rng.nextDouble();
        double[] arr = new double[1];
        arr[0] = input;
        double output = nn.compute(new BasicMLData(arr)).getData(0);
        double fitness = 1.0 - (Math.abs(0.5 - output));
        return fitness;
    }
}
