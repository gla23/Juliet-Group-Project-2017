package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.system.JmeContext;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Torus;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.ui.Picture;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import java.util.Random;
import java.util.LinkedList;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.shape.Sphere;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.Slider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import julietgroupproject.GUI.MainMenuController;

public class UIAppState extends DrawingAppState implements ActionListener {

    boolean runningPhysics = true;
    private MainMenuController myMainMenuController;
    float limbPower = 0.8f;
    float limbTargetVolcity = 2f;
    float time;
    float minBoxDimension = 0.4f;
    float minSphereDimension = 0.4f;
    Random rng = new Random();
    private Nifty nifty;
    private boolean wireMesh = true;
    private ChaseCamera chaseCam;
    private float horizontalAngle = 0;
    private float verticalAngle = 0;
    private float cameraZoom = 10;
    private boolean smoothCam = false;
    private String currentShape = "Box";
    private String currentHingeAxis ="A";
    Alien simpleAlien;
    Alien smallBlock;
    Alien flipper;
    Alien cuboid;
    AlienNode prevAlien;
    int[] jointKeys = { // Used for automatically giving limbs keys
        KeyInput.KEY_T, KeyInput.KEY_Y, // Clockwise and anticlockwise key pair for first limb created
        KeyInput.KEY_U, KeyInput.KEY_I, // and second pair
        KeyInput.KEY_G, KeyInput.KEY_H, // etc
        KeyInput.KEY_J, KeyInput.KEY_K,
        KeyInput.KEY_V, KeyInput.KEY_B,
        KeyInput.KEY_N, KeyInput.KEY_M};

    public UIAppState(Alien _alien, double _simSpeed) {
        super(_alien, _simSpeed);
    }

    public void removeAlien(AlienNode alienNode) {
        reset();
    }

    public void setCurrentShape(String shape) {
        currentShape = shape;
    }
    
    public void setCurrentHingeAxis(String axis) {
        currentHingeAxis = axis;
    }

    public void restartAlien() {
        if (prevAlien != null) {
            removeAlien(prevAlien);
            prevAlien = instantiateAlien(cuboid, new Vector3f(0f, 5f, -10f));
            setChaseCam(cuboid);
            setupKeys(prevAlien);
        }
    }
    
    public void setGravity(float newGrav) {
        physics.getPhysicsSpace().setGravity(new Vector3f(0,-newGrav,0));
        restartAlien();
        
    }

    public void resetAlien() {
        if (prevAlien != null) {
            removeAlien(prevAlien);
            prevAlien = null;
        }

    }

    public void toggleSmoothness() {
        smoothCam = !smoothCam;
        chaseCam.setSmoothMotion(smoothCam);


    }

    public void toggleWireMesh() {
        physics.setDebugEnabled(wireMesh);
        wireMesh = !wireMesh;
    }

    //Method for easily printing out vectors for debugging
    public void printVector3f(Vector3f vec) {
        System.out.println(vec);
    }

