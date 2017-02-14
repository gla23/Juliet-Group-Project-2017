package julietgroupproject;

import com.jme3.bullet.control.RigidBodyControl;
import java.util.ArrayList;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

public class Brain {
    // Brain is created when the Alien class is instatiated in the simulation.
    //It has a list of the limbs (Geometries), joints (HingeJoints) and the graphical node all the geometries are attached to.
    // These can be used later e.g. by the neural network
    
    
    public ArrayList<HingeJoint> joints = new ArrayList<HingeJoint>();
    public ArrayList<Geometry> geometries = new ArrayList<Geometry>();
    public Node nodeOfLimbGeometries;
    
    public Vector3f getPosition(Node node) {
        return node.getControl(RigidBodyControl.class).getPhysicsLocation();
    }

    public void still() {
        for(Geometry g : geometries){
            g.getControl(RigidBodyControl.class).setAngularVelocity(Vector3f.ZERO);
        }
    }
}
