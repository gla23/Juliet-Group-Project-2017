/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;

/**
 * A background simulator dedicated to training process.
 * 
 * @author Sunny
 */
public class SlaveSimulator extends SimpleApplication {

    private BulletAppState bulletAppState;
    private SimulatorAppState simulatorAppState;

    public SlaveSimulator(SimulatorAppState state) {
        super(new StatsAppState());
        simulatorAppState = state;
    }
    
    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();

        stateManager.attach(bulletAppState);
        stateManager.attach(simulatorAppState);
        //stateManager.detach();
    }
    
    /**
     * Gracefully terminate this simulator after current
     * simulation finishes.
     */
    public void kill()
    {
        simulatorAppState.setToKill(true);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!this.stateManager.hasState(simulatorAppState))
        {
            System.out.println("Stopping myself");
            this.stop();
        }
    }
}
