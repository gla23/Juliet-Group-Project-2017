/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.util.BufferUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.FloatBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.encog.ml.MLRegression;

/**
 * Helper class to help instantiate an Alien Block as a Node, with
 * PhysicsControl attached.
 *
 * @author George Andersen
 */
public class AlienHelper {

    /**
     * Assemble a limb geometry from definition in a Block. This is a wrapper
     * function of createLimb.
     *
     * @param block the Block containing the limb to add
     * @param location the world location of limb
     * @return the assembled Geometry
     */
    public static Geometry assembleBlock(Block block, Vector3f location) {
        return createLimb(block.collisionShapeType, block.width, block.height, block.length, location, block.mass,block.rotation,block.rotationForYPR);
    }

    /**
     * Assemble a full alien from rootBlock by adding limbs and joints
     * recursively.
     *
     * @param rootBlock the root block containing definition of the alien
     * @param parentBlock the parent block containing limbs
     * @param parentGeometry the parent geometry described by parentBlock
     * @param alienNode the AlienNode that all limb geometries should belong to
     */
    public static void recursivelyAddBlocks(Block rootBlock, Block parentBlock, Geometry parentGeometry, AlienNode alienNode) {
        for (Block b : parentBlock.getConnectedLimbs()) {
            Geometry g = createLimb(b.collisionShapeType, b.width, b.height, b.length, parentGeometry.getControl(RigidBodyControl.class).getPhysicsLocation().add(b.getPosition()), b.mass,b.rotation,b.rotationForYPR);
            b.applyProperties(g);

            //printVector3f(b.getHingePosition());

            HingeJoint joint = joinHingeJoint(parentGeometry, g, parentGeometry.getControl(RigidBodyControl.class).getPhysicsLocation().add(b.getHingePosition()), b.hingeType);
            alienNode.attachChild(g);
            alienNode.joints.add(joint);
            alienNode.geometries.add(g);
            recursivelyAddBlocks(rootBlock, b, g, alienNode);
        }
    }

    public static Geometry createLimb(String meshShape, float width, float height, float length, Vector3f location, float mass, Matrix3f rotation, Matrix3f rotationForYPR) {

        Mesh mesh;
        Vector3f moveOriginOfRotation = Vector3f.ZERO;
        if (meshShape.equals("Cylinder")) {
            mesh = new Cylinder(40, 40, length, 2*width, true);
        } else if (meshShape.equals("Torus")) {
            mesh = new Torus(40, 40, length, width-length);
        } else if (meshShape.equals("Sphere")) {
            mesh = new Sphere(40, 40, 1f);
        } else {
            mesh = new Box(width, height, length);
            moveOriginOfRotation = new Vector3f(width,0f,0f);
        }
        mesh = transformMesh(rotation, rotationForYPR, moveOriginOfRotation, mesh);
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

    /**
     * Assemble an alien from an abstract alien definition.
     *
     * @param a the abstract alien definition
     * @param location the location where the instantiated alien should locate
     * @return AlienNode containing the assembled alien
     */
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
    
    public static Mesh transformMesh(Matrix3f rotation, Matrix3f rotationForYPR, Vector3f moveOrigin, Mesh mesh){        
        // Get the buffer from the mesh and put in array of vectors
        VertexBuffer vb = mesh.getBuffer(Type.Position);
        FloatBuffer vbData = (FloatBuffer)vb.getData();
        int elements = vb.getNumElements();
        Vector3f[] points;// = new Vector3f[elements];
        points = BufferUtils.getVector3Array(vbData);
        
        for (int i = 0; i < elements; i++) {
            points[i] = points[i].add(moveOrigin);
            points[i] = rotationForYPR.mult((points[i]));
            points[i] = points[i].add(new Vector3f(-moveOrigin.x,-moveOrigin.y,-moveOrigin.z));
            points[i] = rotation.mult((points[i]));
            BufferUtils.setInBuffer(points[i], vbData, i);
            
        }
        
        //Update it:
        vb.setUpdateNeeded();
        return mesh;
    }
    
    public static SavedAlien readAlien(String name)
    {
        File f = new File("aliens/" + name + "/" + name + "_current.sav");
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(f))) {
            return (SavedAlien) o.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(UIAppState.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static boolean writeAlien(SavedAlien alien)
    {
        if (alien == null)
            return false;
        File f = new File("aliens/" + alien.getName() + "/" + alien.getName() + "_current.sav");
        f.getParentFile().mkdirs();
        
        
        //backup the existing file if the population was reset.
        if (alien.getHasBeenReset())
        {
            for (File toRename : f.getParentFile().listFiles()) {
                if (toRename.getPath().contains(alien.getName() + "_current.sav")) {
                    DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
                    Date dateobj = new Date();
                    File target = new File(toRename.getPath().substring(0, toRename.getPath().length() - 4) + df.format(dateobj) + ".sav");
                    toRename.renameTo(target);
                }
            }
                
        }
        
        
        try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f))) {
            o.writeObject(alien);

            alien.alienSaved();
            
            return true;
        } catch (IOException ex) {
            Logger.getLogger(UIAppState.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}