package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.system.JmeContext;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.Slider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import julietgroupproject.GUI.MainMenuController;
import org.encog.ml.MLRegression;
import org.encog.util.obj.ObjectCloner;

public class UIAppState extends DrawingAppState implements ActionListener {

    boolean runningPhysics = true;
    private MainMenuController myMainMenuController;
    float limbPower = 0.8f;
    float limbTargetVolcity = 2f;
    float time;
    float minBoxDimension = 0.4f;
    float minSphereDimension = 0.4f;
    private Nifty nifty;
    private boolean wireMesh = true;
    private ChaseCamera chaseCam;
    private float horizontalAngle = 0;
    private float verticalAngle = 0;
    private float cameraZoom = 25;
    private boolean smoothCam = false;
    private String currentShape = "Box";
    private final int SIM_COUNT = 8;
    private static final int DEFAULT_FRAMERATE = 60;
    private List<SlaveSimulator> slaves = new ArrayList<>(SIM_COUNT);
    private Queue<SimulationData> simulationQueue = new ConcurrentLinkedQueue<>();
    private AlienTrainer trainer;
    private boolean editing = true;
    private SimulationData currentSim;
    private float simTimeLimit;
    private String currentHingeAxis = "A";
    private Geometry ghostLimb;
    private Geometry ghostLimb2;
    private Geometry delghost;
    private Material ghostMaterial;
    private Material ghostMaterial2;
    private Geometry arrowGeometry;
    private int speedUpFactor = 1000;
    private boolean shiftDown = false;
    private Node ghostRoot;
    private boolean isCollisionOccuring = false;
    int[] jointKeys = { // Used for automatically giving limbs keys
        KeyInput.KEY_T, KeyInput.KEY_Y, // Clockwise and anticlockwise key pair for first limb created
        KeyInput.KEY_U, KeyInput.KEY_I, // and second pair
        KeyInput.KEY_G, KeyInput.KEY_H, // etc
        KeyInput.KEY_J, KeyInput.KEY_K,
        KeyInput.KEY_V, KeyInput.KEY_B,
        KeyInput.KEY_N, KeyInput.KEY_M};

    public UIAppState(Alien _alien, double _simSpeed, double _accuracy) {
        super(_alien, _simSpeed, _accuracy);
    }

    public UIAppState(Alien _alien, double _simSpeed, double _accuracy, double _fixedTimeStep) {
        super(_alien, _simSpeed, _accuracy, _fixedTimeStep);
    }

    public void setTexture(int textno) {
        System.out.println("Setting texture to " + textno);
        removeAlien(currentAlienNode);
        alien.materialCode = textno;
        instantiateAlien(alien, this.startLocation);
        setChaseCam(this.currentAlienNode);
        setupKeys(this.currentAlienNode);
    }

    public int getTextureNo() {
        System.out.println(alien.getCode());
        return alien.getCode();
    }

    public int getSpeedUpFactor() {
        return this.speedUpFactor;
    }

    public void setSpeedUpFactor(int _speedUpFactor) {
        if (_speedUpFactor > 0) {
            this.speedUpFactor = _speedUpFactor;
        } else {
            throw new IllegalArgumentException("speed up factor must be a positive integer.");
        }
    }

    public void removeAlien(AlienNode alienNode) {
        if (alienNode != null) {
            this.physics.getPhysicsSpace().removeAll(alienNode);
            alienNode.removeFromParent();
        }
    }

    public void setCurrentShape(String shape) {
        currentShape = shape;
    }

    public void setCurrentHingeAxis(String axis) {
        currentHingeAxis = axis;
    }

    public void restartAlien() {
        if (currentAlienNode != null) {
            removeAlien(currentAlienNode);
            instantiateAlien(alien, startLocation);
            setChaseCam(currentAlienNode);
            setupKeys(currentAlienNode);
        }
    }

    public void setGravity(float newGrav) {
        physics.getPhysicsSpace().setGravity(new Vector3f(0, -newGrav, 0));
        restartAlien();
    }

