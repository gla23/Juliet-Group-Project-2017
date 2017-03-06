package julietgroupproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.encog.ml.MLRegression;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.util.obj.ObjectCloner;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;

public class LearnToBeAnAlienCommandLine {
    
    public static void main(String[] args) {
        
        //mute logger
        Logger physicslogger = Logger.getLogger(PhysicsSpace.class.getName());
        physicslogger.setUseParentHandlers(false);
        
        final int SIM_COUNT = Runtime.getRuntime().availableProcessors();
        final int SAVE_INTERVAL = 5;
        
        if (args.length != 3) {
            System.out.println("usage: LearnToBeAnAlienCommandLine <alien.sav> <number of iterations> <population size>");
        } else {
            File f = new File(args[0]);
            if (!f.exists()) {
                System.err.println("Alien save file does not exist!");
                return;
            }
            SavedAlien savedAlien;
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
                savedAlien = (SavedAlien)in.readObject();
                if (savedAlien.body == null) throw new ClassNotFoundException();
            } catch (IOException | ClassNotFoundException ex){
                System.err.println("Invalid saved alien");
                return;
            }
            // create aliens folder if not exist
            try {
                Files.createDirectories(Paths.get("aliens"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // take out alien body
            Alien alien = savedAlien.body;
            AlienNode currentAlienNode = traverseAlien(alien);
            
            int numOfIterations = 1;
            try {
                numOfIterations = Integer.parseInt(args[1]);
                if (numOfIterations < 1) { throw new NumberFormatException(); }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input for number of iterations");
                return;
            }
            
            int popSize = 30;
            try {
                popSize = Integer.parseInt(args[2]);
                if (popSize < 1) { throw new NumberFormatException(); }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input for population size");
                return;
            }
            
            
            AlienBrain tmpBrain = new AdvancedAlienBrain();
            savedAlien.inputCount = tmpBrain.getInputCount(currentAlienNode);
            savedAlien.outputCount = tmpBrain.getOutputCount(currentAlienNode);
            // task queue
            ConcurrentLinkedQueue<SimulationData> simulationQueue = new ConcurrentLinkedQueue<>();
            // slave simulators
            List<SlaveSimulator> slaves = new ArrayList<>();
            while (slaves.size() < SIM_COUNT) {
                Alien alienToTrain = (Alien) ObjectCloner.deepCopy(alien);
                SlaveSimulator toAdd = new SlaveSimulator(new TrainingAppState(alienToTrain, simulationQueue, 1.0f, 1f/60f, 1f / 60f));
                slaves.add(toAdd);
                toAdd.setShowSettings(false);
                AppSettings sett = new AppSettings(false);
                sett.setCustomRenderer(FastNullContext.class);
                sett.setFrameRate(30);
                toAdd.setSettings(sett);
                toAdd.start();
            }
            
            EvolutionaryAlgorithm trainer;
            //initialise trainer
            if (savedAlien.train != null) {
                //use existing trainer
                trainer = savedAlien.train;
            } else {
                NEATPopulation pop = new NEATPopulation(savedAlien.inputCount, savedAlien.outputCount, popSize);
                //TODO configure trainer sensibly to speed up convergence/ get better results
                pop.setActivationCycles(4);
                pop.reset();
                /* score has its calculateScore method called concurrently
                 * by the trainer. The calculateScore method puts requests
                 * into the simTasks queue to be resolved externally.
                 */
                AlienEvaluator score = new AlienEvaluator(simulationQueue);
                //creates the trainer to train this population
                trainer = NEATUtil.constructNEATTrainer(pop, score);
            }
            
            // training
            ((AlienEvaluator) (trainer.getScoreFunction())).setQueue(simulationQueue);
            System.out.println("Start training...");
            try {
                for (int i = 0; i < numOfIterations; ++i) {
                    trainer.iteration();
                    savedAlien.addEntry(new GenerationResult(
                            trainer.getIteration(),
                            (float) trainer.getPopulation().getBestGenome().getScore(),
                            (MLRegression) trainer.getCODEC().decode(trainer.getBestGenome())));
                    printGenerationResult(
                            trainer.getIteration(),
                            (float) trainer.getPopulation().getBestGenome().getScore());
                    if (i % SAVE_INTERVAL == SAVE_INTERVAL - 1) {
                        // save file
                        AlienHelper.writeAlien(savedAlien);
                    }
                }
            } finally {
                trainer.finishTraining();
                AlienHelper.writeAlien(savedAlien);
                for (SlaveSimulator slave : slaves) {
                    slave.kill();
                }
                System.out.println("Training finished!");
            }
        }
    }

    private static void printGenerationResult(int iteration, float score) {
        System.out.println("Iteration " + iteration + "\tFitness: " + score);
    }
    
    private static AlienNode traverseAlien(Alien a) {
        AlienNode node = new AlienNode();
        Block root = a.rootBlock;
        // fake geometries and joints
        Geometry g = new Geometry();
        HingeJoint j = new HingeJoint();
        traverseBlock(root,g,j,node);
        return node;
    }
    
    private static void traverseBlock(Block b, Geometry g, HingeJoint j, AlienNode n) {
        n.geometries.add(g);
        for (Block limb : b.getConnectedLimbs()) {
            n.joints.add(j);
            traverseBlock(limb,g,j,n);
        }
    }
}
