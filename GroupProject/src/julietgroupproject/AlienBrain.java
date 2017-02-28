/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;

/**
 * A controller that controls an alien contained in an AlienNode using neural
 * network.
 *
 * @author Sunny
 */
public abstract class AlienBrain extends AbstractControl {

    protected AlienNode alien;
    protected MLRegression nn;
    protected double[] nnInput;
    private MLData in;
    private MLData out;
    protected static final float MAX_VELOCITY = 1f;
    protected static final float MIN_VELOCITY = 0.1f;
    protected static final float MAX_POWER = 3f;
    
    private final boolean isFixedTimestep;
    private static final double DEFAULT_UPDATE_INTERVAL = 0.5;
    private double updateInterval = DEFAULT_UPDATE_INTERVAL;
    private final float timeStep;
    private final float accuracy;
    private float speed;
    private int tickCycle;
    private int tick = 0;

    public void setUpdateInterval(double _updateInterval) {
        this.updateInterval = _updateInterval;
    }
    public double getUpdateInterval() {
        return this.updateInterval;
    }

    /**
     * Construct an AlienBrain for alien controlling.
     *
     * @param _nn the neural network that controls alien
     * @param _accuracy the PhysicsSpace accuracy, i.e. length of
     * each physics tick
     * @param _speed the speed of simulation
     */
    public AlienBrain(MLRegression _nn, float _accuracy, float _speed, double _updateInterval) {

        this.nn = _nn;
        this.accuracy = _accuracy;
        this.isFixedTimestep = false;
        this.speed = _speed;
        this.timeStep = 0.0f;
        this.updateInterval = _updateInterval;
        this.tickCycle = (int) (this.updateInterval / (double)this.accuracy);
    }
    
    public AlienBrain(MLRegression _nn, float _accuracy, float _speed) {

        this(_nn,_accuracy,_speed,DEFAULT_UPDATE_INTERVAL);
    }
    
    public AlienBrain(MLRegression _nn, float _accuracy, float _speed, float _timeStep, double _updateInterval) {

        this.nn = _nn;
        this.accuracy = _accuracy;
        this.isFixedTimestep = true;
        this.speed = _speed;
        this.timeStep = _timeStep;
        this.updateInterval = _updateInterval;
    }

    public AlienBrain(MLRegression _nn, float _accuracy, float _speed, float _timeStep) {

        this(_nn,_accuracy,_speed,_timeStep,DEFAULT_UPDATE_INTERVAL);
    }
    
    protected abstract void updateInput();
    protected abstract void updateOutput(double[] nnOutput);
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        // initialise nn input/output arrays
        if (spatial != null) {
            nnInput = new double[nn.getInputCount()];
            in = new BasicMLData(nn.getInputCount());
            out = new BasicMLData(nn.getOutputCount());
            if (spatial instanceof AlienNode) {
                this.alien = (AlienNode) spatial;
            }
        }
        this.tick = 0;
    }

    /**
     * Updates state of alien. Every tickCycle ticks this method will pull new
     * information from the simulation. Current implementation uses angles of
     * joints. It will then feed the input to the ANN, computing output and
     * reflect back to the simulation.
     */
    @Override
    protected void controlUpdate(float tpf) {

        if (nn == null) {
            System.err.println("No neural network set.");
            return;
        };

        if(this.isFixedTimestep) {
            tpf = this.timeStep;
        }
        tick += (tpf * this.speed / this.accuracy);
        if (tick >= this.tickCycle) {
            tick -= this.tickCycle;

            updateInput();
            this.in.setData(nnInput);
            this.out = this.nn.compute(in);
            double[] nnOutput = out.getData();
            updateOutput(nnOutput);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