    public void resetAlien() {
        reset();
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

    public Geometry makeGhostLimb(Block block) {

        Block copy = new Block(block);

        copy.width += 0.02;
        copy.length += 0.02;
        copy.height += 0.02;

        copy.setPosition(copy.getPosition().add(new Vector3f(-0.01f, -0.01f, -0.01f)));

        Geometry gl = AlienHelper.assembleBlock(copy, block.getGeometry().getWorldTranslation());
        ghostRoot.attachChild(gl);
        return gl;

    }

    public void relocateGhostLimb(Geometry gl, Block block, Vector3f contactPt, Vector3f normal) {
        //Take the entries from the sliders for limb properties
        Slider widthField = nifty.getCurrentScreen().findNiftyControl("limbWidthSlider", Slider.class);
        Slider heightField = nifty.getCurrentScreen().findNiftyControl("limbHeightSlider", Slider.class);
        Slider lengthField = nifty.getCurrentScreen().findNiftyControl("limbLengthSlider", Slider.class);
        Slider weightField = nifty.getCurrentScreen().findNiftyControl("limbWeightSlider", Slider.class);
        Slider frictionField = nifty.getCurrentScreen().findNiftyControl("limbFrictionSlider", Slider.class);
        Slider strengthField = nifty.getCurrentScreen().findNiftyControl("limbStrengthSlider", Slider.class);
        Slider seperationField = nifty.getCurrentScreen().findNiftyControl("limbSeperationSlider", Slider.class);
        CheckBox symmetricBox = nifty.getCurrentScreen().findNiftyControl("symmetricCheckBox", CheckBox.class);


        Slider rollSlider = nifty.getCurrentScreen().findNiftyControl("rollSlider", Slider.class);
        Slider yawSlider = nifty.getCurrentScreen().findNiftyControl("yawSlider", Slider.class);
        Slider pitchSlider = nifty.getCurrentScreen().findNiftyControl("pitchSlider", Slider.class);
        Slider jointPosSlider = nifty.getCurrentScreen().findNiftyControl("jointPosSlider", Slider.class);
        Slider jointRotSlider = nifty.getCurrentScreen().findNiftyControl("jointRotSlider", Slider.class);

        float limbWidth;
        float limbHeight;
        float limbLength;
        float limbSeperation;
        // currentHingeAxis Will be either "X", "Y", "Z" or "A" for auto

        limbWidth = widthField.getValue();
        limbHeight = heightField.getValue();
        limbLength = lengthField.getValue();
        limbSeperation = seperationField.getValue();


        //Get the current shape from the selector
        //myMainMenuController.setCurrentLimbShape();


        //Find hinge and postion vectors given shape and click position
        //TODO fix this so that is gets the actual distance, and also make that distance correct when it is rotated
        Vector3f newHingePos = contactPt.add(normal.mult(-0.36f));
        Vector3f newPos = contactPt.add(normal.mult(Math.max(Math.max(limbLength, limbHeight), limbWidth) + limbSeperation));

        String axisToUse = "ZAxis";
        if (currentHingeAxis.equals("A")) {
            if (Math.abs(normal.x) < Math.abs(normal.z)) {
                axisToUse = "XAxis";
            }
        } else {
            axisToUse = currentHingeAxis;
        }

        //Build the new limb
        Matrix3f rotation = new Matrix3f();
        rotation.fromStartEndVectors(new Vector3f(1, 0, 0), normal);

        //Mesh m = gl.getMesh();
        //AlienHelper.rotateMesh(rotation, m);
        gl.setLocalRotation(rotation);
        gl.setLocalTranslation(newPos.add(AlienHelper.getGeometryLocation(block.getGeometry())));
    }

    public Geometry addGhostLimb(Block block, Vector3f contactPt, Vector3f normal) {

        Block limb = createLimb(block, contactPt, normal);

        Geometry gl = AlienHelper.assembleBlock(limb, limb.getPosition().add(AlienHelper.getGeometryLocation(block.getGeometry())));
        Mesh m = gl.getMesh();
        AlienHelper.rotateMesh(limb.rotation.invert(), m);
        gl.setLocalRotation(limb.rotation);

        // check for collision

        GhostControl ghostControl = new GhostControl(CollisionShapeFactory.createDynamicMeshShape(gl));
        ghostControl.setApplyPhysicsLocal(true);
        gl.addControl(ghostControl);

        physics.getPhysicsSpace().add(ghostControl);
        gl.setMaterial(ghostMaterial);

        //System.out.println("OverlappingCount:" + ghostControl.getOverlappingCount());
        ghostRoot.attachChild(gl);

        return gl;
    }

    public void removeGhostLimb(Geometry gl) {
        if (gl != null) {
            GhostControl gc = gl.getControl(GhostControl.class);
            if (gc != null) {
                this.physics.getPhysicsSpace().remove(gc);
            }
            gl.removeFromParent();
        }

    }

    private boolean ghostCollisionCheck(Geometry gl) {
        if (gl != null) {
            GhostControl gc = gl.getControl(GhostControl.class);
            if (gc != null) {
                //System.out.println("Overlapping count:" + gc.getOverlappingCount());
                if (gc.getOverlappingCount() > 0) {
                    gl.setMaterial(ghostMaterial2);
                    return true;
                } else {
                    gl.setMaterial(ghostMaterial);
                }
            }
        }
        return false;
    }

    public void updateGhostLimb() {
        //removeGhostLimb(ghostLimb);
        //removeGhostLimb(ghostLimb2);

        if (alien != null && alien.rootBlock != null) {

            // only check collsion with solid objects
            CollisionResult collision = getCursorRaycastCollision(this.simRoot);

            //If collided then generate new limb at collision point
            if (collision != null) {

                Geometry geo = collision.getGeometry();
                Vector3f colpt = collision.getContactPoint();
                Vector3f pt = colpt.add(geo.getControl(RigidBodyControl.class).getPhysicsLocation().negate());
                Vector3f norm = collision.getContactNormal();

                //Find block assoicated with collision geometry
                Block block = null;

                LinkedList<Block> q = new LinkedList<>();
                q.push(alien.rootBlock);

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

                    if (!shiftDown) {
                        // remove deletion ghost
                        if (delghost != null) {
                            removeGhostLimb(delghost);
                            delghost = null;
                        }
                        if (ghostLimb == null) {
                            ghostLimb = addGhostLimb(block, pt, norm);
                        } else {
                            relocateGhostLimb(ghostLimb, block, pt, norm);
                        }
                        CheckBox symmetricBox = nifty.getCurrentScreen().findNiftyControl("symmetricCheckBox", CheckBox.class);
                        boolean symmetric = symmetricBox.isChecked();

                        if (symmetric) {
                            switch (block.collisionShapeType) {
                                case "Box":
                                    if (ghostLimb2 == null) {
                                        ghostLimb2 = addGhostLimb(block, pt.subtract(pt.project(collision.getContactNormal()).mult(2.0f)), norm.negate());
                                    } else {
                                        relocateGhostLimb(ghostLimb2, block, pt.subtract(pt.project(collision.getContactNormal()).mult(2.0f)), norm.negate());
                                    }
                                    break;
                                default:
                                    if (ghostLimb2 == null) {
                                        ghostLimb2 = addGhostLimb(block, pt.subtract(pt.project(Vector3f.UNIT_Z).mult(2.0f)), norm.subtract(norm.project(Vector3f.UNIT_Z).mult(2.0f)));
                                    } else {
                                        relocateGhostLimb(ghostLimb2, block, pt.subtract(pt.project(Vector3f.UNIT_Z).mult(2.0f)), norm.subtract(norm.project(Vector3f.UNIT_Z).mult(2.0f)));
                                    }
                                    break;

                            }
                        }
                    } else {
                        // remove adding limb ghosts
                        removeGhostLimb(ghostLimb);
                        removeGhostLimb(ghostLimb2);
                        ghostLimb = null;
                        ghostLimb2 = null;

                        if (delghost == null) {
                            delghost = makeGhostLimb(block);
                            delghost.setMaterial(ghostMaterial2);
                        }
                    }
                } else {
                    removeGhostLimb(ghostLimb);
                    removeGhostLimb(ghostLimb2);
                    ghostLimb = null;
                    ghostLimb2 = null;
                }
            }
        } else {
            removeGhostLimb(ghostLimb);
            removeGhostLimb(ghostLimb2);
            ghostLimb = null;
            ghostLimb2 = null;
        }
        this.isCollisionOccuring = ghostCollisionCheck(ghostLimb);
        this.isCollisionOccuring |= ghostCollisionCheck(ghostLimb2);

    }

