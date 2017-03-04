/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.FlyByCamera;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;

/**
 * A SimulatorAppState with graphics support.
 *
 * @author Peter
 */
public class DrawingAppState extends SimulatorAppState {

    protected Camera cam;
    protected FlyByCamera flyCam;
    protected ViewPort guiViewPort;
    protected AudioRenderer audioRenderer;
    protected RenderManager renderManager;
    //Textures
    protected Texture alienTexture1;
    protected Texture alienTexture2;
    protected Texture alienTexture3;
    protected Texture alienTexture4;
    protected Texture grassTexture;
    protected Texture skyTexture;
    protected Texture alienTransparentTexture1;
    protected Texture alienTransparentTexture2;
    protected Texture alienTransparentTexture3;
    protected Texture alienTransparentTexture4;
    //Materials
    protected Material alienMaterial1;
    protected Material alienMaterial2;
    protected Material alienMaterial3;
    protected Material alienMaterial4;
    protected Material grassMaterial;
    protected Material skyMaterial;
    protected Material alienTransparentMaterial1;
    protected Material alienTransparentMaterial2;
    protected Material alienTransparentMaterial3;
    protected Material alienTransparentMaterial4;
    protected Material greenGhostMaterial;
    protected Material redGhostMaterial;
    protected Material[] materials;
    protected Material[] materials_t;
    private Geometry arrowGeometry;
    protected boolean showArrow = false;

    public DrawingAppState(Alien _alien, double _simSpeed, double _accuracy) {
        super(_alien, _simSpeed, _accuracy);
    }

    public DrawingAppState(Alien _alien, double _simSpeed, double _accuracy, double _fixedTimeStep) {
        super(_alien, _simSpeed, _accuracy, _fixedTimeStep);
    }

    @Override
    public void reset() {
        boolean arrow = this.showArrow;
        super.reset();
        if (arrow) {
            showArrow();
        } else {
            hideArrow();
        }
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.cam = this.app.getCamera();
        this.flyCam = this.app.getFlyByCamera();
        this.guiViewPort = this.app.getGuiViewPort();
        this.audioRenderer = this.app.getAudioRenderer();
        this.renderManager = this.app.getRenderManager();
    }

    /**
     * Additionally add ambient lighting, and add textures to the floor box.
     */
    @Override
    protected void initialiseWorld() {

        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        simRoot.addLight(light);
        viewPort.setBackgroundColor(new ColorRGBA(98 / 255f, 167 / 255f, 224 / 255f, 1f));

        Box floorBox = new Box(140f, 1f, 140f);
        floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setLocalTranslation(0, -5, 0);
        floorGeometry.addControl(new RigidBodyControl(0));
        floorGeometry.setMaterial(grassMaterial);
        floorGeometry.getMesh().scaleTextureCoordinates(new Vector2f(40f, 40f));

        simRoot.attachChild(floorGeometry);
        physics.getPhysicsSpace().add(floorGeometry);

        //setupBackground();
    }

    protected void setupBackground() {

        Picture p = new Picture("background");
        p.setMaterial(skyMaterial);
        p.setWidth(guiViewPort.getCamera().getWidth());
        p.setHeight(guiViewPort.getCamera().getHeight());
        p.setPosition(0, 0);
        p.updateGeometricState();
        ViewPort pv = renderManager.createPreView("background", cam);
        pv.setClearFlags(true, true, true);
        pv.attachScene(p);
        viewPort.setClearFlags(false, true, true);
        guiViewPort = pv;

    }

    /**
     * Load textures and materials from asset files.
     */
    @Override
    public void setupTextures() {
        grassTexture = assetManager.loadTexture("Textures/grass4.png");
        grassTexture.setAnisotropicFilter(4);
        grassTexture.setWrap(Texture.WrapMode.Repeat);
        grassMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        grassMaterial.setTexture("ColorMap", grassTexture);
        skyTexture = assetManager.loadTexture("Textures/sky1.jpg");
        skyTexture.setWrap(Texture.WrapMode.Repeat);
        skyMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        skyMaterial.setTexture("ColorMap", skyTexture);

        alienTexture1 = assetManager.loadTexture("Textures/alien1.jpg");
        alienTexture1.setWrap(Texture.WrapMode.Repeat);
        alienMaterial1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial1.setTexture("ColorMap", alienTexture1);

        alienTexture2 = assetManager.loadTexture("Textures/alien2.jpg");
        alienTexture2.setWrap(Texture.WrapMode.Repeat);
        alienMaterial2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial2.setTexture("ColorMap", alienTexture2);

        alienTexture3 = assetManager.loadTexture("Textures/alien3.jpg");
        alienTexture3.setWrap(Texture.WrapMode.Repeat);
        alienMaterial3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial3.setTexture("ColorMap", alienTexture3);

        alienTexture4 = assetManager.loadTexture("Textures/alien4.jpg");
        alienTexture4.setWrap(Texture.WrapMode.Repeat);
        alienMaterial4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial4.setTexture("ColorMap", alienTexture4);

        alienTransparentTexture1 = assetManager.loadTexture("Textures/alien1_t.png");
        alienTransparentTexture1.setWrap(Texture.WrapMode.Repeat);
        alienTransparentMaterial1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienTransparentMaterial1.setTexture("ColorMap", alienTransparentTexture1);
        alienTransparentMaterial1.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

        alienTransparentTexture2 = assetManager.loadTexture("Textures/alien2_t.png");
        alienTransparentTexture2.setWrap(Texture.WrapMode.Repeat);
        alienTransparentMaterial2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienTransparentMaterial2.setTexture("ColorMap", alienTransparentTexture2);
        alienTransparentMaterial2.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

        alienTransparentTexture3 = assetManager.loadTexture("Textures/alien3_t.png");
        alienTransparentTexture3.setWrap(Texture.WrapMode.Repeat);
        alienTransparentMaterial3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienTransparentMaterial3.setTexture("ColorMap", alienTransparentTexture3);
        alienTransparentMaterial3.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

        alienTransparentTexture4 = assetManager.loadTexture("Textures/alien4_t.png");
        alienTransparentTexture4.setWrap(Texture.WrapMode.Repeat);
        alienTransparentMaterial4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienTransparentMaterial4.setTexture("ColorMap", alienTransparentTexture4);
        alienTransparentMaterial4.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

        materials = new Material[]{alienMaterial1, alienMaterial2, alienMaterial3, alienMaterial4};
        materials_t = new Material[]{alienTransparentMaterial1, alienTransparentMaterial2, alienTransparentMaterial3, alienTransparentMaterial4};

        greenGhostMaterial = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        greenGhostMaterial.setColor("Color", new ColorRGBA(0.32f, 0.85f, 0.5f, 1f));

        redGhostMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        redGhostMaterial.setColor("Color", new ColorRGBA(0.85f, 0.32f, 0.32f, 1f));
    }

    /**
     * Spawn a new alien at a specified location with textures.
     */
    @Override
    protected AlienNode instantiateAlien(Alien a, Vector3f location) {

        AlienNode node = super.instantiateAlien(a, location);

        // TEMP
        node.setMaterial(materials[a.materialCode]);

        return node;
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

    public boolean showArrow() {
        if (arrowGeometry == null) {
            createArrow();
        }
        if (!simRoot.hasChild(arrowGeometry)) {
            simRoot.attachChild(arrowGeometry);
        }
        return showArrow;
    }

    public boolean hideArrow() {
        if (arrowGeometry == null) {
            createArrow();
        }
        if (simRoot.hasChild(arrowGeometry)) {
            simRoot.detachChild(arrowGeometry);
        }
        return showArrow;
    }
}
