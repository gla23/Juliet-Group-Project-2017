package julietgroupproject;

import java.util.List;
import java.util.Queue;
import org.encog.ml.MLRegression;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.util.obj.ObjectCloner;

/**
 * A framework for alien(ANN) training.
 *
 * @author Peter
 */
public class AlienTrainer extends Thread {

    //configurable constants
    /**
     * population size to use.
     */
    private final static int POPULATION = 30;
    /**
     * number of generations to perform before automatically saving.
     */
    private final static int ITERATIONS_BETWEEN_SAVES = 5; //
    //local state
    /**
     * the alien we are training to walk, contains the trainer itself.
     */
    private SavedAlien savedAlien;
    /**
     * list of slaves to kill when we have terminated.
     */
    private final List<SlaveSimulator> slaves;
    //volatile state (can be modified concurrently):
    /**
     * flag to indicate we should stop training after current generation.
     */
    private volatile boolean terminating = false;
    /**
     * the ConcurrentLinkedQueue containing simulation tasks, copying to
     * AlienEvalutor.
     */
    private Queue<SimulationData> simTasks;
    private volatile boolean isRunning;

    /**
     * Create a new trainer to train an alien to walk
     *
     * @param _savedAlien the alien to be taught to walk
     * @param _simTasks a queue onto which neural networks can be placed which
     * causes them to then be simulated and have their fitness evaluated
     */
    public AlienTrainer(SavedAlien _savedAlien, Queue<SimulationData> _simTasks, List<SlaveSimulator> _slaves) {

        savedAlien = _savedAlien;
        simTasks = _simTasks;
        slaves = _slaves;
        //if no population saved, create new population
        if (this.savedAlien.train == null) {
            resetTraining();
        }
    }

    /**
     * Start training from scratch.
     */
    private void resetTraining() {
        //indicate that the alien's save file should be backed up next save
        this.savedAlien.alienChanged();
        //generate a fresh population
        NEATPopulation pop = new NEATPopulation(savedAlien.inputCount, savedAlien.outputCount, POPULATION);
        //TODO configure trainer sensibly to speed up convergence/ get better results
        pop.setActivationCycles(4);
        pop.reset();
        /* score has its calculateScore method called concurrently
         * by the trainer. The calculateScore method puts requests
         * into the simTasks queue to be resolved externally.
         */
        AlienEvaluator score = new AlienEvaluator(simTasks);
        //creates the trainer to train this population
        this.savedAlien.train = NEATUtil.constructNEATTrainer(pop, score);
    }

    /**
     * Get the best neural network trained so far
     *
     * @param train trainer to get best genome from
     * @return best neural network so far
     */
    public static MLRegression getBestSoFar(EvolutionaryAlgorithm train) {
        if (train != null) {
            return (MLRegression) train.getCODEC().decode(train.getBestGenome());
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {

        // Remove stale TaskExecutors and other threading related transient fields
        // in trainer by serialising then deserialising.
        // the ObjectCloner uses ByteArrayInputStream/ByteArrayOutputStream
        // to do the serialisation/deserialisation in memory.
        // TODO: fix this hack to remove the one-off overhead
        isRunning = true;
        this.savedAlien.train = (EvolutionaryAlgorithm) ObjectCloner.deepCopy(this.savedAlien.train);
        EvolutionaryAlgorithm trainer = this.savedAlien.train;

        // Score function (AlienEvaluator) is also stored in serialised trainer,
        //but the reference to queue needs update everytime we start/restart training.
        ((AlienEvaluator) (trainer.getScoreFunction())).setQueue(simTasks);
        try {
            //loop until a stop is requested by calling terminateTraining
            do {
                //get fitnesses for the next generation
                trainer.iteration();

                //get information about the generation's performance

                //store information about the generation's performance
                this.savedAlien.addEntry(new GenerationResult(
                        trainer.getIteration(),
                        (float) trainer.getPopulation().getBestGenome().getScore(),
                        getBestSoFar(trainer)));

                //save if generation number a multiple of the save interval
                if (trainer.getIteration() % ITERATIONS_BETWEEN_SAVES == 0) {
                    AlienHelper.writeAlien(savedAlien);
                }
            } while (!terminating);
        } finally {
            //signal to Encog to close its threads and stop requesting fitnesses
            trainer.finishTraining();

            //write out current population state to file.
            AlienHelper.writeAlien(savedAlien);

            //close any slave simulators which were serving simulation requests
            if (slaves != null) {
                for (SlaveSimulator slave : this.slaves) {
                    slave.kill();
                }
                slaves.clear();
            }
            isRunning = false;
        }
    }

    /**
     * Ask the trainer to stop after the current generation.
     */
    public void terminateTraining() {

        //signal to stop after current generation completed
        terminating = true;
    }

    public boolean getIsRunning() {
        return isRunning;
    }
}