    public void setChaseCam(AlienNode shape) {

        if (chaseCam != null) {
            horizontalAngle = chaseCam.getHorizontalRotation();
            verticalAngle = chaseCam.getVerticalRotation();
            cameraZoom = chaseCam.getDistanceToTarget();
            chaseCam.setSmoothMotion(false);
            //remove warnings about adding duplicate mappings.
            inputManager.deleteMapping("ChaseCamMoveLeft");
            inputManager.deleteMapping("ChaseCamMoveRight");
            inputManager.deleteMapping("ChaseCamToggleRotate");
        }
        //toggleSmoothness();
        chaseCam = new ChaseCamera(cam, shape.geometries.get(0), inputManager);

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
        chaseCam.setZoomInTrigger(new KeyTrigger(KeyInput.KEY_EQUALS));
        chaseCam.setZoomOutTrigger(new KeyTrigger(KeyInput.KEY_MINUS));
        chaseCam.setZoomSensitivity(10);
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setMaxDistance(150);

    }

    public void createNewBody() {
        if (this.currentAlienNode == null) {

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
            Vector3f pos = Vector3f.ZERO;


            Block bodyBlock = new Block(pos, pos.mult(0.5f), bodyWidth, bodyHeight, bodyLength, currentShape, "ZAxis", bodyWeight);
            int texturecode = alien != null ? alien.materialCode : 1;
            alien = new Alien(bodyBlock);
            alien.materialCode = texturecode;
            instantiateAlien(alien, startLocation);
            setChaseCam(this.currentAlienNode);
            setupKeys(this.currentAlienNode);
        }

    }

