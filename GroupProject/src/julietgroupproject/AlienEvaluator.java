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

    private Queue<SimulationData> simTasks;
    double simTime;
    long returnedRequests = 0;

    @Override
    public boolean requireSingleThreaded() {
        return false;
    }

    @Override
    public boolean shouldMinimize() {
        return false;
    }

    @Override
    public double calculateScore(MLMethod nn) {
        SimulationData request = new SimulationData((MLRegression) nn, simTime);

        simTasks.add(request);

        double val = request.getFitness();
        System.out.println(++returnedRequests + " returned: " + val);
        return val;
    }

    public AlienEvaluator(Queue<SimulationData> _simTasks) {
        /**
         * Construct an alien evaluator which will put simulation tasks onto
         * _simTasks.
         *
         * @param _simTasks the thread-safe queue containing simulation tasks
         */
        simTasks = _simTasks;
        simTime = 5.0;
    }
}
