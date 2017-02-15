/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import java.util.Queue;

/**
 *
 * @author Sunny <ss2324@cam.ac.uk>
 */
public class TestSimulator extends SimpleApplication {

    private BulletAppState bulletAppState;
    private Queue<SimulationData> queue;
    private boolean waiting;
    private boolean toDisplay;
    
    public TestSimulator(Queue queue, boolean _toDisplay) {
        super();
        this.queue = queue;
        waiting = true;
        toDisplay = _toDisplay;
    }

    @Override
    public void simpleInitApp() {

        bulletAppState = new BulletAppState();
        
        // try changing BulletAppState.speed and Brain.TICK_CYCLE
        bulletAppState.setSpeed(100.0f);
        stateManager.attach(bulletAppState);


        // build an example alien
        Alien flipper;
        float flipperTranslation = 0.8f;
        Block rootBlock = new Block(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), 0.9f, 0.1f, 0.0f, "Torus", "ZAxis", 1.5f);
        Block legLeft = new Block(new Vector3f(-2.9f, 0.0f, 0.0f), new Vector3f(-1.3f, 0.0f, 0.0f), 0.7f, 0.1f, 0.6f, "Torus", "ZAxis", 2.2f);
        legLeft.setRotation(new Matrix3f(1f, 0f, 0f, 0f, 0f, -1f, 0f, 1f, 0f));
        Block legRight = new Block(new Vector3f(2.6f, 0.0f, 0.0f), new Vector3f(1.3f, 0.0f, 0.0f), 1.5f, 0.1f, 1.3f, "Box", "YAxis", 1.2f);
        Block flipper1 = new Block(new Vector3f(flipperTranslation, 0.0f, 3.6f), new Vector3f(flipperTranslation, 0.0f, 1.3f), 0.6f, 0.1f, 2.1f, "Box", "XAxis", 1f);
        Block flipper2 = new Block(new Vector3f(flipperTranslation, 0.0f, -3.6f), new Vector3f(flipperTranslation, 0.0f, -1.3f), 0.6f, 0.1f, 2.1f, "Box", "XAxis", 1f);
        Block head = new Block(new Vector3f(-2.0f, 0.0f, 0.0f), new Vector3f(-1.3f, 0.0f, 0.0f), 0.5f, 0.5f, 0.5f, "Cylinder", "ZAxis", 1f);
        rootBlock.addLimb(legRight);
        rootBlock.addLimb(legLeft);
        legLeft.addLimb(flipper1);
        legLeft.addLimb(flipper2);
        flipper = new Alien(rootBlock);

        if (!toDisplay)
        {
            stateManager.attach(new SimulatorAppState(flipper));
        }
        else
        {
            stateManager.attach(new DrawingSimulatorAppState(flipper));
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!stateManager.getState(SimulatorAppState.class).isRunningSimulation()) {
            SimulationData s = this.queue.poll();
            if (s != null) {
                System.out.println(Thread.currentThread().getId()+ ": running simulation!");
                waiting = false;
                stateManager.getState(SimulatorAppState.class).startSimulation(s);
            } else {
                if (!waiting)
                {
                    System.out.println(Thread.currentThread().getId()+ ": waiting for simulation data!");
                    waiting = true;
                }
            }
        }

    }
}
