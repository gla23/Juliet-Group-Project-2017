/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.bullet.BulletAppState;

/**
 * BulletAppState with fixed physics time step for each frame
 * @author Sunny
 */
public class FixedTimeStepBulletAppState extends BulletAppState {
    
    private final float fixedTimeStep;
    public FixedTimeStepBulletAppState(float _fixedTimeStep) {
        this.fixedTimeStep = _fixedTimeStep;
        this.tpf = this.fixedTimeStep;
    }
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
        this.tpf = fixedTimeStep;
    }
    
}
