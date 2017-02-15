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

public class Brain extends AbstractControl {
    
    /*
     * Brain is created when the Alien class is instatiated in the simulation.
     * It is added to the rootNode of Alien as a Control,
     * which will control the alien using a provided ANN,
     * set by the setNN() method.
     * 
     * TODO: adapt to refactored Simulator class and
     * better encapsulation.
     * 
     * @author ss2324
     */


    public ArrayList<HingeJoint> joints = new ArrayList<HingeJoint>();
    public ArrayList<Geometry> geometries = new ArrayList<Geometry>();
    public Node nodeOfLimbGeometries;
    private MLRegression nn;
    private double[] nnInput;
    private static final float MAX_VELOCITY = 2f;
    private static final float MIN_VELOCITY = 0.1f;
    private static final float MAX_POWER = 1f;
    private static final int TICK_CYCLE = 10;
    private int tick = 0;

    public Vector3f getPosition(Node node) {
        return node.getControl(RigidBodyControl.class).getPhysicsLocation();
    }
    
    public MLRegression getNN() {
        return this.nn;
    }

    public void setNN(MLRegression nn) {
        this.nn = nn;
    }
    
    private void updateInput() {
        /*
         * fetch physical information into nnInput.
         */

        for (int i = 0; i < nnInput.length; i++) {
            
            // normalise input to range from 0 to 1
            // Angles are in radians. 0 indicates no
            // rotation, positive and negative values
            // denote clockwise and anticlockwise rotations
            // (PS: I'm not sure about which is which though)
            double in = ((double)joints.get(i).getHingeAngle())/(1.5 * Math.PI) + 0.5;
            if (in < 0.0) in = 0.0;
            if (in > 1.0) in = 1.0;
            nnInput[i] = in;
        }
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        // initialise nn input/output arrays
        if (spatial != null) {
            nnInput = new double[nn.getInputCount()];
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        /*
         * Updates state of alien.
         * Every TICK_CYCLE ticks this method will
         * pull new information from the simulation.
         * Current implementation uses angles of joints.
         * It will then feed the input to the ANN, computing
         * output and reflect back to the simulation.
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
