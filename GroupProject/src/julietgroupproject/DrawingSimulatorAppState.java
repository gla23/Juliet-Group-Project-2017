/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import org.encog.ml.MLRegression;

/**
 *
 * @author Peter
 */
public class DrawingSimulatorAppState extends SimulatorAppState
{
    public DrawingSimulatorAppState(Alien alien)
    {
        super(alien);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        // TEMPORARY CODE!!!!
        setupTextures();
    }
    
    protected void initialiseWorld() {
        super.initialiseWorld();
        
        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        simRoot.addLight(light);
        viewPort.setBackgroundColor(new ColorRGBA(98 / 255f, 167 / 255f, 224 / 255f, 1f));
        super.floorGeometry.setMaterial(grassMaterial);
        floorGeometry.getMesh().scaleTextureCoordinates(new Vector2f(40,40));
    }
    
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
    }
    
    protected Brain instantiateAlien(Alien a, Vector3f location, MLRegression nn) {
        /*
         * Spawn a new alien at a specified location.
         */
        
        Brain b = super.instantiateAlien(a, location, nn);
        
        // TEMP
        b.nodeOfLimbGeometries.setMaterial(alienMaterial2);

        return b;
    }
    
}
