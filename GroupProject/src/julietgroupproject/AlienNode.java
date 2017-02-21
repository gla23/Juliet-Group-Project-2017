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

public class AlienNode extends Node {
    

    public ArrayList<HingeJoint> joints = new ArrayList<HingeJoint>();
    public ArrayList<Geometry> geometries = new ArrayList<Geometry>();
    
}
