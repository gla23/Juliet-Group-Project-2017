/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;

/**
 * The main application running at front end.
 * It handles input from user and uses
 * appropriate AppStates for alien editing/simulation.
 * 
 * @author GeorgeLenovo
 */
public class LearnToBeAnAlien extends SimpleApplication {
    
    //configurable constants
    public static final double SIM_SPEED = 1.0f;
    public static final double SIM_ACCURACY = 1f/60f;
    public static final double FIXED_TIME_STEP = 1.0/60.0;
    public static final int FRAMERATE = 60;
    
    public static void main(String[] args) {
        
        //Disable joint warnings TODO this doesn't seem to always be working on mac
        Logger physicslogger = Logger.getLogger(PhysicsSpace.class.getName());
        physicslogger.setUseParentHandlers(false);
        
        //make an instance off this class
        LearnToBeAnAlien app = new LearnToBeAnAlien();
        
        //show settings if running on mac
        app.setShowSettings((System.getProperty("os.name").toLowerCase()).contains("mac"));
        
        //display settings
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Learn To Be An Alien");
        
        if (true) {
            settings.put("Width", 1920*4/5);
            settings.put("Height",1080*4/5);
            settings.setFullscreen(false);
        }else{
            settings.put("Width", 1920);
            settings.put("Height",1080);
            settings.setFullscreen(true);
        }
        
        settings.setFrameRate(FRAMERATE);
        app.setSettings(settings);
        
        //start the application
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
        //disable info about rendering
        setDisplayStatView(false);
        
        //allow this application to perform physical simulation
        BulletAppState physics = new BulletAppState();
        this.stateManager.attach(physics);
        
        //make this application a user interface
        UIAppState uiAppState = new UIAppState(new SavedAlien(), SIM_SPEED, SIM_ACCURACY, FIXED_TIME_STEP);
        this.stateManager.attach(uiAppState);
    }
}