    private void createArrow() {
        Arrow directionArrow = new Arrow(new Vector3f(7, 0, 0));
        directionArrow.setLineWidth(5);
        arrowGeometry = new Geometry("Arrow", directionArrow);
        Material arrowMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        arrowMaterial.setColor("Color", ColorRGBA.Blue);
        arrowGeometry.setMaterial(arrowMaterial);
        arrowGeometry.setLocalTranslation(0, -3.5f, 0);
    }

    public void toggleArrow() {
        if (arrowGeometry == null) {
            createArrow();
        }
        if (simRoot.hasChild(arrowGeometry)) {
            simRoot.detachChild(arrowGeometry);
        } else {
            simRoot.attachChild(arrowGeometry);
        }
    }

    // Returns closest collision result after casting ray from cursor
    private CollisionResult getCursorRaycastCollision() {
        return getCursorRaycastCollision(this.rootNode);
    }

    private CollisionResult getCursorRaycastCollision(Node nodeToCollide) {
        //Generate the ray from position of click
        CollisionResults results = new CollisionResults();
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();

        // Aim the ray from the clicked spot forwards.
        Ray ray = new Ray(click3d, dir);

        //Check for collisions with body recursively
        nodeToCollide.collideWith(ray, results);
        return results.getClosestCollision();
    }

    public boolean checkRootNull() {
        return (currentAlienNode == null);
    }

    public void removeLimb(Block block) {
        if (block != null) {
            if (block == alien.rootBlock) {
                resetAlien();
            } else {
                alien.rootBlock.removeDescendantBlock(block);
                restartAlien();
            }
        }
    }

