package julietgroupproject;

import java.util.List;
import java.util.Queue;
import org.encog.ml.MLRegression;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;

/**
 * A framework for alien(ANN) training.
 *
 * @author Peter
 */
public class AlienTrainer extends Thread {

    //configurable constants
    private final static int POPULATION = 30;              //population size to use
    private final static int ITERATIONS_BETWEEN_SAVES = 5; //number of generations to perform before automatically saving
    
    //local state
    private EvolutionaryAlgorithm train; //manages training
    private SavedAlien savedAlien;       //the alien we are training to walk

    //volatile state (can be modified concurrently):
    private volatile boolean terminating = false; //flag to indicate we should stop training after current generation
    private volatile List<SlaveSimulator> slaves; //list of slaves to kill when we have terminated
    
    /**
     * Create a new trainer to train an alien to walk
     *
     * @param _savedAlien the alien to be taught to walk
     * @param _simTasks a queue onto which neural networks can be placed which
     * causes them to then be simulated and have their fitness evaluated
     */
    public AlienTrainer(SavedAlien _savedAlien, Queue<SimulationData> _simTasks) {

        savedAlien = _savedAlien;

        //if no population saved, create new population
        if (this.savedAlien.pop == null) {
            resetTraining();
        }

        /* score has its calculateScore method called concurrently
         * by the trainer. The calculateScore method puts requests
         * into the simTasks queue to be resolved externally.
         */
        AlienEvaluator score = new AlienEvaluator(_simTasks);

        //creates the trainer to train this population
        train = NEATUtil.constructNEATTrainer(this.savedAlien.pop, score);
    }

    /**
     * Start training from scratch.
     */
    private void resetTraining() {
        //indicate that the alien's save file should be backed up next save
        this.savedAlien.alienChanged();
        //generate a fresh population
        this.savedAlien.pop = new NEATPopulation(savedAlien.inputCount, savedAlien.outputCount, POPULATION);
        //TODO ?
        this.savedAlien.pop.setActivationCycles(4);
        this.savedAlien.pop.reset();
    }

    /**
     * Get the best neural network trained so far
     * 
     * @return best neural network so far
     */
    public MLRegression getBestSoFar() {
        return (MLRegression) train.getCODEC().decode(train.getBestGenome());
    }

    @Override
    public void run() {

        try {
            //loop until a stop is requested by calling terminateTraining
            do {
                //get fitnesses for the next generation
                this.train.iteration();

                //get information about the generation's performance

                //store information about the generation's performance
                this.savedAlien.addEntry(new GenerationResult(
                        this.train.getIteration(),
                        (float) this.savedAlien.pop.getBestGenome().getScore(),
                        getBestSoFar()));

                //save if generation number a multiple of the save interval
                if (this.train.getIteration() % ITERATIONS_BETWEEN_SAVES == 0) {
                    AlienHelper.writeAlien(savedAlien);
                }
            } while (!terminating);
        } finally {
            //signal to Encog to close its threads and stop requesting fitnesses
            this.train.finishTraining();

            //write out current population state to file.
            AlienHelper.writeAlien(savedAlien);

            //close any slave simulators which were serving simulation requests
            if (slaves != null) {
                for (SlaveSimulator slave : this.slaves) {
                    slave.kill();
                }
                slaves.clear();
                slaves = null;
            }
        }
    }

    /**
     * Ask the trainer to stop after the current generation.
     * 
     * @param _slaves a list of the slaves serving simulation requests to be closed
     * once all requests have been answered
     */
    public void terminateTraining(List<SlaveSimulator> _slaves) {
        //take copy of slaves to be terminated
        slaves = _slaves;
        
        //signal to stop after current generation completed
        terminating = true;
    }
}