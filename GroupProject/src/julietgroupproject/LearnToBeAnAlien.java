/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.input.controls.ActionListener;

/**
 *
 * @author GeorgeLenovo
 */
public class LearnToBeAnAlien extends SimpleApplication {
    public static void main(String[] args) {
        Editor app = new Editor();
        app.mainApplication = true;
        app.runningPhysics = false;
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
        UIAppState uiAppState = new UIAppState(null, 1.0);
        
    }
}