    // Used to get the block that is currently being specified by the options.
    // Used by add limb and add ghost limb so the ghost limb ghosts the same block that is going to be created
    public Block createLimb(Block block, Vector3f contactPt, Vector3f normal) {

        //Take the entries from the sliders for limb properties
        Slider widthField = nifty.getCurrentScreen().findNiftyControl("limbWidthSlider", Slider.class);
        Slider heightField = nifty.getCurrentScreen().findNiftyControl("limbHeightSlider", Slider.class);
        Slider lengthField = nifty.getCurrentScreen().findNiftyControl("limbLengthSlider", Slider.class);
        Slider weightField = nifty.getCurrentScreen().findNiftyControl("limbWeightSlider", Slider.class);
        Slider frictionField = nifty.getCurrentScreen().findNiftyControl("limbFrictionSlider", Slider.class);
        Slider strengthField = nifty.getCurrentScreen().findNiftyControl("limbStrengthSlider", Slider.class);
        Slider seperationField = nifty.getCurrentScreen().findNiftyControl("limbSeperationSlider", Slider.class);
        CheckBox symmetricBox = nifty.getCurrentScreen().findNiftyControl("symmetricCheckBox", CheckBox.class);


        Slider rollSlider = nifty.getCurrentScreen().findNiftyControl("rollSlider", Slider.class);
        Slider yawSlider = nifty.getCurrentScreen().findNiftyControl("yawSlider", Slider.class);
        Slider pitchSlider = nifty.getCurrentScreen().findNiftyControl("pitchSlider", Slider.class);
        Slider jointPosSlider = nifty.getCurrentScreen().findNiftyControl("jointPosSlider", Slider.class);
        Slider jointRotSlider = nifty.getCurrentScreen().findNiftyControl("jointRotSlider", Slider.class);

        float limbWidth;
        float limbHeight;
        float limbLength;
        float weight;
        float friction;
        float strength;
        float limbSeperation;
        boolean symmetric;
        float roll;
        float yaw;
        float pitch;
        float jointPositionFraction;
        float jointStartRotation;
        // currentHingeAxis Will be either "X", "Y", "Z" or "A" for auto

        limbWidth = widthField.getValue();
        limbHeight = heightField.getValue();
        limbLength = lengthField.getValue();
        weight = weightField.getValue();
        friction = frictionField.getValue();
        strength = strengthField.getValue();
        limbSeperation = seperationField.getValue();
        symmetric = symmetricBox.isChecked();
        roll = rollSlider.getValue();
        yaw = yawSlider.getValue();
        pitch = pitchSlider.getValue();
        jointPositionFraction = jointPosSlider.getValue();
        jointStartRotation = jointRotSlider.getValue();


        //Find hinge and postion vectors given shape and click position
        //TODO fix this so that is gets the actual distance, and also make that distance correct when it is rotated
        Vector3f newHingePos;
        Vector3f newPos;
        if (currentShape.equals("Cylinder")) {
            newPos = contactPt.add(normal.mult(limbWidth + limbSeperation));
        } else {
            newPos = contactPt.add(normal.mult(Math.max(Math.max(limbLength, limbHeight), limbWidth) + limbSeperation));
        }
        //Vector3f 
        newHingePos = contactPt.add(normal.mult(4 * jointPositionFraction));
        // Work out which hinge axis would make sense for auto hinge axis
        String axisToUse = "ZAxis";
        if (currentHingeAxis.equals("A")) {
            if (Math.abs(normal.x) < Math.abs(normal.z)) {
                axisToUse = "XAxis";
            }
        } else {
            axisToUse = currentHingeAxis;
        }

        //Build the new limb
        Block limb = new Block(newPos, newHingePos, limbWidth, limbHeight, limbLength, currentShape, axisToUse, weight);
        Matrix3f rotation = new Matrix3f();

        if (currentShape.equals("Cylinder")) {
            rotation.fromStartEndVectors(new Vector3f(0, 0, 1), normal);
        } else {
            rotation.fromStartEndVectors(new Vector3f(1, 0, 0), normal);
        }
        if (currentShape.equals("Sphere")) {
            rotation = rotation.mult(new Matrix3f(limbWidth, 0f, 0f, 0f, limbHeight, 0f, 0f, 0f, limbLength));
        }

        limb.rotation = rotation;

        // Stores the normal the limb was created at in the limb for future use
        limb.setNormal(normal);

        return limb;
    }

