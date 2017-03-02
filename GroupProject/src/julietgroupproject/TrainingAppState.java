/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import java.util.Queue;
import org.encog.ml.MLRegression;
import org.encog.util.obj.ObjectCloner;

/**
 * An AppState providing useful functionalities for alien training process.
 *
 * @author GeorgeLenovo
 */
public class TrainingAppState extends SimulatorAppState {

    // Simulation related fields
    protected Queue<SimulationData> queue;
    protected SimulationData currentSim;
    protected float simTimeLimit;
    // workaround weird bug of first simulation
    protected boolean isFirstSimulation = true;

    /**
     * Constructor, taking an Alien object as the Alien to be tested in this
     * simulator, the task queue with simulation data (neural network objects)
     * and the simulation speed.
     *
     * @param _alien The alien to be tested.
     * @param q The SimulationData queue. Must be thread-safe.
     * @param _simSpeed Simulation speed. Default should be 1.0.
     * @param _accuracy Simulation accuracy. 
     */
    public TrainingAppState(Alien _alien, Queue<SimulationData> q, double _simSpeed, double _accuracy) {

        super(_alien, _simSpeed, _accuracy);
        this.queue = q;
        this.isFixedTimeStep = false;
    }
    
    /**
     * 
     * @param _alien
     * @param q
     * @param _simSpeed
     * @param _accuracy
     * @param _fixedTimeStep 
     */
    public TrainingAppState(Alien _alien, Queue<SimulationData> q, double _simSpeed, double _accuracy, double _fixedTimeStep) {

        super(_alien, _simSpeed, _accuracy, _fixedTimeStep);
        this.queue = q;
    }

    /**
     * Start a new simulation. This method should not be called externally by
     * another thread.
     *
     * @param data the SimulationData object containing the ANN to be tested and
     * other parameters
     */
    protected void startSimulation(SimulationData data) {
        // turn physics back on
        this.physics.setEnabled(true);
        this.reset();
        this.currentSim = data;
        this.simTimeLimit = (float) data.getSimTime();
        this.currentAlienNode = instantiateAlien(this.alien, this.startLocation);
        AlienBrain brain;
        MLRegression nn = (MLRegression) ObjectCloner.deepCopy(data.getToEvaluate());
        if (isFixedTimeStep) {
            brain = new BasicAlienBrain(nn, this.physics.getPhysicsSpace().getAccuracy(), this.physics.getSpeed(), this.fixedTimeStep);
        } else {
            brain = new BasicAlienBrain(nn, this.physics.getPhysicsSpace().getAccuracy(), this.physics.getSpeed());
        }
        this.currentAlienNode.addControl(brain);
        this.simInProgress = true;
    }

    /**
     * Stop simulation and set fitness value.
     */
    protected void stopSimulation() {

        this.simInProgress = false;
        if (this.currentSim != null && !this.isFirstSimulation) {
            double fitness = this.calcFitness();
            this.currentSim.setFitness(fitness);
        }
        // turn physics off to save CPU time
        this.physics.setEnabled(false);
        this.isFirstSimulation = false;
    }

    /**
     * Perform initialisation and disable physics.
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);        
        this.reset();
        resetGravity();
        // turn physics off to save CPU time
        this.physics.setEnabled(false);
    }

    @Override
    public void reset() {
        super.reset();
        this.currentSim = null;
        this.simInProgress = false;
        this.simTimeLimit = 0.0f;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (this.currentSim != null) {
            // push unfinished simulation back to queue
            queue.add(this.currentSim);
        }
        this.currentSim = null;
    }

    @Override
    public void update(float tpf) {
        if (simInProgress) {
            if (this.isFixedTimeStep) {
                tpf = this.fixedTimeStep;
            }
            simTimeLimit -= tpf * physics.getSpeed();
            if (simTimeLimit < 0f) {
                // stop simulation and report result
                stopSimulation();
            }
        } else {
            // try to poll task from the queue
            if (toKill) {
                this.stateManager.detach(this);
            } else {
                SimulationData s;
                if (isFirstSimulation) {
                    s = this.queue.peek();
                } else {
                    s = this.queue.poll();
                }
                if (s != null) {
                    startSimulation(s);
                }
            }
        }
    }
}
