package julietgroupproject;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import java.io.Serializable;
import java.util.LinkedList;

public class Block implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final float DEFAULT_FRICTION = 5f;
    
    private Vector3f pos; // Position of limb relative to parent Block
    private Vector3f hingePos; // Position of hinge connecting this block and the parent, relative to parent
    public float width;
    public float height;
    public float length;
    public float mass;
    public float friction;
    public Matrix3f rotation = Matrix3f.IDENTITY;
    public Matrix3f rotationForYRP = Matrix3f.IDENTITY;
    public String collisionShapeType;
    public String hingeType;
    public boolean restrictHinges = false;
    public float hingeMax;
    public float hingeMin;
    public boolean createdBySymetric = false;
    private LinkedList<Block> connectedLimbs = new LinkedList<Block>(); // List of Blocks that this is the parent of
    private transient Geometry geo;
    private Vector3f normal = new Vector3f(1,0,0);
    
    

    public Block(Vector3f pos, Vector3f hingePos, float width, float height, float length, String collisionShapeType, String hingeType,float mass) {
        this.pos = pos;
        this.hingePos = hingePos;
        this.width = width;
        this.height = height;
        this.length = length;
        this.collisionShapeType = collisionShapeType;
        this.hingeType = hingeType;
        this.mass = mass;
        
        this.friction = DEFAULT_FRICTION;
    }
    
    public Block(Vector3f pos, Vector3f hingePos, float width, float height, float length, String collisionShapeType, String hingeType,float mass, float hingeMin, float hingeMax) {
        this.pos = pos;
        this.hingePos = hingePos;
        this.width = width;
        this.height = height;
        this.length = length;
        this.collisionShapeType = collisionShapeType;
        this.hingeType = hingeType;
        this.mass = mass;
        this.hingeMax = hingeMax;
        this.hingeMin = hingeMin;
        this.restrictHinges = true;
        
        this.friction = DEFAULT_FRICTION;
    }
    
    // rubbish copy - don't use
    public Block(Block copy) {
        this.pos = copy.pos;
        this.hingePos = copy.hingePos;
        this.width = copy.width;
        this.height = copy.height;
        this.length = copy.length;
        this.collisionShapeType = copy.collisionShapeType;
        this.hingeType = copy.hingeType;
        this.mass = copy.mass;
        this.normal = copy.normal;
        this.rotation = copy.rotation;
        this.rotationForYRP = copy.rotationForYRP;
    }
    
    public void applyProperties(Geometry g){
        // This function is used when the block is instantiated, used to make the properties of the geometry the same as the block.
        // As we add more properties for the blocks, this functions should be edited to apply them when the block is created
        this.geo = g;
        
        //Still trying to figure out the rotation here
        /*geo.getControl(RigidBodyControl.class).setPhysicsRotation(new Matrix3f(0.1f,0.2f,0.3f,0.4f,0.5f,0.6f,0.7f,0.8f,0.9f));
        geo.rotate(0.1f,0.4f,0.7f);*/
        
        
        // Set limb properties
        g.getControl(RigidBodyControl.class).setFriction(friction);
        //g.getControl(RigidBodyControl.class).setMass(mass);
    }
    
    public void setNormal(Vector3f norm) {
        normal = norm;
    }
    public Geometry getGeometry() {
        return geo;
    }
    
    public Vector3f getPosition() {
        return pos;
    }

    public void setPosition(Vector3f pos) {
        this.pos = pos;
    }

    public Vector3f getHingePosition() {
        return hingePos;
    }

    public void setHingePosition(Vector3f hingePos) {
        this.hingePos = hingePos;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public void setHingeType(String hingeType) {
        this.hingeType = hingeType;
    }

    public void setPos(Vector3f pos) {
        this.pos = pos;
    }

    public void setHingePos(Vector3f hingePos) {
        this.hingePos = hingePos;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public void setRotation(Matrix3f rotation) {
        this.rotation = rotation;
    }

    public void setCollisionShapeType(String collisionShapeType) {
        this.collisionShapeType = collisionShapeType;
    }

    public LinkedList<Block> getConnectedLimbs() {
        return connectedLimbs;
    }
    
    public void addLimb(Block limb) {
        connectedLimbs.add(limb);
    }
    
    public boolean removeDescendantBlock(Block descendant) {
        
        if (connectedLimbs.remove(descendant)) {
            return true;
        } else {
            for (Block child : connectedLimbs) {
                if(child.removeDescendantBlock(descendant)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static Block getParent(Block block, Block ancestor) {
        
        LinkedList<Block> children = ancestor.connectedLimbs;
        
        if (children.contains(block)) {
            return ancestor;
        } else {
            for (Block child : children) {
                Block parent = getParent(block, child);
                if (parent != null) {
                    return parent;
                }
            }
        }
        
        return null;
    }

}
