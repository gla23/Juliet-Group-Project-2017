package julietgroupproject;

import com.jme3.bullet.joints.HingeJoint;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.ArrayList;

/**
 * A Node containing all the geometries of an alien,
 * with two additional Lists of references to the joints and geometries
 * for convenience of interacting with the alien.
 */
public class AlienNode extends Node {
    public ArrayList<HingeJoint> joints = new ArrayList<>();
    public ArrayList<Geometry> geometries = new ArrayList<>();   
}
