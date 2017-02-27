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
public class AlienBrain extends AbstractControl {

    private AlienNode alien;
    private MLRegression nn;
    private double[] nnInput;
    private static final float MAX_VELOCITY = 2f;
    private static final float MIN_VELOCITY = 0.1f;
    private static final float MAX_POWER = 1f;
    
    private final boolean isFixedTimestep;
    public static final double DEFAULT_UPDATE_INTERVAL = 0.5;
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
    
    /**
     * Fetch physical information into nnInput.
     */
    private void updateInput() {
        for (int i = 0; i < nnInput.length - 1; i++) { //TODO class to describe input categories

            // normalise input to range from 0 to 1
            // Angles are in radians. 0 indicates no
            // rotation, positive and negative values
            // denote clockwise and anticlockwise rotations
            // (PS: I'm not sure about which is which though)
            double in = ((double) this.alien.joints.get(i).getHingeAngle()) / (2.0 * Math.PI) + 0.5;
            if (in < 0.0) {
                in = 0.0;
            }
            if (in > 1.0) {
                in = 1.0;
            }
            nnInput[i] = in;

        }


        double bearing = this.alien.geometries.get(0).getControl(RigidBodyControl.class).getPhysicsRotation().toAngles(null)[2];

        nnInput[nnInput.length - 1] = (bearing + Math.PI) / (2.0 * Math.PI);

        if ((bearing + Math.PI) / (2.0 * Math.PI) < 0.0 || (bearing + Math.PI) / (2.0 * Math.PI) > 1.0) {
            throw new RuntimeException("Angle normalisation incorrect");
        }
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        // initialise nn input/output arrays
        if (spatial != null) {
            nnInput = new double[nn.getInputCount()];
            if (spatial instanceof AlienNode) {
                this.alien = (AlienNode) spatial;
            }
        }
        this.tick = 0;
    }

    /**
     * Updates state of alien. Every TICK_CYCLE ticks this method will pull new
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
            MLData in = new BasicMLData(this.nnInput);
            MLData out = this.nn.compute(in);
            double[] nnOutput = out.getData();

            for (int i = 0; i < this.alien.joints.size(); i++) {
                HingeJoint j = this.alien.joints.get(i);
                j.getBodyA().activate();
                j.getBodyB().activate();
                float v = MAX_VELOCITY * (float) (2 * (nnOutput[i] - 0.5));
                float p = MAX_POWER;
                //System.out.println("applying torque to limb " + i + " with v:" + v + " p:" + p);
                if (Math.abs(v) < MIN_VELOCITY) {
                    // try to stop moving
                    j.enableMotor(true, 0f, 0f);
                    // debug
                    //System.out.println("Suspending limb #" + i);
                } else {
                    j.enableMotor(true, v, p);
                    // debug
                    //System.out.println("Moving limb #" + i + " with velocity " + v + ", power " + p);
                }

            }
        }


    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
