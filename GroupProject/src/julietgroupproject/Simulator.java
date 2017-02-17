package julietgroupproject;

import com.jme3.system.JmeContext;
import com.jme3.app.SimpleApplication;
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
import julietgroupproject.GUI.MainMenuController;

public class Simulator extends SimpleApplication implements ActionListener {

    
    int age = 0;
    int simTime;
    int simsRunning = 0;
    Simulator parent;
    Alien alienToSim;
    Brain brainOfAlienCurrentlyBeingSimulated;
    
    boolean mainApplication = false;
    boolean runningPhysics = true;
    


    private MainMenuController myMainMenuController;

    float limbPower = 0.8f;
    float limbTargetVolcity = 2f;
    float time;
    float minBoxDimention = 0.4f;
    float minSphereDimention = 0.4f;
    Random rng = new Random();
    private BulletAppState bulletAppState = new BulletAppState();
    private Brain brainToControl;
    private Texture alienTexture1;
    private Texture alienTexture2;
    private Texture alienTexture3;
    private Texture grassTexture;
    private Texture skyTexture;
    private Material alienMaterial1;
    private Material alienMaterial2;
    private Material alienMaterial3;
    private Material grassMaterial;
    private Material skyMaterial;
    private Nifty nifty;
    private boolean wireMesh = true;
    
    private ChaseCamera chaseCam;
    private float horizontalAngle = 0;
    private float verticalAngle = 0;
    private float cameraZoom = 10;
    private boolean smoothCam = true;
    
    private String currentShape = "Box";
           
    
    Alien simpleAlien;
    Alien smallBlock;
    Alien flipper;
    Alien cuboid;
    Brain prevAlien;
    

    int[] jointKeys = { // Used for automatically giving limbs keys
        KeyInput.KEY_T, KeyInput.KEY_Y, // Clockwise and anticlockwise key pair for first limb created
        KeyInput.KEY_U, KeyInput.KEY_I, // and second pair
        KeyInput.KEY_G, KeyInput.KEY_H, // etc
        KeyInput.KEY_J, KeyInput.KEY_K,
        KeyInput.KEY_V, KeyInput.KEY_B,
        KeyInput.KEY_N, KeyInput.KEY_M};
    
