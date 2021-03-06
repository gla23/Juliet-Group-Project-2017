/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import static julietgroupproject.AlienBrain.MAX_VELOCITY;

/**
 *
 * @author Sunny
 */
public class BasicAlienBrain extends AlienBrain {
    
    /**
     * {@inheritDoc}
     */
    public BasicAlienBrain() {
        super();
    }
    
    public BasicAlienBrain(float _accuracy, float _speed, double _updateInterval) {
        super(_accuracy,_speed,_updateInterval);
    }
    public BasicAlienBrain(float _accuracy, float _speed) {
        super(_accuracy,_speed);
    }
    public BasicAlienBrain(float _accuracy, float _speed, float _timeStep, double _updateInterval) {
        super(_accuracy,_speed,_timeStep,_updateInterval);
    }
    public BasicAlienBrain(float _accuracy, float _speed, float _timeStep) {
        super(_accuracy,_speed,_timeStep);
    }
    
    /**
     * Fetch physical information into the double array nnInput.
     */
    @Override
    protected void updateInput() {
        for (int i = 0; i < this.alien.joints.size(); i++) { //TODO class to describe input categories

            // normalise input to range from 0 to 1
            // Angles are in radians. 0 indicates no
            // rotation, positive and negative values
            // denote clockwise and anticlockwise rotations
            // (PS: I'm not sure about which is which though)
            double in = ((double) this.alien.joints.get(i).getHingeAngle()) / (2.0 * Math.PI) + 0.5;
            if (in < 0.0) {
                in = 0.0;
            }
            if (in > 1.0) {
                in = 1.0;
            }
            nnInput[i] = in;
        }

        double bearing = this.alien.geometries.get(0).getControl(RigidBodyControl.class).getPhysicsRotation().toAngles(null)[2];

        nnInput[nnInput.length - 1] = (bearing + Math.PI) / (2.0 * Math.PI);

        if ((bearing + Math.PI) / (2.0 * Math.PI) < 0.0 || (bearing + Math.PI) / (2.0 * Math.PI) > 1.0) {
            throw new RuntimeException("Angle normalisation incorrect");
        }
    }
    
    @Override
    protected void updateOutput(double[] nnOutput) {
        for (int i = 0; i < this.alien.joints.size(); i++) {
                HingeJoint j = this.alien.joints.get(i);
                j.getBodyA().activate();
                j.getBodyB().activate();
                float v = MAX_VELOCITY * (float) (2 * (nnOutput[i] - 0.5));
                float p = MAX_POWER;
                if (Math.abs(v) < MIN_VELOCITY) {
                    // try to stop moving
                    j.enableMotor(true, 0f, p);
                } else {
                    j.enableMotor(true, v, p);
                }
            }
    }

    @Override
    public int getInputCount(AlienNode a) {
        return a.joints.size() + 1;
    }

    @Override
    public int getOutputCount(AlienNode a) {
        return a.joints.size();
    }
}
