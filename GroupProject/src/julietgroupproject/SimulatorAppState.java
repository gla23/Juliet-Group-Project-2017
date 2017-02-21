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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.encog.ml.MLRegression;

/**
 * SimulatorAppState provides basic functionality of a alien simulator without
 * rendering.
 *
 * @author Sunny <ss2324@cam.ac.uk>
 */
public class SimulatorAppState extends AbstractAppState {

    // Resources from app
    protected SimpleApplication app;
    protected Node rootNode;
    protected AssetManager assetManager;
    protected AppStateManager stateManager;
    protected InputManager inputManager;
    protected ViewPort viewPort;
    protected BulletAppState physics;
    // Physics Fields
    protected Alien alien;
    protected AlienNode currentAlienNode;
    protected Node simRoot;
    protected Node scene;
    // Physics Timing Fields
    public static final int DEFAULT_UPDATE_CYCLE = 100;
    protected int nnUpdateCycle;
    protected double simSpeed;
    private float originalSpeed;
    // Flags
    protected volatile boolean toKill;
    protected boolean simInProgress;
    // World related
    final protected Vector3f startLocation = Vector3f.ZERO;
    protected Geometry floorGeometry;

    public SimulatorAppState(Alien _alien, double _simSpeed) {
        this.simSpeed = _simSpeed;
        this.alien = _alien;
        this.nnUpdateCycle = (int) (DEFAULT_UPDATE_CYCLE / this.simSpeed);
    }

    public void setToKill(boolean _toKill) {
        //might need to watch for concurrent
        this.toKill = _toKill;
    }

    public boolean getToKill() {
        return this.toKill;
    }

    protected double calcFitness() {
        /*
         * Calculate fitness for the current simulated
         * alien.
         */
        if (this.currentAlienNode != null) {
            Vector3f pos = AlienHelper.getGeometryLocation(this.currentAlienNode.geometries.get(0));
            double fitness = pos.x;
            if (Double.isNaN(fitness)) {
                System.err.println(pos);
                throw new RuntimeException("bad fitness");
            };
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
        this.originalSpeed = this.physics.getSpeed();
        this.physics.setSpeed((float) simSpeed);
    }

    public void reset() {

        this.currentAlienNode = null;
        this.simInProgress = true;

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

    protected AlienNode instantiateAlien(Alien a, Vector3f location) {
        /*
         * Spawn a new alien at a specified location.
         */
        AlienNode alienNode = AlienHelper.assembleAlien(a, location);

        physics.getPhysicsSpace().addAll(alienNode);
        simRoot.attachChild(alienNode);

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
        this.currentAlienNode = null;
        this.physics.setSpeed(originalSpeed);
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    // Note that update is only called while the state is both attached and enabled.
    @Override
    public void update(float tpf) {
        if (toKill) {
            this.stateManager.detach(this);
        }
    }
}
