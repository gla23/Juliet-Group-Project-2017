/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.FastMath;
import static julietgroupproject.AlienBrain.MAX_VELOCITY;

/**
 *
 * @author Sunny
 */
public class AdvancedAlienBrain extends AlienBrain{
    
    // period (in seconds) = 2*PI*SINE_WAVE_PERIOD*fixedTimeStep
    private static double SINE_WAVE_PERIOD = 20.0;
    
    /**
     * {@inheritDoc}
     */
    public AdvancedAlienBrain() {
        super();
    }

    public AdvancedAlienBrain(float _accuracy, float _speed, double _updateInterval) {
        super(_accuracy, _speed, _updateInterval);
    }

    public AdvancedAlienBrain(float _accuracy, float _speed) {
        super(_accuracy, _speed);
    }

    public AdvancedAlienBrain(float _accuracy, float _speed, float _timeStep, double _updateInterval) {
        super(_accuracy, _speed, _timeStep, _updateInterval);
    }

    public AdvancedAlienBrain(float _accuracy, float _speed, float _timeStep) {
        super(_accuracy, _speed, _timeStep);
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
            double in;
            HingeJoint j = this.alien.joints.get(i);
            // check hinge limit and linearly map angle values
            // to doubles in 0.0~1.0
            if (j.getUpperLimit() < j.getLowerLimit()) {
                //no limit at all
                in = AlienHelper.normalise(j.getHingeAngle(),-Math.PI,Math.PI);
            } else {
                in = AlienHelper.normalise(j.getHingeAngle(),j.getLowerLimit(),j.getUpperLimit());
            }
            if (in < 0.0) {
                in = 0.0;
            }
            if (in > 1.0) {
                in = 1.0;
            }
            nnInput[i] = in;
        }

        // orientation input
        float[] ypr = this.alien.geometries.get(0).getControl(RigidBodyControl.class).getPhysicsRotation().toAngles(null);

        nnInput[nnInput.length - 4] = AlienHelper.normalise(ypr[0], -Math.PI, Math.PI);
        nnInput[nnInput.length - 3] = AlienHelper.normalise(ypr[1], -Math.PI, Math.PI);
        nnInput[nnInput.length - 2] = AlienHelper.normalise(ypr[2], -Math.PI, Math.PI);
        // sine wave input
        nnInput[nnInput.length - 1] = Math.sin(tick / SINE_WAVE_PERIOD);
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
        return a.joints.size() + 3 + 1;
    }

    @Override
    public int getOutputCount(AlienNode a) {
        return a.joints.size();
    }
}
