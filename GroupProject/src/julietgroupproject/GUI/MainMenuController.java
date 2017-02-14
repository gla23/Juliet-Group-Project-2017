package julietgroupproject.GUI;


import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import julietgroupproject.Simulator;

public class MainMenuController extends AbstractAppState implements ScreenController {

  private Simulator app;
  private AppStateManager stateManager;
  private Nifty nifty;
  private Screen screen;
    
  public MainMenuController(Simulator App) {
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
   public void hideGUI() {
       nifty.exit();
       
   }
   
   public void addLimb() {
       app.addLimb();
       
       
   }
    public void onStartScreen() {
  }

    public void onEndScreen() {
  }
    
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
   // this.app = app;
    this.stateManager = stateManager;
  }
}

