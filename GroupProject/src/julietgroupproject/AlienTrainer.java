/*
 * Encog(tm) Java Examples v3.4
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-examples
 *
 * Copyright 2008-2016 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package julietgroupproject;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.encog.ml.CalculateScore;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.hyperneat.substrate.SubstrateFactory;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;
import org.encog.util.Format;

/**
 * This program demonstrates HyperNEAT.
 * 
 * The objective is to distinguish a large object from a small object in a two-
 * dimensional visual field. Because the same principle determines the
 * difference between small and large objects regardless of their location in
 * the retina, this task is well suited to testing the ability of HyperNEAT to
 * discover and exploit regularities.
 * 
 * This program will display two rectangles, one large, and one small. The
 * program seeks to place the red position indicator in the middle of the larger
 * rectangle. The program trains and attempts to gain the maximum score of 110.
 * Once training is complete, you can run multiple test cases and see the
 * program attempt to find the center.
 * 
 * One unique feature of HyperNEAT is that the resolution can be adjusted after
 * training has occured. This allows you to efficiently train on a small data
 * set and run with a much larger.
 * 
 */
public class AlienTrainer{

	private NEATPopulation pop;
	private EvolutionaryAlgorithm train;
        private double targetError = 110.0;
        private String filename;
        
        private int inputCount = 1;
        private int outputCount = 1;
        private int popCount = 500;
        
        public void load()
        {
            pop = null;
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
        
        public void save()
        {
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
                            
                out.writeObject(pop);
                
                out.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
                System.out.println("Error saving to file.");
            }
        }
        
        public AlienTrainer(double _targetError, String _filename)
        {
            targetError = _targetError;
            filename = _filename;
            
            //try to load from saved file
            this.load();
            
            //if unsucessful, create new population
            if (this.pop == null) {
                resetTraining();
            }
            
            AlienEvaluator score = new AlienEvaluator();
            
            train = NEATUtil.constructNEATTrainer(pop, score);
            OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
            speciation.setCompatibilityThreshold(1);
            train.setSpeciation(speciation = new OriginalNEATSpeciation());
            // train.setThreadCount(1);
        }
        
	public void resetTraining() {
		pop = new NEATPopulation(inputCount, outputCount, popCount);
		pop.setActivationCycles(4);
		pop.reset();
                System.out.println("Population reset");
	}

	public static void main(String[] args) {
            if (args.length != 2)
            {
                System.out.println("Usage: AlienTrainer <filename> <error>");
                return;
            }
            
            double error = Double.parseDouble(args[1]);
            
            AlienTrainer trainer = new AlienTrainer(error, args[0]);
            trainer.run();
                
	}

	public void run() {

            BufferedInputStream IO = new BufferedInputStream(System.in);

            System.out.println(targetError);
            
            do  {
                try {
                    if (IO.available() > 0){break;}
                } catch(IOException e) {break;}
                this.train.iteration();
                System.out.println("Error: " + Format.formatDouble(this.train.getError(), 2));
                System.out.println("Iterations: " + Format.formatInteger(this.train.getIteration()));
            } while (this.train.getError() > targetError);

            this.train.finishTraining();
                
            this.save();
	}

	
}