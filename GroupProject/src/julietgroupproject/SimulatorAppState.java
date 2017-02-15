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
    private SimulationData currentSim;
    private Brain currentAlienBrain;
    private boolean simInProgress;
    private long simStartTime;
    private long simTimeLimit;
    private Vector3f startLocation = Vector3f.ZERO;
    
    protected Geometry floorGeometry;

    public SimulatorAppState(Alien alien) {
        /*
         * Constructor, taking an Alien object as the Alien
         * to be tested in this simulator.
         * 
         * @param alien The alien to be tested.
         */
        this.alien = alien;
    }

    public void startSimulation(SimulationData data) {
        this.reset();
        
        this.currentSim = data;
        this.simTimeLimit = (long) (1000000000 * data.getSimTime());
        this.currentAlienBrain = instantiateAlien(this.alien, this.startLocation, data.getToEvaluate());
        this.simStartTime = System.nanoTime();
        this.simInProgress = true;
    }

    private void stopSimulation() {

        this.simInProgress = false;
        if (this.currentSim != null) {
            synchronized (this.currentSim) {
                this.currentSim.setFitness(this.calcFitness());
                System.out.println("Stopping simulation! " + this.currentSim.toString());
            }
        }
    }

    private double calcFitness() {
        if (this.currentAlienBrain != null) {
            return (double) AlienHelper.getGeometryLocation(this.currentAlienBrain.geometries.get(0)).x;
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
        
        //reset();
    }

    public void reset() {

        this.currentAlienBrain = null;
        this.currentSim = null;
        this.simInProgress = false;
        this.simStartTime = 0L;
        this.simTimeLimit = 0L;

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
        //floorGeometry.setMaterial(grassMaterial);
        floorGeometry.setLocalTranslation(0, -5, 0);
        floorGeometry.addControl(new RigidBodyControl(0));
        ////////
        //floorGeometry.getMesh().scaleTextureCoordinates(new Vector2f(40,40));
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
            synchronized(this.currentSim){
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

        if (simInProgress && ((System.nanoTime() - simStartTime) > simTimeLimit)) {
            // stop simulation and report result
            stopSimulation();
        }
    }
}
