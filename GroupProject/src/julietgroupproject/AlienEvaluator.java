package julietgroupproject;

import java.util.Queue;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.MLRegression;

/**
 * A class evaluating a neural network by experiment in simulation.
 *
 * @author Peter
 */
public class AlienEvaluator implements CalculateScore {

    private Queue<SimulationData> simTasks;    //queue shared with simulators to make requests for neural networks to be evaluated.
    public final static double simTime = 20.0; //time for which all simulations are to be run, in in-engine seconds.

    /**
     * Check whether only one score request can be made at a time.
     *
     * @return true iff only one score should be requested at once
     */
    @Override
    public boolean requireSingleThreaded() {
        return false; //when set to false, trainer makes up to eight requests at a time
    }

    /**
     * Check whether the objective is to minimise the result of calculateScore.
     *
     * @return true iff the score should be minimised
     */
    @Override
    public boolean shouldMinimize() {
        return false; //when set to false, trainer tries to maximise score, i.e. distance travelled along x axis.
    }

    /**
     * Make a request for a neural network to be evaluated to determine its
     * fitness. Blocks until simulation complete.
     *
     * @param _nn the neural network to be evaluated
     * @return the score for _nn
     */
    @Override
    public double calculateScore(MLMethod _nn) {

        //build request to send for simulation
        SimulationData request = new SimulationData((MLRegression) _nn, simTime);

        //add the simulation request to the queue
        simTasks.add(request);

        //return the fitness: this call blocks until the fitness has been set by a simulator.
        return request.getFitness();
    }

    /**
     * Construct an alien evaluator which will put simulation tasks onto
     * _simTasks.
     *
     * @param _simTasks the thread-safe queue on which to push simulation
     * requests
     */
    public AlienEvaluator(Queue<SimulationData> _simTasks) {
        simTasks = _simTasks;
    }
}