    public void removeAlien(Brain brain) {
        bulletAppState.getPhysicsSpace().removeAll(brain.nodeOfLimbGeometries);
        brain.nodeOfLimbGeometries.removeFromParent();
    }
    
    
    public void toggleGravityOn() {
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -9.81f, 0));
    }
    
    public void toggleGravityOff() {
        bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);
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
        bulletAppState.setDebugEnabled(wireMesh);
        wireMesh = !wireMesh;
    }
    
    //Method for easily printing out vectors for debugging
    public void printVector3f(Vector3f vec) {
        System.out.println("("+vec.getX()+"," +vec.getY()+","+vec.getZ()+")");
    }
    
    public void setChaseCam(Alien shape) {
        if (chaseCam !=null) { 
            horizontalAngle = chaseCam.getHorizontalRotation();
            verticalAngle = chaseCam.getVerticalRotation();
            cameraZoom = chaseCam.getDistanceToTarget();
            chaseCam.setSmoothMotion(false);
            
        }
        //toggleSmoothness();
        chaseCam = new ChaseCamera(cam, shape.rootBlock.getGeometry(), inputManager);
        //toggleSmoothness();
        chaseCam.setSmoothMotion(false);
        chaseCam.setDefaultDistance(20.6f);
        //chaseCam.setSmoothMotion(true);
        chaseCam.setDefaultHorizontalRotation(horizontalAngle);
        chaseCam.setDefaultVerticalRotation(verticalAngle);
        
        chaseCam.setMinVerticalRotation((float) (Math.PI)*-0.25f);
        //chaseCam.setTrailingEnabled(true);
        chaseCam.setChasingSensitivity(1f);
        
        chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_LEFT),new KeyTrigger(KeyInput.KEY_P));
        chaseCam.setTrailingRotationInertia(0.1f);
        chaseCam.setUpVector(new Vector3f(0,1,0));
        chaseCam.setZoomInTrigger(new KeyTrigger(KeyInput.KEY_LBRACKET), new MouseButtonTrigger(MouseInput.AXIS_WHEEL));
        chaseCam.setZoomOutTrigger(new KeyTrigger(KeyInput.KEY_RBRACKET),new MouseButtonTrigger(MouseInput.AXIS_WHEEL));
        chaseCam.setZoomSensitivity(10);
        //toggleSmoothness();
        chaseCam.setSmoothMotion(true);
        
    }
    
    public void setShapeToCuboid() {
        currentShape = "Box";
        System.out.println(currentShape);
        
    }
    
    public void setShapeToSphere() {
        currentShape = "Sphere";
        System.out.println(currentShape);
    }
    public void createNewBody() {
        if (prevAlien==null){
            
            //Take the entries from text fields for limb size, do some error handling
            TextField heightField = nifty.getCurrentScreen().findNiftyControl("bodyHeightTextField", TextField.class);
            TextField widthField = nifty.getCurrentScreen().findNiftyControl("bodyWidthTextField", TextField.class);
            TextField lengthField = nifty.getCurrentScreen().findNiftyControl("bodyLengthTextField", TextField.class);
            float bodyWidth = 0f;
            float bodyHeight = 0f;
            float bodyLength = 0f;
            
            
            try {
                bodyWidth = Float.valueOf(heightField.getText());
            } catch (NumberFormatException e) {
                System.out.println("Whoops - Incorrect number format");
            } finally {
                if (bodyWidth <0) {
                    bodyWidth = -bodyWidth;
                } else if (bodyWidth ==0) {
                    bodyWidth = 4*rng.nextFloat();
                }
            } 
            
            try {
                bodyHeight = Float.valueOf(widthField.getText());
            } catch (NumberFormatException e) {
                System.out.println("Whoops - Incorrect number format");
            } finally {
                if (bodyHeight <0) {
                    bodyHeight = -bodyHeight;
                } else if (bodyHeight ==0) {
                    bodyHeight = 4*rng.nextFloat();
                }
            } 
           
            try {
                bodyLength = Float.valueOf(lengthField.getText());
            } catch (NumberFormatException e) {
                System.out.println("Whoops - Incorrect number format");
            } finally {
                if (bodyLength <0) {
                    bodyLength = -bodyLength;
                } else if (bodyLength ==0) {
                    bodyLength = 4*rng.nextFloat();
                }
            } 
            
            //Instantiate the new alien
            Vector3f pos = new Vector3f(-10+20*rng.nextFloat(),-10+20*rng.nextFloat(),-10+20*rng.nextFloat());
            Block bodyBlock   = new Block(pos, pos.mult(0.5f), bodyHeight, bodyWidth, bodyLength, currentShape, "ZAxis", 2.2f);
            cuboid = new Alien(bodyBlock);
            prevAlien = instantiateAlien(cuboid, new Vector3f(0f, 5f, -10f));
            setChaseCam(cuboid);
            setupKeys(prevAlien);
        }
        
    }
    
    
    //To be run when addLimb button pressed, adds random limb anywhere around body
    public void addLimb() {
        
        //Get rid of old alien on screen
        if (prevAlien!=null){
            removeAlien(prevAlien);
        
            //Find safe distance for hinge point from body
            float radius = (float) Math.sqrt((cuboid.rootBlock.height*cuboid.rootBlock.height)+(cuboid.rootBlock.width*cuboid.rootBlock.width)+(cuboid.rootBlock.length*cuboid.rootBlock.length));
            
            //Choose a random diretion to add limb and find hinge point
            Vector3f newDir = new Vector3f(rng.nextFloat(),rng.nextFloat(),rng.nextFloat()).normalize();
            Vector3f newHingePos = newDir.mult(radius);
            
            //Build random sizes for new limb
            float boxWidth = rng.nextFloat();
            float boxHeight = 0.3f*rng.nextFloat();
            float boxLength = 4*rng.nextFloat();
            
            //Find safe distance from limb and get postion vector
            float boxRad = (float) Math.sqrt((boxWidth*boxWidth)+(boxHeight*boxHeight)+(boxLength*boxLength));
            Vector3f newPos = newDir.mult(radius+boxRad);

            //Make limb and add it to body
            Block flipper  = new Block(newPos,newHingePos, boxWidth, boxHeight, boxLength, currentShape, "XAxis", 1f);
            cuboid.rootBlock.addLimb(flipper);
            
             //Instantiate the new alien
            prevAlien = instantiateAlien(cuboid, new Vector3f(0f, 5f, -10f));
            setChaseCam(cuboid);
            setupKeys(prevAlien);
        }
     
       
    }
   
    //To be run when right click on body, adds new limb with dimensions defined in text fields
    public void addLimb(Block block, Vector3f contactPt, Vector3f normal) {
        
        //Get rid of old alien on screen
         if (prevAlien!=null){
            removeAlien(prevAlien);
        } 
   
       
        //Take the entries from text fields for limb size, do some error handling
        TextField heightField = nifty.getCurrentScreen().findNiftyControl("heightTextField", TextField.class);
        TextField widthField = nifty.getCurrentScreen().findNiftyControl("widthTextField", TextField.class);
        TextField lengthField = nifty.getCurrentScreen().findNiftyControl("lengthTextField", TextField.class);
        float boxWidth = -0.2f;
        float boxHeight = -0.2f;
        float boxLength = -0.2f;
        try {
            boxWidth = Float.valueOf(heightField.getText());
            boxHeight = Float.valueOf(widthField.getText());
            boxLength = Float.valueOf(lengthField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Whoops - Incorrect number format");
        } finally {
            if (boxWidth <0) {
                boxWidth = -boxWidth;
            } else if (boxWidth ==0) {
                boxWidth = 0.5f;
            }
            if (boxHeight <0) {
                boxHeight = -boxHeight;
            } else if (boxHeight ==0) {
                boxHeight = 0.5f;
            }
            if (boxLength <0) {
                boxLength = -boxLength;
            } else if (boxLength ==0) {
                boxLength = 0.5f;
            }
        } 
        
        //Find hinge and postion vectors given shape and click position
        Vector3f newHingePos = contactPt.add(normal.mult(0.5f));
        Vector3f newPos = contactPt.add(normal.mult(Math.max(Math.max(boxLength,boxHeight),boxWidth)+1.0f));
 
        //Build the new limb
        Block limb  = new Block(newPos,newHingePos, boxHeight, boxWidth, boxLength, currentShape, "XAxis", 1f);
        
        //Still working on getting this to rotate
        limb.setNormal(normal);
        
        //Add new limb to alien and instantiate
        block.addLimb(limb);
        prevAlien = instantiateAlien(cuboid, new Vector3f(0f, 5f, -10f));
        setChaseCam(cuboid);
        setupKeys(prevAlien);
    }
   
    
    @Override
    public void simpleInitApp() {
        // Application start code
        
        // Setup Physics
        setupTextures();
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        

        // setDebugEnabled - wireframe
        bulletAppState.setDebugEnabled(true);

        // turn the wireframe off
        bulletAppState.setDebugEnabled(false);
        setupPhysicsWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        viewPort.setBackgroundColor(new ColorRGBA(98 / 255f, 167 / 255f, 224 / 255f, 1f));
        //setupBackground();
        

        if(mainApplication) {
            Alien simpleAlien;
            Alien smallBlock;
            Alien flipper;

            // Create an examples of aliens (what the editor will do) - we can add serialising them and saving later
            Block chip = new Block(new Vector3f( 0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, 0.0f, 0.0f), 0.1f, 0.1f, 0.1f, "Box", "ZAxis", 1.0f);
            smallBlock = new Alien(chip);

            Block body = new Block(new Vector3f( 0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, 0.0f, 0.0f), 1.0f, 0.1f, 1.0f, "Box", "ZAxis", 1.0f);
            body.setRotation(new Matrix3f(1f,0f,0f,0f,0f,-1f,0f,1f,0f));
            Block left = new Block(new Vector3f(-3.0f, 0.0f, 0.0f), new Vector3f(-1.5f, 0.0f, 0.0f), 1.0f, 0.1f, 1.0f, "Box", "ZAxis", 1.0f);
            left.setRotation(new Matrix3f(1f,0f,0f,0f,0f,-1f,0f,1f,0f));
            body.addLimb(left);
            simpleAlien = new Alien(body);

            float flipperTranslation = 0.8f;
            Block rootBlock = new Block(new Vector3f( 0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, 0.0f, 0.0f), 0.9f, 0.1f, 0.0f, "Torus", "ZAxis", 1.5f);
            Block legLeft   = new Block(new Vector3f(-2.9f, 0.0f, 0.0f), new Vector3f(-1.3f, 0.0f, 0.0f), 0.7f, 0.1f, 0.6f, "Torus", "ZAxis", 2.2f);
            legLeft.setRotation(new Matrix3f(1f,0f,0f,0f,0f,-1f,0f,1f,0f));
            Block legRight  = new Block(new Vector3f( 2.6f, 0.0f, 0.0f), new Vector3f( 1.3f, 0.0f, 0.0f), 1.5f, 0.1f, 1.3f, "Box", "YAxis", 1.2f);
            Block flipper1  = new Block(new Vector3f( flipperTranslation, 0.0f, 3.6f), new Vector3f( flipperTranslation, 0.0f, 1.3f), 0.6f, 0.1f, 2.1f, "Box", "XAxis", 1f);
            Block flipper2  = new Block(new Vector3f( flipperTranslation, 0.0f,-3.6f), new Vector3f( flipperTranslation, 0.0f,-1.3f), 0.6f, 0.1f, 2.1f, "Box", "XAxis", 1f);
            Block head      = new Block(new Vector3f(-2.0f, 0.0f, 0.0f), new Vector3f(-1.3f, 0.0f, 0.0f), 0.5f, 0.5f, 0.5f, "Cylinder", "ZAxis", 1f);
            rootBlock.addLimb(legRight);
            rootBlock.addLimb(legLeft);
            legLeft.addLimb(flipper1);
            legLeft.addLimb(flipper2);
            flipper = new Alien(rootBlock);

            // Create that alien in the simulation, with the Brain interface used to control it.
            //brainOfAlienCurrentlyBeingSimulated = instantiateAlien(flipper, new Vector3f(0f, 0f, -10f));
            //Brain flipperb = instantiateAlien(flipper, new Vector3f(10f, 30f, -30f));
            //Brain flipperc = instantiateAlien(flipper, new Vector3f(-15f, 90f, -60f));

            /*
            Simulator app2 = new Simulator();
            app2.simTime = 1000;
            app2.alienToSim = flipper;
            app2.parent = this;
            app2.start(JmeContext.Type.Headless);
            Simulator app3 = new Simulator();
            app3.simTime = 1000;
            app3.alienToSim = flipper;
            app3.parent = this;
            app3.start(JmeContext.Type.Headless);
            simsRunning = 2;
            
            
        }
        if (!mainApplication) {
            brainOfAlienCurrentlyBeingSimulated = instantiateAlien(alienToSim, Vector3f.ZERO);
            // Control the instantiated alien (what the neural network will do)
            setupKeys(brainOfAlienCurrentlyBeingSimulated);
        }

            */
            
        }
        /*if (!mainApplication) {
            brainOfAlienCurrentlyBeingSimulated = instantiateAlien(alienToSim, Vector3f.ZERO);
            // Control the instantiated alien (what the neural network will do)
            setupKeys(brainOfAlienCurrentlyBeingSimulated);
        }*/


        

        Vector3f pos = new Vector3f(-10+20*rng.nextFloat(),-10+20*rng.nextFloat(),-10+20*rng.nextFloat());
        Block randomCuboid   = new Block(pos, pos.mult(0.5f), 4*rng.nextFloat(), 4*rng.nextFloat(), 4*rng.nextFloat(), "Box", "ZAxis", 2.2f);
        cuboid = new Alien(randomCuboid);
        
        Block body = new Block(new Vector3f( 0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, 0.0f, 0.0f), 1.0f, 0.1f, 1.0f, "Box", "ZAxis", 1.0f);
        body.setRotation(new Matrix3f(1f,0f,0f,0f,0f,-1f,0f,1f,0f));
        Block left = new Block(new Vector3f(-3.0f, 0.0f, 0.0f), new Vector3f(-1.5f, 0.0f, 0.0f), 1.0f, 0.1f, 1.0f, "Box", "ZAxis", 1.0f);
        left.setRotation(new Matrix3f(1f,0f,0f,0f,0f,-1f,0f,1f,0f));
        body.addLimb(left);
        simpleAlien = new Alien(body);

        
        
        
        //removeAlien(flippera);

        // Control the instantiated alien (what the neural network will do)
        //setupKeys(flippera);

        toggleGravityOff();

        //toggleGravityOff();

                
        myMainMenuController = new MainMenuController(this);

        stateManager.attach(myMainMenuController);
        
        //Set up nifty
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);

        nifty = niftyDisplay.getNifty();

        Nifty nifty = niftyDisplay.getNifty();

        guiViewPort.addProcessor(niftyDisplay);
        nifty.fromXml("Interface/MainMenuLayout.xml", "start", myMainMenuController);
        //nifty.setDebugOptionPanelColors(true); //un-comment this line to use DebugPanelColors and make sure Nifty is running correctly.
        

        //flyCam.setDragToRotate(true); //detaches camera from mouse unless you click/drag.a
        
        flyCam.setEnabled(false);
        
    }

   
    
    

    public void endSimulator(Simulator s) {
        // Only run in parent simulator, called by children when simTime is up.
        System.out.println("Simulator has finished with a fitness of: "+ s.fitness());
        s.stop();
        simsRunning -= 1;
        
        if (simsRunning == 0){
            System.out.println("finished simulating!");
            // Continue with the app
        }
    }
    
    public Brain instantiateAlien(Alien alien, Vector3f location) {

        Brain brain = new Brain();

        Node nodeOfLimbGeometries = new Node();
        
        Block rootBlock = alien.rootBlock;
        Geometry rootBlockGeometry = createLimb(rootBlock.collisionShapeType, rootBlock.width, rootBlock.height, rootBlock.length, location, rootBlock.mass);
        rootBlock.applyProperties(rootBlockGeometry);
        
        nodeOfLimbGeometries.attachChild(rootBlockGeometry);
        brain.geometries.add(rootBlockGeometry);

        recursivelyAddBlocks(rootBlock, rootBlock, rootBlockGeometry, nodeOfLimbGeometries, brain);
        
        bulletAppState.getPhysicsSpace().addAll(nodeOfLimbGeometries);
        rootNode.attachChild(nodeOfLimbGeometries);
        brain.nodeOfLimbGeometries = nodeOfLimbGeometries;

        
        final int jointCount = brain.joints.size();
        
        BasicNetwork nn = new BasicNetwork(){
            
            Random rng = new Random();
            final int size = jointCount;
            double[] out = new double[size];
            
            @Override
            public MLData compute(MLData in) {
                for (int i=0;i<size;i++) {
                    out[i] = rng.nextDouble();
                }
                return new BasicMLData(out);
            }
        };
        nn.addLayer(new BasicLayer(jointCount));
        nn.addLayer(new BasicLayer(jointCount));
        nn.getStructure().finalizeStructure();
        nn.reset();
        brain.setNN(nn);
        
        // uncomment this line to allow control from ANN
        //brain.nodeOfLimbGeometries.addControl(brain);

                
        return brain;
    }

    public void recursivelyAddBlocks(Block rootBlock, Block parentBlock, Geometry parentGeometry, Node geometries, Brain brain) {
        for (Block b : parentBlock.getConnectedLimbs()) {
            Geometry g = createLimb(b.collisionShapeType, b.width, b.height, b.length, parentGeometry.getControl(RigidBodyControl.class).getPhysicsLocation().add(b.getPosition()), b.mass);
            b.applyProperties(g);

            printVector3f(b.getHingePosition());

            HingeJoint joint = joinHingeJoint(parentGeometry, g, parentGeometry.getControl(RigidBodyControl.class).getPhysicsLocation().add(b.getHingePosition()), b.hingeType);
            geometries.attachChild(g);
            brain.joints.add(joint);
            brain.geometries.add(g);
            recursivelyAddBlocks(rootBlock, b, g, geometries, brain);
        }
    }

    private Geometry createLimb(String meshShape, float width, float height, float length, Vector3f location, float mass) {
        
        
        Mesh mesh;
        if (meshShape.equals("Cylinder")) {
            mesh = new Cylinder(40,40,width,length,true);
        } else if (meshShape.equals("Torus")) {
            mesh = new Torus(40,40,width,length);
        } else if (meshShape.equals("Sphere")) {
            mesh = new Sphere(40,40,Math.max(width,minSphereDimention));
        } else {
            mesh = new Box(Math.max(minBoxDimention,width),Math.max(minBoxDimention,height),Math.max(minBoxDimention,length));
        }
        Geometry limb = new Geometry("Limb",mesh);
        RigidBodyControl r;
        r = new RigidBodyControl(CollisionShapeFactory.createDynamicMeshShape(limb),mass);
        limb.addControl(r); //limb.setMesh(CollisionShapeFactory.createMeshShape(limb));
        r.setPhysicsLocation(location);
        //limb.setLocalTranslation(location);
        limb.setMaterial(alienMaterial2);
        limb.getMesh().scaleTextureCoordinates(new Vector2f(1f,1f));
        limb.getControl(RigidBodyControl.class).setPhysicsLocation(location);
        return limb;
    }
    private Node createLimbNode(String collisionType, float width, float height, float length, Vector3f location, boolean rotate, float mass) {
        
        int axis = rotate ? PhysicsSpace.AXIS_X : PhysicsSpace.AXIS_Y;
        Node node = new Node("Limb");
        RigidBodyControl rigidBodyControl;
        if (collisionType.equals("Capsule")) {
            CapsuleCollisionShape shape = new CapsuleCollisionShape(width, height, axis);
            rigidBodyControl = new RigidBodyControl(shape, mass);
        } else {
            BoxCollisionShape shape = new BoxCollisionShape(new Vector3f(width, height, length));
            rigidBodyControl = new RigidBodyControl(shape, mass);
        }
        node.setLocalTranslation(location);
        node.addControl(rigidBodyControl);
        node.setMaterial(alienMaterial3);
        return node;
    }
    private HingeJoint joinHingeJoint(Geometry A, Geometry B, Vector3f connectionPoint, String hingeType) {
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

    public void onAction(String string, boolean bln, float tpf) {

        // Controls the joints with keys in jointKeys
        if (brainToControl != null) {
            int numberOfJoints = Math.min(brainToControl.joints.size(), jointKeys.length / 2);

            for (int i = 0; i < numberOfJoints; i++) {

                if (("Alien joint " + ((Integer) i).toString() + " clockwise").equals(string)) {
                    if (bln) {
                        brainToControl.joints.get(i).getBodyA().activate();
                        brainToControl.joints.get(i).getBodyB().activate();
                        brainToControl.joints.get(i).enableMotor(true, 1 * limbTargetVolcity, limbPower);
                        System.out.println("G1");
                    } else {
                        brainToControl.joints.get(i).enableMotor(false, 0, 0);
                        System.out.println("G2");
                    }
                }
                if (("Alien joint " + ((Integer) i).toString() + " anticlockwise").equals(string)) {
                    if (bln) {
                        brainToControl.joints.get(i).getBodyA().activate();
                        brainToControl.joints.get(i).getBodyB().activate();
                        brainToControl.joints.get(i).enableMotor(true, -1 * limbTargetVolcity, limbPower);
                    } else {
                        brainToControl.joints.get(i).enableMotor(false, 0, 0);
                    }
                }
            }
        }
        if ("Spawn Alien".equals(string)) {
            if (!bln){
                //spawnAlien();
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
            if (results.size()>0) {
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
                    addLimb(block,pt,norm);
                }
            }
        }

    }

    public void setupKeys(Brain brain) {
        // Creates keyboard bindings for the joints with keys in jointKeys, limited by umber of joints or keys specified in jointKeys
        int numberOfJoints = Math.min(brain.joints.size(), jointKeys.length / 2);
        for (int i = 0; i < numberOfJoints; i++) {
            inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " clockwise", new KeyTrigger(jointKeys[2 * i]));
            inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " anticlockwise", new KeyTrigger(jointKeys[2 * i + 1]));
            inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " clockwise");
            inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " anticlockwise");
        }
        brainToControl = brain;
        inputManager.addMapping("Pull ragdoll up", new MouseButtonTrigger(0));
        inputManager.addListener(this, "Pull ragdoll up");
        inputManager.addMapping("Spawn Alien", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Spawn Alien");

        
        //Add the key binding for the right click to add limb funtionality
        inputManager.addMapping("AddLimb",new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(this, "AddLimb");
        
    }
    public void setupTextures() {
        grassTexture = assetManager.loadTexture("Textures/grass1.jpg");
        grassTexture.setAnisotropicFilter(4);
        grassTexture.setWrap(WrapMode.Repeat);
        grassMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        grassMaterial.setTexture("ColorMap", grassTexture);
        skyTexture = assetManager.loadTexture("Textures/sky1.jpg");
        skyTexture.setWrap(WrapMode.Repeat);
        skyMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        skyMaterial.setTexture("ColorMap", skyTexture);
        
        alienTexture1 = assetManager.loadTexture("Textures/alien1.jpg");
        alienTexture1.setWrap(WrapMode.Repeat);
        alienMaterial1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial1.setTexture("ColorMap", alienTexture1);
        
        alienTexture2 = assetManager.loadTexture("Textures/alien2.jpg");
        alienTexture2.setWrap(WrapMode.Repeat);
        alienMaterial2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial2.setTexture("ColorMap", alienTexture2);
        alienTexture3 = assetManager.loadTexture("Textures/alien3.jpg");
        alienTexture3.setWrap(WrapMode.Repeat);
        alienMaterial3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial3.setTexture("ColorMap", alienTexture3);
    }
    public void setupPhysicsWorld(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);

        Box floorBox = new Box(140, 1f, 140);
        Geometry floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setMaterial(grassMaterial);
        floorGeometry.setLocalTranslation(0, -5, 0);
        floorGeometry.addControl(new RigidBodyControl(0));
        floorGeometry.getMesh().scaleTextureCoordinates(new Vector2f(40,40));
        rootNode.attachChild(floorGeometry);
        space.add(floorGeometry);
        /*
         //movable boxes
         for (int i = 0; i < 12; i++) {
         Box box = new Box(0.25f, 0.25f, 0.25f);
         Geometry boxGeometry = new Geometry("Box", box);
         boxGeometry.setMaterial(material);
         boxGeometry.setLocalTranslation(i, 5, -3);
         //RigidBodyControl automatically uses box collision shapes when attached to single geometry with box mesh
         boxGeometry.addControl(new RigidBodyControl(2));
         rootNode.attachChild(boxGeometry);
         space.add(boxGeometry);
         }
         */
    }
    public void setupBackground() {

        Picture p = new Picture("background");
        p.setMaterial(skyMaterial);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setPosition(0, 0);
        p.updateGeometricState();
        ViewPort pv = renderManager.createPreView("background", cam);
        pv.setClearFlags(true, true, true);
        pv.attachScene(p);
        viewPort.setClearFlags(false, true, true);
        guiViewPort = pv;

    }
    
    public float fitness() {
        return brainOfAlienCurrentlyBeingSimulated.geometries.get(0).getControl(RigidBodyControl.class).getPhysicsLocation().x;
    }
    @Override
    public void simpleUpdate(float tpf) {
        if (runningPhysics) {
            age += 1;
            System.out.println(Thread.currentThread().getId() + " : "  + age + " : "+fitness());
            if (!mainApplication && age >= simTime) {
                parent.endSimulator(this);
            }
        }
    }

    public static void main(String[] args) {
        Simulator app = new Simulator();
        app.mainApplication = true;
        app.runningPhysics = false;
        app.start();
    }
}
