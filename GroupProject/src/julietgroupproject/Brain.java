package julietgroupproject;

import com.jme3.bullet.control.RigidBodyControl;
import java.util.ArrayList;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;

public class Brain extends AbstractControl {
    // Brain is created when the Alien class is instatiated in the simulation.
    //It has a list of the limbs (Geometries), joints (HingeJoints) and the graphical node all the geometries are attached to.
    // These can be used later e.g. by the neural network

    public ArrayList<HingeJoint> joints = new ArrayList<HingeJoint>();
    public ArrayList<Geometry> geometries = new ArrayList<Geometry>();
    public Node nodeOfLimbGeometries;
    private MLRegression nn;
    private double[] nnInput;
    private static final float MAX_VELOCITY = 2f;
    private static final float MIN_VELOCITY = 0.1f;
    private static final float MAX_POWER = 1f;
    private static final int TICK_CYCLE = 100;
    private int tick = 0;

    public Vector3f getPosition(Node node) {
        return node.getControl(RigidBodyControl.class).getPhysicsLocation();
    }

    public void still() {
        for (Geometry g : geometries) {
            g.getControl(RigidBodyControl.class).setAngularVelocity(Vector3f.ZERO);
        }
    }

    public void setNN(MLRegression nn) {
        this.nn = nn;
    }

    private void updateInput() {
        /*
         * fetch physical information into nnInput.
         */

        for (int i = 0; i < nnInput.length; i++) {
            nnInput[i] = (double) joints.get(i).getHingeAngle();
        }
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        // initialise nn input/output arrays
        if (spatial != null) {
            nnInput = new double[((BasicNetwork) (this.nn)).getInputCount()];
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        /*
         * 
         */

        if (nn == null) {
            System.err.println("No neural network set.");
            return;
        };

        tick++;

        if (tick == TICK_CYCLE) {
            tick = 0;
            
            updateInput();
            MLData in = new BasicMLData(this.nnInput);
            MLData out = this.nn.compute(in);
            double[] nnOutput = out.getData();

            for (int i = 0; i < joints.size(); i++) {
                HingeJoint j = joints.get(i);
                j.getBodyA().activate();
                j.getBodyB().activate();
                float v = MAX_VELOCITY * (float) (2 * (nnOutput[i] - 0.5));
                float p = MAX_POWER;
                if (Math.abs(v) < MIN_VELOCITY) {
                    // stop moving
                    j.enableMotor(false, 0f, 0f);
                    // debug
                    System.out.println("Suspending limb #" + i);
                } else {
                    j.enableMotor(true, v, p);
                    // debug
                    System.out.println("Moving limb #" + i + " with velocity " + v + ", power " + p);
                }

            }
        }


    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