    //To be run when right click on body, adds new limb with dimensions defined in text fields
    public void addLimb(Block block, Vector3f contactPt, Vector3f normal) {

        // check if valid:
        if (this.isCollisionOccuring) {
            return;
        }

        //Get rid of old alien on screen
        if (this.currentAlienNode != null) {
            removeAlien(this.currentAlienNode);
        }

        Block limb = createLimb(block, contactPt, normal);


        //Add new limb to alien and instantiate
        block.addLimb(limb);

        instantiateAlien(alien, startLocation);
        setChaseCam(this.currentAlienNode);
        setupKeys(this.currentAlienNode);

        // get rid of ghost limbs
        removeGhostLimb(ghostLimb);
        removeGhostLimb(ghostLimb2);
        ghostLimb = null;
        ghostLimb2 = null;
    }

    public boolean saveAlien(String name) {
        if (alien != null) {
            alien.setName(name);
            File f = new File("aliens/" + alien.getName() + "/body.sav");
            f.getParentFile().mkdirs();
            try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f))) {
                o.writeObject(alien);

                //invalidate the training file, it it exists, in case the number of joints was changed
                //keep it but renamed to prevent data loss
                for (File toRename : f.getParentFile().listFiles()) {
                    if (toRename.getPath().contains("training.pop")) {
                        DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
                        Date dateobj = new Date();
                        File target = new File(toRename.getPath().substring(0, toRename.getPath().length() - 4) + df.format(dateobj) + ".pop");
                        toRename.renameTo(target);
                    }
                }
                return true;
            } catch (IOException ex) {
                Logger.getLogger(UIAppState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public boolean resetTraining() {
        if (alien != null) {
            String name = alien.getName();
            // File with old name
            File file = new File("aliens/" + name + "/training.pop");
            if (file.exists()) {
                // File with new name
                DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
                File file2 = new File("aliens/" + name + "/training" + df.format(new Date()) + ".pop");
                // Rename file 
                return file.renameTo(file2);
            }
            return false;
        }
        return false;
    }

    public String[] getLoadableAliens() {
        File file = new File("aliens");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        return directories;
    }

    public boolean loadAlien(String name) {
        File f = new File("aliens/" + name + "/body.sav");
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(f))) {
            Alien a = (Alien) o.readObject();
            if (a != null) {
                alien = a;
                resetAlien();
                instantiateAlien(alien, startLocation);
                setChaseCam(this.currentAlienNode);
                setupKeys(this.currentAlienNode);
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

        addKeyBindings();

        ghostMaterial = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        //ghostMaterial.setTexture("ColorMap", 
        //    assetManager.loadTexture("Textures/ColoredTex/Monkey.png"));
        ghostMaterial.setColor("Color", new ColorRGBA(0.32f, 0.85f, 0.5f, 1f));

        //ghostMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

        ghostMaterial2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        ghostMaterial2.setColor("Color", new ColorRGBA(0.85f, 0.32f, 0.32f, 1f));

        // setup ghost root node
        this.ghostRoot = new Node("ghost root");
        this.rootNode.attachChild(ghostRoot);

    }

    public void addKeyBindings() {
        //Add the key binding for the right click to add limb funtionality
        if (!inputManager.hasMapping("AddLimb")) {
            inputManager.addMapping("AddLimb", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
            inputManager.addListener(this, "AddLimb");
        }
        if (!inputManager.hasMapping("ToggleMesh")) {
            inputManager.addMapping("ToggleMesh", new KeyTrigger(KeyInput.KEY_Q));
            inputManager.addListener(this, "ToggleMesh");
        }
        if (!inputManager.hasMapping("ToggleSmooth")) {
            inputManager.addMapping("ToggleSmooth", new KeyTrigger(KeyInput.KEY_S));
            inputManager.addListener(this, "ToggleSmooth");
        }
        if (!inputManager.hasMapping("GoToEditor")) {
            inputManager.addMapping("GoToEditor", new KeyTrigger(KeyInput.KEY_E));
            inputManager.addListener(this, "GoToEditor");
        }
        if (!inputManager.hasMapping("Pulsate")) {
            inputManager.addMapping("Pulsate", new KeyTrigger(KeyInput.KEY_W));
            inputManager.addListener(this, "Pulsate");
        }
        if (!inputManager.hasMapping("Shift")) {
            inputManager.addMapping("Shift", new KeyTrigger(KeyInput.KEY_LSHIFT));
            inputManager.addListener(this, "Shift");
        }
    }

    public void removeKeyBindings() {
        if (inputManager.hasMapping("AddLimb")) {
            inputManager.deleteMapping("AddLimb");
        }
        if (inputManager.hasMapping("ToggleMesh")) {
            inputManager.deleteMapping("ToggleMesh");
        }
        if (inputManager.hasMapping("ToggleSmooth")) {
            inputManager.deleteMapping("ToggleSmooth");
        }
        if (inputManager.hasMapping("GoToEditor")) {
            inputManager.deleteMapping("GoToEditor");
        }
        if (inputManager.hasMapping("Pulsate")) {
            inputManager.deleteMapping("Pulsate");
        }
        if (!inputManager.hasMapping("Shift")) {
            inputManager.deleteMapping("Shift");
        }
    }

    public boolean beginTraining() {
        if (alien == null) {
            return false;
        }

        editing = false;

        this.physics.setEnabled(false);
        resetGravity();

        if (currentAlienNode == null) {
            instantiateAlien(alien, Vector3f.ZERO);
        }

        this.trainer = new AlienTrainer("aliens/" + alien.getName() + "/training.pop",
                simulationQueue, currentAlienNode.joints.size() + 1,
                currentAlienNode.joints.size());

        while (this.slaves.size() < SIM_COUNT) {
            SlaveSimulator toAdd = new SlaveSimulator(new TrainingAppState(this.alien, this.simulationQueue, 1.0f, this.accuracy, 1f / 60f));
            this.slaves.add(toAdd);
            // speed up by 5 times, 300 = 60 * 5
            /*
             AppSettings set = new AppSettings(false);
             set.setFrameRate(this.getSpeedUpFactor() * DEFAULT_FRAMERATE);
             toAdd.setSettings(set);
             toAdd.start(JmeContext.Type.Headless);
             */
            toAdd.setShowSettings(false);
            AppSettings sett = new AppSettings(false);
            sett.setCustomRenderer(FastNullContext.class);
            toAdd.setSettings(sett);
            toAdd.start();
        }

        this.trainer.start();


        return true;
    }

    public void endTraining() {
        this.trainer.terminateTraining(slaves);

        //slaves are cleaned up by trainer after current requests have been answered

        this.stopSimulation();

        resetAlien();

        instantiateAlien(alien, startLocation);
        setChaseCam(this.currentAlienNode);
        setupKeys(this.currentAlienNode);

        editing = true;

        setGravity(0.0f);
    }

    @Override
    public void onAction(String string, boolean bln, float tpf) {

        if (editing) {
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
        }
        if ("ToggleMesh".equals(string)) {
            if (!bln) {
                CheckBox mesh = nifty.getScreen("editor_options").findNiftyControl("wireMeshCheckBox", CheckBox.class);
                mesh.setChecked(!mesh.isChecked());
            }
        }

        if ("ToggleSmooth".equals(string)) {
            if (!bln) {
                CheckBox chasecam = nifty.getScreen("editor_options").findNiftyControl("chaseCamCheckBox", CheckBox.class);
                chasecam.setChecked(!chasecam.isChecked());
            }
        }

        if ("GoToEditor".equals(string)) {
            if (!bln) {
                myMainMenuController.editorOptions();
            }
        }

        if ("Pulsate".equals(string)) {
            if (!bln) {
                myMainMenuController.pulsateToggle();
            }
        }

        if ("Shift".equals(string)) {
            shiftDown = bln;
        }


        //When right mouse button clicked, fire ray to see if intersects with body
        if ("AddLimb".equals(string) && !bln && !checkRootNull()) {


            CollisionResult collision = getCursorRaycastCollision(this.simRoot);


            //If collided then generate new limb at collision point
            if (collision != null) {
                Geometry geo = collision.getGeometry();
                Vector3f colpt = collision.getContactPoint();
                Vector3f pt = colpt.add(geo.getWorldTranslation().negate());
                Vector3f norm = collision.getContactNormal();

                //Find block assoicated with collision geometry
                Block block = null;

                LinkedList<Block> q = new LinkedList<>();
                q.push(alien.rootBlock);

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

                    if (!shiftDown) { // add limb

                        addLimb(block, pt, norm);

                        CheckBox symmetricBox = nifty.getCurrentScreen().findNiftyControl("symmetricCheckBox", CheckBox.class);
                        boolean symmetric = symmetricBox.isChecked();

                        if (symmetric) {
                            switch (block.collisionShapeType) {
                                case "Box":
                                    addLimb(block, pt.subtract(pt.project(collision.getContactNormal()).mult(2.0f)), norm.negate());
                                    break;
                                default:
                                    addLimb(block, pt.subtract(pt.project(Vector3f.UNIT_Z).mult(2.0f)), norm.subtract(norm.project(Vector3f.UNIT_Z).mult(2.0f)));
                                    break;
                            }
                        }
                    } else { // delete limb

                        removeLimb(block);
                        // remove deletion ghost
                        if (delghost != null) {
                            removeGhostLimb(delghost);
                            delghost = null;
                        }
                    }
                }
            }
        }
    }

    public void setupKeys(AlienNode brain) {

        // Creates keyboard bindings for the joints with keys in jointKeys, limited by umber of joints or keys specified in jointKeys
        int numberOfJoints = Math.min(brain.joints.size(), jointKeys.length / 2);
        for (int i = 0; i < numberOfJoints; i++) {

            if (!inputManager.hasMapping("Alien joint " + ((Integer) i).toString() + " clockwise")) {
                inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " clockwise", new KeyTrigger(jointKeys[2 * i]));
                inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " anticlockwise", new KeyTrigger(jointKeys[2 * i + 1]));
                inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " clockwise");
                inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " anticlockwise");
            }
        }
        currentAlienNode = brain;
    }

    /**
     * Start a new simulation. This method should not be called externally by
     * another thread.
     *
     * @param data the SimulationData object containing the ANN to be tested and
     * other parameters
     */
    protected void startSimulation(SimulationData data) {
        // turn physics back on
        this.physics.setEnabled(true);
        this.reset();
        this.currentSim = data;
        this.simTimeLimit = (float) data.getSimTime();
        this.currentAlienNode = instantiateAlien(this.alien, this.startLocation);
        setChaseCam(currentAlienNode);
        MLRegression nn = (MLRegression) ObjectCloner.deepCopy(data.getToEvaluate());
        if (this.isFixedTimeStep) {
            this.currentAlienNode.addControl(new AlienBrain(nn, physics.getPhysicsSpace().getAccuracy(), physics.getSpeed(), this.fixedTimeStep));
        } else {
            this.currentAlienNode.addControl(new AlienBrain(nn, physics.getPhysicsSpace().getAccuracy(), physics.getSpeed()));
        }
        this.simInProgress = true;
    }

    /**
     * Stop simulation and set fitness value.
     */
    protected void stopSimulation() {

        this.simInProgress = false;
        if (this.currentSim != null) {
            double fitness = this.calcFitness();
            //this.currentSim.setFitness(fitness);
            System.out.println("Stopping simulation! " + this.currentSim.toString());
        }
        // turn physics off to save CPU time
        // don't need this in front end
        //this.physics.setEnabled(false);
    }

    @Override
    public void reset() {
        super.reset();
        this.currentSim = null;
        this.simInProgress = false;
        this.simTimeLimit = 0.0f;
    }

    @Override
    public void update(float tpf) {
        if (simInProgress) {
            simTimeLimit -= tpf * physics.getSpeed();
            if (simTimeLimit < 0f) {
                // stop simulation and report result
                stopSimulation();
            }
        } else {
            updateGhostLimb();

            // try to poll task from the queue
            if (!editing) {
                SimulationData s;
                s = this.simulationQueue.peek();
                if (s != null) {
                    System.out.println(Thread.currentThread().getId() + ": starting simulation!");
                    startSimulation(s);
                }
            }
        }
    }

    @Override
    public void cleanup() {
        if (this.trainer != null) {
            this.trainer.terminateTraining(slaves);
        }
        super.cleanup();
    }
}
