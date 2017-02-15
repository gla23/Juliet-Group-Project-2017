package julietgroupproject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Queue;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;
import org.encog.util.Format;

public class AlienTrainer{

	private NEATPopulation pop; //The population being trained
	private EvolutionaryAlgorithm train; //Manages training
        private double targetError; //If error becomes lower than this, stop.
        private String filename; //File to save/load population from.
        
        private int inputCount; //Number of inputs to the NN
        private int outputCount; //Number of outputs from the NN
        private int popCount = 500; //population size to use
        
        private void load()
        {
            pop = null;
            
            //read in a serialized population from a file
            try
            {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
            
                pop = (NEATPopulation)in.readObject();
                
                in.close();
            }
            catch(IOException e)
            {
                System.out.println("Error reading from file: creating new population");
            }
            catch(ClassNotFoundException e)
            {
                System.out.println("Error reading from file: creating new population");
            }
        }
        
        private void save()
        {
            //TODO: can cause stack overflow as NEATPopulation's Serialize is not implemented properly
            
            //serialize the population and write it to a file.
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
                            
                out.writeObject(pop);
                
                out.close();
            }
            catch(IOException e)
            {
                System.out.println("Error saving to file.");
            }
        }
        
        public AlienTrainer(double _targetError, String _filename, Queue<SimulationData> _simTasks, int _inputCount, int _outputCount)
        {
            targetError = _targetError;
            filename = _filename;
            
            inputCount = _inputCount;
            outputCount = _outputCount;
            
            //try to load from saved file
            this.load();
            
            //if unsucessful, create new population
            if (this.pop == null) {
                resetTraining();
            }
            
            /* score has its calculateScore method called concurrently
             * by the trainer. The calculateScore method puts requests
             * into the simTasks queue to be resolved externally.
             */
            AlienEvaluator score = new AlienEvaluator(_simTasks);
            
            train = NEATUtil.constructNEATTrainer(pop, score);
            OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
            speciation.setCompatibilityThreshold(1);
            train.setSpeciation(new OriginalNEATSpeciation());
        }
        
	private void resetTraining() {
		pop = new NEATPopulation(inputCount, outputCount, popCount);
		pop.setActivationCycles(4);
		pop.reset();
                System.out.println("Population reset");
	}

	public void run() {

            //checks if the user has provided input.
            BufferedInputStream IO = new BufferedInputStream(System.in);

            System.out.println(targetError);
            
            do  {
                try {
                    if (IO.available() > 0){break;} //break if the user entered anything.
                } catch(IOException e) {break;}
                this.train.iteration(); //perform the next training iteration.
                
                //print statistics
                System.out.println("Error: " + Format.formatDouble(this.train.getError(), 2)); //TODO: Error always returns 1
                System.out.println("Iterations: " + Format.formatInteger(this.train.getIteration()));
            } while (this.train.getError() > targetError);

            this.train.finishTraining();
            
            //write out current population state to file.
            this.save();
	}

	
}