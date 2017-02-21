package julietgroupproject.GUI;


import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.Window;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import julietgroupproject.Editor;

public class MainMenuController extends AbstractAppState implements ScreenController {

  private Editor app;
  private AppStateManager stateManager;
  private Nifty nifty;
  private Screen screen;
    
  public MainMenuController(Editor App) {
      this.app = App;
  }
  
  public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
  }
  
  public void toggleGravOn() {
      app.toggleGravityOn();
  }
  
  public void toggleGravOff() {
      app.toggleGravityOff();
  }
  
  public void newBody() {
      app.createNewBody();
      
  }
   public void hideEditor() {
       nifty.gotoScreen("hidden");
       
   }
   
   public void showEditor(){
       nifty.gotoScreen("start");
   }
   
   public void newAlienButtonClick() {
       nifty.gotoScreen("start");
   }
   
   public void loadAlienButtonClick() {
       nifty.gotoScreen("loadAlien");
   }
   
   public void runPreviousSimulation() {
       //TODO
   }
   
   public void runNewSimulation() {
       //TODO
   }
   
   public void loadAlien() {
       //TODO
       nifty.getCurrentScreen().findElementByName("panel_background").startEffect(EffectEventId.onCustom);
       
   }
   
   public void saveAlien() {
       //TODO
   }
   
   public void editorOptions() {
       //TODO
       nifty.gotoScreen("editor_options");
       //Window window = nifty.getCurrentScreen().findNiftyControl("editor_options_window", Window.class);
       
   }
   
   public void addLimb() {
       app.addLimb();
       
       
   }
    public void onStartScreen() {
  }

    public void onEndScreen() {
  }
    
   public void toggleWireMesh() {
       app.toggleWireMesh();
   }
    
   public void setToCuboid() {
       app.setShapeToCuboid();
       Label currentShape = nifty.getCurrentScreen().findNiftyControl("currentShape", Label.class);
       currentShape.setText("Currently Selected: Cuboid");
   }
   
   public void toggleSmoothness() {
       app.toggleSmoothness();
   }
   
   public void setToSphere() {
       app.setShapeToSphere();
       Label currentShape = nifty.getCurrentScreen().findNiftyControl("currentShape", Label.class);
       currentShape.setText("Currently Selected: Sphere");
   }
   
   public void resetAlien() {
       app.resetAlien();
   }
   
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
   // this.app = app;
    this.stateManager = stateManager;
  }
}

