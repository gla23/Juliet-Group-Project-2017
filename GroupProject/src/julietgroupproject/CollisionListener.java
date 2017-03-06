/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;

/**
 *
 * @author Peter
 */
public class CollisionListener implements PhysicsCollisionListener {

    private boolean isColliding = false;
    
    public CollisionListener(PhysicsSpace ps)
    {
        ps.addCollisionListener(this);
    }

    @Override
    public void collision(PhysicsCollisionEvent pce) {
        isColliding = true;
    }

    public boolean getIsColliding() {
        return isColliding;
    }

    public void resetIsColliding() {
        isColliding = false;
    }
}
