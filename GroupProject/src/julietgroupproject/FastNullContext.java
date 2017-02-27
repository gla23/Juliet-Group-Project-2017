/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.system.NullContext;

/**
 *
 * @author Sunny
 */
public class FastNullContext extends NullContext{
    
     @Override
     public void run(){
        initInThread();

        do {
            listener.update();
            /*
            if (frameRate > 0) {
                sync(frameRate);
            }*/
        } while (!needClose.get());

        deinitInThread();

        logger.fine("FastNullContext destroyed.");
    }
}
