/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.JmeContext;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 *
 * @author Sunny <ss2324@cam.ac.uk>
 */
public class TestAlienTrainer {
    
    
    public static void main(String[] args) {
        // Mute the PhysicsSpace logger
        Logger physicslogger = Logger.getLogger(PhysicsSpace.class.getName());
        physicslogger.setUseParentHandlers(false);
        
        Queue<SimulationData> q = new ConcurrentLinkedQueue<>();
        AlienTrainer trainer = new AlienTrainer(0.5,"test.pop",q,4,4);
        TestSimulator sim0 = new TestSimulator(q, true);
        TestSimulator sim1 = new TestSimulator(q, false);
        TestSimulator sim2 = new TestSimulator(q, false);
        
        /*
        TestSimulator sim3 = new TestSimulator(q, false);
        TestSimulator sim4 = new TestSimulator(q, false);
        TestSimulator sim5 = new TestSimulator(q, false);
        TestSimulator sim6 = new TestSimulator(q, false);
        TestSimulator sim7 = new TestSimulator(q, false);
        */
        //sim.start(JmeContext.Type.Headless);
        sim0.start();
        sim1.start(JmeContext.Type.Headless);
        sim2.start(JmeContext.Type.Headless);
        //sim3.start(JmeContext.Type.Headless);
        //sim4.start(JmeContext.Type.Headless);
        //sim5.start(JmeContext.Type.Headless);
        //sim6.start(JmeContext.Type.Headless);
        //sim7.start(JmeContext.Type.Headless);
        
        trainer.run();
    }
}
