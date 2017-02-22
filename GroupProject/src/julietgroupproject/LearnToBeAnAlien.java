/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.controls.ActionListener;

/**
 *
 * @author GeorgeLenovo
 */
public class LearnToBeAnAlien extends SimpleApplication {
    public static void main(String[] args) {
        LearnToBeAnAlien app = new LearnToBeAnAlien();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //disable frame rate info etc
        setDisplayStatView(false);
        BulletAppState physics = new BulletAppState();
        this.stateManager.attach(physics);
        UIAppState uiAppState = new UIAppState(null, 1.0);
        this.stateManager.attach(uiAppState);
    }
}
