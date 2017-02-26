/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

/**
 *
 * @author Sunny
 */
public class TestPhysics {

    public static int countJoint(Alien a) {
        return countJoint(a.rootBlock);
    }

    public static int countJoint(Block b) {
        Block root = b;
        int count = 0;
        for (Block child : root.getConnectedLimbs()) {
            count++;
            count += countJoint(child);
        }
        return count;
    }
/*
    private static class NoBrainSimulatorAppState extends TrainingAppState {

        public NoBrainSimulatorAppState(Alien _alien, Queue<SimulationData> q, double _simSpeed) {

            super(_alien, q, _simSpeed, 1f / 3000f);

        }

        @Override
        public void initialize(AppStateManager stateManager, Application app) {
            super.initialize(stateManager, app);
            /*stateManager.detach(physics);
            physics = new BulletAppState() {
                public void update(float tpf) {
                    super.update(tpf);
                    this.tpf = 1f / 60f;
                }
            };
            physics.setSpeed((float) this.simSpeed);
            stateManager.attach(physics);
            physics.setEnabled(false);
            PhysicsSpace pSpace = physics.getPhysicsSpace();
            pSpace.setAccuracy(1f / 300f);
            pSpace.setMaxSubSteps(200000);
        }

        @Override
        public void startSimulation(SimulationData data) {
            // turn physics back on
            this.physics.setEnabled(true);
            this.reset();
            this.currentSim = data;
            this.simTimeLimit = (float) data.getSimTime();
            this.currentAlienNode = instantiateAlien(this.alien, this.startLocation);
            //this.currentAlienNode.addControl(new AlienBrain(data.getToEvaluate(), physics.getPhysicsSpace().getAccuracy()));
            this.simInProgress = true;
        }
    }
*/
    public static Alien loadAlien(String filename) {
        File f = new File(filename);
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(f))) {
            Alien a = (Alien) o.readObject();
            return a;
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(UIAppState.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void main(String[] args) {
        //Disable joint warnings
        Logger physicslogger = Logger.getLogger(PhysicsSpace.class.getName());
        physicslogger.setUseParentHandlers(false);

        Alien a = loadAlien("aliens/test3/body.sav");
        ConcurrentLinkedQueue<SimulationData> q = new ConcurrentLinkedQueue<>();
        List<SimulationData> lsd = new ArrayList<>();
        int bgSimulatorCount = 1;
        int fgSimulatorCount = 1;
        int samplecount = 20;
        float accuracy = 1f / 60f;
        float bgSimTimeStep = 1f / 60f;
        final float defaultSpeed = 1.0f;
        final int defaultFramerate = 60;
        int bgSpeedUpFactor = 2;
        List<SlaveSimulator> list = new ArrayList<>();
        for (int i = 0; i < fgSimulatorCount; ++i) {
            // foreground simulators
            SlaveSimulator s = new SlaveSimulator(new TrainingAppState(a, q, defaultSpeed, accuracy));
            s.start(JmeContext.Type.Headless);
            list.add(s);
        }
        for (int i = 0; i < bgSimulatorCount; ++i) {
            // background simulators
            SlaveSimulator s = new SlaveSimulator(new TrainingAppState(a, q, defaultSpeed, accuracy, bgSimTimeStep));
            /*SlaveSimulator s = new SlaveSimulator(new NoBrainSimulatorAppState(a, q, 1.0) {
                @Override
                public void initialize(AppStateManager stateManager, Application app) {
                    super.initialize(stateManager, app);
                    PhysicsSpace pSpace = this.physics.getPhysicsSpace();
                    pSpace.setAccuracy(1f / 600f);
                    pSpace.setMaxSubSteps(2000);
                }
            });*/
            AppSettings set = new AppSettings(false);
            set.setFrameRate(defaultFramerate * bgSpeedUpFactor);
            s.setSettings(set);
            s.start(JmeContext.Type.Headless);
            list.add(s);
        }

        MLRegression nn = new BasicNetwork() {
            @Override
            public MLData compute(MLData input) {
                //System.out.println(Thread.currentThread().getId() + ": data #0:" + input.getData(0));
                double[] i = input.getData();
                double[] o = new double[i.length - 1];
                for (int j = 0; j < o.length; j++) {
                    double out;
                    if (i[j] > 0.4) {
                        out = i[j] * 0.5;
                    } else {
                        out = i[j] + 0.5;
                    }
                    o[j] = out;
                }
                return new BasicMLData(o);
            }
        };
        int jCount = countJoint(a);
        System.out.println("no of joints " + jCount);
        ((BasicNetwork) nn).addLayer(new BasicLayer(null, true, jCount + 1));
        ((BasicNetwork) nn).addLayer(new BasicLayer(new ActivationSigmoid(), false, jCount));
        ((BasicNetwork) nn).getStructure().finalizeStructure();


        final double[] fitness = new double[samplecount];
        for (int i = 0; i < samplecount; i++) {
            //final SimulationData d = new SimulationData(null, 10.0);
            final SimulationData d = new SimulationData(nn, 10.0);
            lsd.add(d);
            q.offer(d);
        }
        for (int i = 0; i < samplecount; i++) {
            fitness[i] = lsd.get(i).getFitness();
        }
        System.out.println(Arrays.toString(fitness));
        for (SlaveSimulator s : list) {
            s.stop();
        }
    }
}
