/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;

/**
 *  Helper class to help instantiate
 * an Alien Block as a Node, with PhysicsControl attached.
 * @author George Andersen <gla23@cam.ac.uk>
 */
public class AlienHelper {

    public static Geometry assembleBlock(Block block, Vector3f location) {
        return createLimb(block.collisionShapeType, block.width, block.height, block.length, location, block.mass);
    }
    public static void recursivelyAddBlocks(Block rootBlock, Block parentBlock, Geometry parentGeometry, AlienNode brain) {
        for (Block b : parentBlock.getConnectedLimbs()) {
            Geometry g = createLimb(b.collisionShapeType, b.width, b.height, b.length, parentGeometry.getControl(RigidBodyControl.class).getPhysicsLocation().add(b.getPosition()), b.mass);
            b.applyProperties(g);

            //printVector3f(b.getHingePosition());

            HingeJoint joint = joinHingeJoint(parentGeometry, g, parentGeometry.getControl(RigidBodyControl.class).getPhysicsLocation().add(b.getHingePosition()), b.hingeType);
            brain.attachChild(g);
            brain.joints.add(joint);
            brain.geometries.add(g);
            recursivelyAddBlocks(rootBlock, b, g, brain);
        }
    }
    public static Geometry createLimb(String meshShape, float width, float height, float length, Vector3f location, float mass) {

        Mesh mesh;
        if (meshShape.equals("Cylinder")) {
            mesh = new Cylinder(40, 40, width, length, true);
        } else if (meshShape.equals("Torus")) {
            mesh = new Torus(40, 40, width, length);
        } else if (meshShape.equals("Sphere")) {
            mesh = new Sphere(40, 40, width);
        } else {
            mesh = new Box(width, height, length);
        }
        Geometry limb = new Geometry("Limb", mesh);
        RigidBodyControl r;
        r = new RigidBodyControl(CollisionShapeFactory.createDynamicMeshShape(limb), mass);
        limb.addControl(r); //limb.setMesh(CollisionShapeFactory.createMeshShape(limb));
        r.setPhysicsLocation(location);
        //TODO: take texture information from Limb Block
        // and apply it to the created Node.
        limb.getControl(RigidBodyControl.class).setPhysicsLocation(location);
        return limb;
    }

    public static HingeJoint joinHingeJoint(Geometry A, Geometry B, Vector3f connectionPoint, String hingeType) {
        RigidBodyControl rigidBodyControlA = A.getControl(RigidBodyControl.class);
        RigidBodyControl rigidBodyControlB = B.getControl(RigidBodyControl.class);
        Vector3f pivotA = connectionPoint.add(rigidBodyControlA.getPhysicsLocation().mult(-1f));
        Vector3f pivotB = connectionPoint.add(rigidBodyControlB.getPhysicsLocation().mult(-1f));
        Vector3f axisA;
        Vector3f axisB;
        if (hingeType.equals("XAxis")) {
            axisA = Vector3f.UNIT_X;
            axisB = Vector3f.UNIT_X;
        } else if (hingeType.equals("YAxis")) {
            axisA = Vector3f.UNIT_Y;
            axisB = Vector3f.UNIT_Y;
        } else if (hingeType.equals("ZAxis")) {
            axisA = Vector3f.UNIT_Z;
            axisB = Vector3f.UNIT_Z;
        } else {
            axisA = Vector3f.UNIT_Z;
            axisB = Vector3f.UNIT_Z;
        }
        HingeJoint joint = new HingeJoint(rigidBodyControlA, rigidBodyControlB, pivotA, pivotB, axisA, axisB);
        if (hingeType.equals("XAxis")) {
            //joint.setLimit(0.5f, 1f, 0.5f,0.2f,0.5f);
        }
        return joint;
    }
    
    public static Vector3f getGeometryLocation(Geometry g) {
        return g.getControl(RigidBodyControl.class).getPhysicsLocation();
    }
    
    public static AlienNode assembleAlien(Alien a, Vector3f location) {
        AlienNode alienNode = new AlienNode();
        Block rootBlock = a.rootBlock;
        Geometry rootBlockGeometry = AlienHelper.assembleBlock(rootBlock, location);
        rootBlock.applyProperties(rootBlockGeometry);
        alienNode.attachChild(rootBlockGeometry);
        alienNode.geometries.add(rootBlockGeometry);
        recursivelyAddBlocks(rootBlock, rootBlock, rootBlockGeometry, alienNode);
        return alienNode;
    }
    
    
    
}