    public void setChaseCam(Alien shape) {
        if (chaseCam != null) {
            horizontalAngle = chaseCam.getHorizontalRotation();
            verticalAngle = chaseCam.getVerticalRotation();
            cameraZoom = chaseCam.getDistanceToTarget();
            chaseCam.setSmoothMotion(false);

        }
        //toggleSmoothness();
        chaseCam = new ChaseCamera(cam, shape.rootBlock.getGeometry(), inputManager);
        //toggleSmoothness();
        chaseCam.setSmoothMotion(smoothCam);
        if (smoothCam) {
            chaseCam.setDefaultDistance(20.6f);
        } else {
            chaseCam.setDefaultDistance(cameraZoom);
        }
        chaseCam.setDefaultHorizontalRotation(horizontalAngle);
        chaseCam.setDefaultVerticalRotation(verticalAngle);

        chaseCam.setMinVerticalRotation((float) (Math.PI) * -0.25f);
        //chaseCam.setTrailingEnabled(true);
        chaseCam.setChasingSensitivity(1f);

        chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_LEFT), new KeyTrigger(KeyInput.KEY_P));
        chaseCam.setTrailingRotationInertia(0.1f);
        chaseCam.setUpVector(new Vector3f(0, 1, 0));
        chaseCam.setZoomInTrigger(new KeyTrigger(KeyInput.KEY_LBRACKET));
        chaseCam.setZoomOutTrigger(new KeyTrigger(KeyInput.KEY_RBRACKET));
        chaseCam.setZoomSensitivity(10);
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setMaxDistance(150);
    }

    public void createNewBody() {
        if (prevAlien == null) {

            //Take the entries from text fields for limb size, do some error handling
            Slider widthField = nifty.getCurrentScreen().findNiftyControl("bodyWidthSlider", Slider.class);
            Slider heightField = nifty.getCurrentScreen().findNiftyControl("bodyHeightSlider", Slider.class);
            Slider lengthField = nifty.getCurrentScreen().findNiftyControl("bodyLengthSlider", Slider.class);
            Slider weightField = nifty.getCurrentScreen().findNiftyControl("bodyWeightSlider", Slider.class);

            float bodyWidth;
            float bodyHeight;
            float bodyLength;
            float bodyWeight;


            bodyWidth = widthField.getValue();


            bodyHeight = heightField.getValue();



            bodyLength = lengthField.getValue();

            bodyWeight = weightField.getValue();


            //Instantiate the new alien
            Vector3f pos = new Vector3f(-10 + 20 * rng.nextFloat(), -10 + 20 * rng.nextFloat(), -10 + 20 * rng.nextFloat());

            Block bodyBlock = new Block(pos, pos.mult(0.5f), bodyWidth, bodyHeight, bodyLength, currentShape, "ZAxis", bodyWeight);
            cuboid = new Alien(bodyBlock);
            prevAlien = instantiateAlien(cuboid, new Vector3f(0f, 5f, -10f));
            setChaseCam(cuboid);
            setupKeys(prevAlien);
        }

    }

    //To be run when right click on body, adds new limb with dimensions defined in text fields
    public void addLimb(Block block, Vector3f contactPt, Vector3f normal) {

        //Get rid of old alien on screen
        if (prevAlien != null) {
            removeAlien(prevAlien);
        }


        //Take the entries from the sliders for limb properties
        Slider widthField = nifty.getCurrentScreen().findNiftyControl("limbWidthSlider", Slider.class);
        Slider heightField = nifty.getCurrentScreen().findNiftyControl("limbHeightSlider", Slider.class);
        Slider lengthField = nifty.getCurrentScreen().findNiftyControl("limbLengthSlider", Slider.class);
        Slider weightField = nifty.getCurrentScreen().findNiftyControl("limbWeightSlider", Slider.class);
        Slider frictionField = nifty.getCurrentScreen().findNiftyControl("limbFrictionSlider", Slider.class);
        Slider strengthField = nifty.getCurrentScreen().findNiftyControl("limbStrengthSlider", Slider.class);
        Slider seperationField = nifty.getCurrentScreen().findNiftyControl("limbSeperationSlider", Slider.class);
        CheckBox symmeticBox = nifty.getCurrentScreen().findNiftyControl("symmetricCheckBox", CheckBox.class);
        
        float limbWidth;
        float limbHeight;
        float limbLength;
        float weight;
        float friction;
        float strength;
        float limbSeperation;
        boolean symmetric;
        // currentHingeAxis Will be either "X", "Y", "Z" or "A" for auto

        limbWidth = widthField.getValue();
        limbHeight = heightField.getValue();
        limbLength = lengthField.getValue();
        weight = weightField.getValue();
        friction = frictionField.getValue();
        strength = strengthField.getValue();
        limbSeperation = seperationField.getValue();
        symmetric = symmeticBox.isChecked();


        //Get the current shape from the selector
        myMainMenuController.setCurrentLimbShape();
        /*
        if (currentShape.equals("Box")) {
            // Rotate the x y z for making boxes rotate to the normal
            Vector3f whlVec = new Vector3f(limbWidth, limbHeight, limbLength);
            Matrix3f rotator = new Matrix3f();
            rotator.fromStartEndVectors(normal, new Vector3f(1, 0, 0));
            whlVec = rotator.mult(whlVec);
            limbWidth = Math.abs(whlVec.x);
            limbHeight = Math.abs(whlVec.y);
            limbLength = Math.abs(whlVec.z);
            
        } else if (currentShape.equals("Sphere")) {
            //TODO things for attahcing sphere
            
        } else if (currentShape.equals("Torus")) {
            //TODO things for attahcing torus
            
        } else if (currentShape.equals("Cylinder")) {
            //TODO things for attahcing cylinder
            
        }
        */
        
        //Find hinge and postion vectors given shape and click position
        //TODO fix this so that is gets the actual distance, and also make that distance correct when it is rotated
        Vector3f newHingePos = contactPt.add(normal.mult(-0.36f));
        Vector3f newPos = contactPt.add(normal.mult(Math.max(Math.max(limbLength, limbHeight), limbWidth) + limbSeperation));
        
        String axisToUse = "ZAxis";
        if (currentHingeAxis.equals("A")){
            if (Math.abs(normal.x)<Math.abs(normal.z)) {
                axisToUse = "XAxis";
            }
        } else {
            axisToUse = currentHingeAxis;
        }
        
        //Build the new limb
        Block limb = new Block(newPos, newHingePos, limbWidth, limbHeight, limbLength, currentShape, axisToUse, weight);
        Matrix3f rotation = new Matrix3f();
        rotation.fromStartEndVectors(new Vector3f(1, 0, 0), normal);

        limb.rotation = rotation;

        // Stores the normal the limb was created at in the limb for future use
        limb.setNormal(normal);

        //Add new limb to alien and instantiate
        block.addLimb(limb);
        prevAlien = instantiateAlien(cuboid, new Vector3f(0f, 5f, -10f));
        setChaseCam(cuboid);
        setupKeys(prevAlien);
    }

    public boolean saveAlien(String filename) {
        if (cuboid != null) {
            File f = new File(filename);
            try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f))) {
                o.writeObject(cuboid);
                return true;
            } catch (IOException ex) {
                Logger.getLogger(UIAppState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public boolean loadAlien(String filename) {
        File f = new File(filename);
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(f))) {
            Alien a = (Alien) o.readObject();
            if (a != null) {
                cuboid = a;
                resetAlien();
                prevAlien = instantiateAlien(cuboid, new Vector3f(0f, 5f, -10f));
                setChaseCam(cuboid);
                setupKeys(prevAlien);
                return true;
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(UIAppState.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        // turn the wireframe off
        physics.setDebugEnabled(false);

        // disable gravity initially
        setGravity(0.0f);

        myMainMenuController = new MainMenuController(this);

        stateManager.attach(myMainMenuController);

        //Set up nifty
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);

        nifty = niftyDisplay.getNifty();


        guiViewPort.addProcessor(niftyDisplay);
        nifty.fromXml("Interface/MainMenuLayout.xml", "begin", myMainMenuController);
        //nifty.setDebugOptionPanelColors(true); //un-comment this line to use DebugPanelColors and make sure Nifty is running correctly.

        //flyCam.setDragToRotate(true); //detaches camera from mouse unless you click/drag.a

        flyCam.setEnabled(false);

    }

    public void onAction(String string, boolean bln, float tpf) {

        // Controls the joints with keys in jointKeys
        if (currentAlienNode != null) {
            int numberOfJoints = Math.min(currentAlienNode.joints.size(), jointKeys.length / 2);

            for (int i = 0; i < numberOfJoints; i++) {

                if (("Alien joint " + ((Integer) i).toString() + " clockwise").equals(string)) {
                    if (bln) {
                        currentAlienNode.joints.get(i).getBodyA().activate();
                        currentAlienNode.joints.get(i).getBodyB().activate();
                        currentAlienNode.joints.get(i).enableMotor(true, 1 * limbTargetVolcity, limbPower);
                    } else {
                        currentAlienNode.joints.get(i).enableMotor(false, 0, 0);
                    }
                }
                if (("Alien joint " + ((Integer) i).toString() + " anticlockwise").equals(string)) {
                    if (bln) {
                        currentAlienNode.joints.get(i).getBodyA().activate();
                        currentAlienNode.joints.get(i).getBodyB().activate();
                        currentAlienNode.joints.get(i).enableMotor(true, -1 * limbTargetVolcity, limbPower);
                    } else {
                        currentAlienNode.joints.get(i).enableMotor(false, 0, 0);
                    }
                }
            }
        }
        if ("ToggleMesh".equals(string)) {
            if (!bln) {
                toggleWireMesh();
            }
        }


        //When right mouse button clicked, fire ray to see if intersects with body
        if ("AddLimb".equals(string) && !bln) {
            //Generate the ray from position of click
            CollisionResults results = new CollisionResults();
            Vector2f click2d = inputManager.getCursorPosition();
            Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();

            // Aim the ray from the clicked spot forwards.
            Ray ray = new Ray(click3d, dir);

            //Check for collisions with body recursively
            rootNode.collideWith(ray, results);
            CollisionResult collision = results.getClosestCollision();


            //If collided then generate new limb at collision point
            if (results.size() > 0) {
                Geometry geo = collision.getGeometry();
                Vector3f colpt = collision.getContactPoint();
                Vector3f pt = colpt.add(geo.getWorldTranslation().negate());
                Vector3f norm = collision.getContactNormal();

                //Find block assoicated with collision geometry
                Block block = null;

                LinkedList<Block> q = new LinkedList<Block>();
                q.push(cuboid.rootBlock);

                while (!q.isEmpty()) {
                    Block head = q.pop();
                    if (geo == head.getGeometry()) {
                        block = head;
                        break;
                    } else {
                        for (Block child : head.getConnectedLimbs()) {
                            q.push(child);
                        }
                    }
                }

                if (block != null) {
                    addLimb(block, pt, norm);
                }
            }
        }

    }

    public void setupKeys(AlienNode brain) {
        // Creates keyboard bindings for the joints with keys in jointKeys, limited by umber of joints or keys specified in jointKeys
        int numberOfJoints = Math.min(brain.joints.size(), jointKeys.length / 2);
        for (int i = 0; i < numberOfJoints; i++) {
            inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " clockwise", new KeyTrigger(jointKeys[2 * i]));
            inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " anticlockwise", new KeyTrigger(jointKeys[2 * i + 1]));
            inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " clockwise");
            inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " anticlockwise");
        }
        currentAlienNode = brain;

        //Add the key binding for the right click to add limb funtionality
        inputManager.addMapping("AddLimb", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(this, "AddLimb");
        inputManager.addMapping("ToggleMesh",new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addListener(this, "ToggleMesh");

    }
}
