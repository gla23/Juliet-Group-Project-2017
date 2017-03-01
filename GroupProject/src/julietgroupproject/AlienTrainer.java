package julietgroupproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Queue;
import org.encog.ml.MLRegression;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;
import org.encog.util.Format;

/**
 * A framework for alien(ANN) training.
 *
 * @author Peter
 */
public class AlienTrainer extends Thread {

    private EvolutionaryAlgorithm train; //Manages training
    private SavedAlien savedAlien;
    private final int popCount = 30; //population size to use
    private volatile boolean terminating = false;
    private volatile List<SlaveSimulator> slaves;
    private final int iterationsBetweenSaves = 20;

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

        train = NEATUtil.constructNEATTrainer(this.savedAlien.pop, score);
        OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
        speciation.setCompatibilityThreshold(1);
        train.setSpeciation(new OriginalNEATSpeciation());
    }

    private void resetTraining() {
        System.out.println("Resetting training");
        this.savedAlien.alienChanged();
        this.savedAlien.pop = new NEATPopulation(savedAlien.inputCount, savedAlien.outputCount, popCount);
        this.savedAlien.pop.setActivationCycles(4);
        this.savedAlien.pop.reset();
        System.out.println("Population reset");
    }

    public MLRegression getBestSoFar()
    {
        return (MLRegression)train.getCODEC().decode(train.getBestGenome());
    }
    
    @Override
    public void run() {

        try {
            do {
                
                this.train.iteration(); //perform the next training iteration.#

                if (this.train.getIteration() % iterationsBetweenSaves == 0)
                {
                    AlienHelper.writeAlien(savedAlien);
                }
                
                int iterationNumber = this.train.getIteration();
                double populationFitness = this.savedAlien.pop.getBestGenome().getScore();
                    
                this.savedAlien.addEntry(new GenerationResult(iterationNumber, (float) populationFitness, getBestSoFar()));
            } while (!terminating);
        } finally {
            this.train.finishTraining();
        }

        //write out current population state to file.
        AlienHelper.writeAlien(savedAlien);
        
        if (slaves != null)
        {
            for (SlaveSimulator slave : this.slaves) {
                slave.kill();
            }
            slaves.clear();
            slaves = null;
        }
    }

    public void terminateTraining(List<SlaveSimulator> _slaves) {
        slaves = _slaves;
        terminating = true;
    }
}