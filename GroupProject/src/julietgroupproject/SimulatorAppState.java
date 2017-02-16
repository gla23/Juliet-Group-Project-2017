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
    protected Alien alien;
    protected Node simRoot;
    protected Node scene;
    protected Node alienRoot;
    protected int nnUpdateCycle;
    protected SimulationData currentSim;
    protected Brain currentAlienBrain;
    protected boolean simInProgress;
    protected float simTimeLimit;
    protected Vector3f startLocation = Vector3f.ZERO;
    protected Geometry floorGeometry;

    public SimulatorAppState(Alien alien, int _nnUpdateCycle) {
        /*
         * Constructor, taking an Alien object as the Alien
         * to be tested in this simulator.
         * 
         * @param alien The alien to be tested.
         */
        this.alien = alien;
        this.nnUpdateCycle = _nnUpdateCycle;
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

    protected Brain instantiateAlien(Alien a, Vector3f location, MLRegression nn) {
        /*
         * Spawn a new alien at a specified location.
         */

        Brain b = new Brain();
        Node alienNode = new Node("Alien");

        Block rootBlock = a.rootBlock;
        Geometry rootBlockGeometry = AlienHelper.assembleBlock(rootBlock, location);
        rootBlock.applyProperties(rootBlockGeometry);
        alienNode.attachChild(rootBlockGeometry);
        b.geometries.add(rootBlockGeometry);

        AlienHelper.recursivelyAddBlocks(rootBlock, rootBlock, rootBlockGeometry, alienNode, b);

        physics.getPhysicsSpace().addAll(alienNode);
        simRoot.attachChild(alienNode);
        b.nodeOfLimbGeometries = alienNode;
        b.setNN(nn);
        b.setTickCycle(nnUpdateCycle);
        alienNode.addControl(b);

        return b;
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
            synchronized (this.currentSim) {
                this.currentSim.notifyAll();
            }
        }
        this.currentSim = null;
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
        }
    }
}
