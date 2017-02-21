/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import java.util.Queue;
import org.encog.ml.MLRegression;

/**
 * SimulatorAppState provides basic functionality of a alien simulator without
 * rendering.
 *
 * @author Sunny <ss2324@cam.ac.uk>
 */
public class SimulatorAppState extends AbstractAppState {

    // resources from app
    protected SimpleApplication app;
    protected Node rootNode;
    protected AssetManager assetManager;
    protected AppStateManager stateManager;
    protected InputManager inputManager;
    protected ViewPort viewPort;
    protected BulletAppState physics;
    // simulation related fields
    protected Queue<SimulationData> queue;
    protected Alien alien;
    protected Node simRoot;
    protected Node scene;
    protected Node alienRoot;
    public static final int DEFAULT_UPDATE_CYCLE = 100;
    protected int nnUpdateCycle;
    protected float simSpeed;
    private float originalSpeed;
    protected SimulationData currentSim;
    protected AlienNode currentAlienBrain;
    // flags
    private volatile boolean toKill;
    protected boolean simInProgress;
    protected float simTimeLimit;
    // World related
    protected Vector3f startLocation = Vector3f.ZERO;
    protected Geometry floorGeometry;

    public SimulatorAppState(Alien alien, Queue<SimulationData> q, double _simSpeed) {
        /*
         * Constructor, taking an Alien object as the Alien
         * to be tested in this simulator, the task queue with
         * simulation data (neural network objects) and the
         * simulation speed.
         * 
         * @param alien The alien to be tested.
         * @param q The SimulationData queue. Must be thread-safe.
         * @param simSpeed Simulation speed. Default is 1.0.
         */
        this.alien = alien;
        this.queue = q;
        this.simSpeed = (float)_simSpeed;
        
        this.nnUpdateCycle = (int)(DEFAULT_UPDATE_CYCLE / this.simSpeed);
    }
    
    public void setToKill(boolean _toKill) {
        //might need to watch for concurrent
        this.toKill = _toKill;
    }
    
    public boolean getToKill() {
        return this.toKill;
    }

    public void startSimulation(SimulationData data) {
        
        // turn physics back on
        this.physics.setEnabled(true);
        this.reset();
        this.currentSim = data;
        this.simTimeLimit = (float) data.getSimTime();
        this.currentAlienBrain = instantiateAlien(this.alien, this.startLocation, data.getToEvaluate());
        this.simInProgress = true;
    }

    protected void stopSimulation() {
        /*
         * Stop simulation.
         */
        this.simInProgress = false;
        if (this.currentSim != null) {
            synchronized (this.currentSim) {
                double fitness = this.calcFitness();
                this.currentSim.setFitness(fitness);
            }
            System.out.println("Stopping simulation! " + this.currentSim.toString());
        }
        // turn physics off to save CPU time
        this.physics.setEnabled(false);
    }

    protected double calcFitness() {
        /*
         * Calculate fitness for the current simulated
         * alien.
         */
        if (this.currentAlienBrain != null) {
            Vector3f pos = AlienHelper.getGeometryLocation(this.currentAlienBrain.geometries.get(0));
            double fitness = pos.x;
            if(Double.isNaN(fitness)){ System.err.println(pos); throw new RuntimeException("bad fitness"); };
            return fitness;
        } else {
            throw new RuntimeException("No simulation running! Cannot compute fitness.");
        }
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.app = (SimpleApplication) app; // can cast Application to something more specific
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = this.app.getStateManager();
        this.inputManager = this.app.getInputManager();
        this.viewPort = this.app.getViewPort();
        this.physics = this.stateManager.getState(BulletAppState.class);
        // turn physics off to save CPU time
        this.physics.setEnabled(false);
        this.originalSpeed = this.physics.getSpeed();
        this.physics.setSpeed(simSpeed);
        //reset();
    }

    public void reset() {

        this.currentAlienBrain = null;
        this.currentSim = null;
        this.simInProgress = false;
        this.simTimeLimit = 0.0f;

        if (this.simRoot != null) {
            this.physics.getPhysicsSpace().removeAll(simRoot);
            this.simRoot.removeFromParent();
        }
        this.simRoot = new Node("SimulatorRoot");
        this.initialiseWorld();
        this.rootNode.attachChild(simRoot);
    }

    protected void initialiseWorld() {

        Box floorBox = new Box(140, 1f, 140);
        floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setLocalTranslation(0, -5, 0);
        floorGeometry.addControl(new RigidBodyControl(0));
        simRoot.attachChild(floorGeometry);
        physics.getPhysicsSpace().add(floorGeometry);
    }

    protected AlienNode instantiateAlien(Alien a, Vector3f location, MLRegression nn) {
        /*
         * Spawn a new alien at a specified location.
         */

        AlienNode alienNode = new AlienNode();

        Block rootBlock = a.rootBlock;
        Geometry rootBlockGeometry = AlienHelper.assembleBlock(rootBlock, location);
        rootBlock.applyProperties(rootBlockGeometry);
        alienNode.attachChild(rootBlockGeometry);
        alienNode.geometries.add(rootBlockGeometry);

        AlienHelper.recursivelyAddBlocks(rootBlock, rootBlock, rootBlockGeometry, alienNode);

        physics.getPhysicsSpace().addAll(alienNode);
        simRoot.attachChild(alienNode);
        AlienBrain b = new AlienBrain(nn);
        b.setTickCycle(nnUpdateCycle);
        alienNode.addControl(b);

        return alienNode;
    }

    public boolean isRunningSimulation() {
        return simInProgress;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.physics.getPhysicsSpace().removeAll(simRoot);
        this.simRoot.removeFromParent();
        this.currentAlienBrain = null;
        if (this.currentSim != null) {
            // push unfinished simulation back to queue
            queue.add(this.currentSim);
        }
        this.currentSim = null;
        this.physics.setSpeed(originalSpeed);
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    // Note that update is only called while the state is both attached and enabled.
    @Override
    public void update(float tpf) {

        if (simInProgress) {
            simTimeLimit -= tpf * physics.getSpeed();
            if (simTimeLimit < 0f) {
                // stop simulation and report result
                stopSimulation();
            }
        } else {
            // try to poll task from the queue
            if (toKill)
            {
                this.stateManager.detach(this);
            } else {
                SimulationData s;
            s = this.queue.peek();
            if (s != null) {
                System.out.println(Thread.currentThread().getId() + ": starting simulation!");
                stateManager.getState(SimulatorAppState.class).startSimulation(s);
            }
            }
        }
    }
}
