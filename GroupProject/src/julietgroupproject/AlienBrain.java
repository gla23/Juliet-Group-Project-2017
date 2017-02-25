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
    private int tickCycle = 100;
    private int tick = 0;

    public int getTickCycle() {
        return tickCycle;
    }

    public void setTickCycle(int _tickCycle) {
        this.tickCycle = _tickCycle;
    }

    /**
     * Construct an AlienBrain for alien controlling.
     *
     * @param _nn the neural network that controls alien
     */
    public AlienBrain(MLRegression _nn) {

        this.nn = _nn;
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
            double in = ((double) this.alien.joints.get(i).getHingeAngle()) / (1.5 * Math.PI) + 0.5;
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

        tick++;

        if (tick >= this.tickCycle) {
            tick = 0;

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
