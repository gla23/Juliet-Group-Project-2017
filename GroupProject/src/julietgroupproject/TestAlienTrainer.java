/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.system.JmeContext;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Sunny <ss2324@cam.ac.uk>
 */
public class TestAlienTrainer {
    
    
    public static void main(String[] args) {
        Queue<SimulationData> q = new ConcurrentLinkedQueue<>();
        AlienTrainer trainer = new AlienTrainer(0.5,"test.pop",q);
        TestSimulator sim = new TestSimulator(q);
        //sim.start(JmeContext.Type.Headless);
        sim.start();
        trainer.run();
    }
}
