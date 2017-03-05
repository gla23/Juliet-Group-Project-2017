package julietgroupproject;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.io.Serializable;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;

/**
 * A controller that controls an alien contained in an AlienNode using a neural
 * network.
 *
 * @author Sunny
 */
public abstract class AlienBrain extends AbstractControl implements Serializable {

    //movement restrictions:
    protected static final float MAX_VELOCITY = 3f;    //limits relative movement speed of joints
    protected static final float MIN_VELOCITY = 0.1f;  //speeds smaller than this are taken as 0
    protected static final float MAX_POWER = 4f;       //limits torque applied through joints
    //local state:
    protected transient AlienNode alien;    //the physical manifestation of the alien being controlled
    protected transient MLRegression nn;    //the neural network used to make movement decisions
    protected transient double[] nnInput;   //the inputs which will be fed to the neural network next update
    private transient MLData in;            //a packed up structure to contain the neural network's input
    private transient MLData out;           //a packed up structure to contain the neural network's output
    //timing information:
    private transient final boolean isFixedTimestep;
    private transient final float timeStep;
    private transient final float accuracy;
    private transient float speed;
    private transient int tickCycle;
    protected transient int tick = 0;
    /*
     * The updateInterval is the time (in seconds) between each recalculation of
     * muscle movements by the neural network.
     */
    private static final double DEFAULT_UPDATE_INTERVAL = 0.5;
    private double updateInterval = DEFAULT_UPDATE_INTERVAL;

    public void setUpdateInterval(double _updateInterval) {
        this.updateInterval = _updateInterval;
    }

    public double getUpdateInterval() {
        return this.updateInterval;
    }

    /**
     * Default contructor, used for computing NN parameters, e,g, input count.
     */
    public AlienBrain() {
        this(1f / 60f, 1.0f, 1.0);
    }

    /**
     * Construct an AlienBrain for alien controlling.
     *
     * @param _nn the neural network that controls alien
     * @param _accuracy the PhysicsSpace accuracy, i.e. length of each physics
     * tick
     * @param _speed the speed of simulation
     */
    public AlienBrain(float _accuracy, float _speed, double _updateInterval) {

        this.accuracy = _accuracy;
        this.isFixedTimestep = false;
        this.speed = _speed;
        this.timeStep = 0.0f;
        this.updateInterval = _updateInterval;
        this.tickCycle = (int) (this.updateInterval / (double) this.accuracy);
    }

    public AlienBrain(float _accuracy, float _speed) {

        this(_accuracy, _speed, DEFAULT_UPDATE_INTERVAL);
    }

    public AlienBrain(float _accuracy, float _speed, float _timeStep, double _updateInterval) {

        this.accuracy = _accuracy;
        this.isFixedTimestep = true;
        this.speed = _speed;
        this.timeStep = _timeStep;
        this.updateInterval = _updateInterval;
    }

    public AlienBrain(float _accuracy, float _speed, float _timeStep) {

        this(_accuracy, _speed, _timeStep, DEFAULT_UPDATE_INTERVAL);
    }

    protected abstract void updateInput();

    protected abstract void updateOutput(double[] nnOutput);

    public abstract int getInputCount(AlienNode a);

    public int getInputCount() {
        if (this.alien == null) {
            throw new UnsupportedOperationException("Control not attached"
                    + "to an AlienNode");
        } else {
            return getInputCount(this.alien);
        }
    }

    public abstract int getOutputCount(AlienNode a);

    public int getOutputCount() {
        if (this.alien == null) {
            throw new UnsupportedOperationException("Control not attached"
                    + "to an AlienNode");
        } else {
            return getOutputCount(this.alien);
        }
    }

    public MLRegression getNN() {
        return this.nn;
    }

    public void setNN(MLRegression _nn) {
        this.nn = _nn;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        // initialise nn input arrays and MLData references
        if (spatial != null) {
            if (spatial instanceof AlienNode) {
                this.alien = (AlienNode) spatial;
                nnInput = new double[getInputCount()];
                in = new BasicMLData(getInputCount());
                out = new BasicMLData(getOutputCount());
            } else {
                throw new UnsupportedOperationException("Can only add an instantiated "
                        + "AlienBrain to an AlienNode.");
            }
            this.tick = 0;
        }
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
        }

        if (this.isFixedTimestep) {
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
