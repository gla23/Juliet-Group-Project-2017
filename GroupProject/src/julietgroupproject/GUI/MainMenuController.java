package julietgroupproject.GUI;


import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.Tab;
import de.lessvoid.nifty.controls.TabGroup;
import de.lessvoid.nifty.controls.Window;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import julietgroupproject.UIAppState;

public class MainMenuController extends AbstractAppState implements ScreenController {

  private UIAppState app;
  private AppStateManager stateManager;
  private Nifty nifty;
  private Screen screen;
  private Tab addLimb;
  private Tab addBody;
  private boolean firstBody = false;
  
    
  public MainMenuController(UIAppState App) {
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
      setCurrentBodyShape();
      app.createNewBody();
      firstBody = true;
      TabGroup tabs = nifty.getCurrentScreen().findNiftyControl("limb_body_tabs", TabGroup.class);
      screen = nifty.getCurrentScreen();
      addLimb = screen.findNiftyControl("add_limb_tab", Tab.class);
      addBody = screen.findNiftyControl("add_body_tab", Tab.class);
      tabs.setSelectedTab(addLimb);
      DropDown shapeSelect = nifty.getScreen("start").findNiftyControl("shape_selector", DropDown.class);
      shapeSelect.removeItem("Cuboid");
      shapeSelect.removeItem("Sphere");
      //shapeSelect.removeItem("Option3");
      shapeSelect.addItem("Cuboid");
      shapeSelect.addItem("Sphere");
      //shapeSelect.addItem("Option3");
      
      
  }
   public void hideEditor() {
       nifty.gotoScreen("hidden");
       
   }
   
   public void showEditor(){
       nifty.gotoScreen("start");
       
       addValues();
       
       
   }
   public void addValues() {
       Thread thread = new Thread() {
            public void run() {
                try {
                  this.sleep(75);DropDown shapeSelect = nifty.getScreen("start").findNiftyControl("shape_selector_body", DropDown.class);
                  shapeSelect.removeItem("Cuboid");
                  shapeSelect.removeItem("Sphere");
                  //shapeSelect.removeItem("Option3");
                  shapeSelect.addItem("Cuboid");
                  shapeSelect.addItem("Sphere");
                  //shapeSelect.addItem("Option3");
                  TabGroup tabs = nifty.getCurrentScreen().findNiftyControl("limb_body_tabs", TabGroup.class);
                // tabs.addTab(addBody);
                  if (firstBody) {
                        tabs.setSelectedTab(addLimb);
                  } else {
                      tabs.setSelectedTab(addBody);
                  }
         
               } catch (InterruptedException  e) {
                
               }
                
            }
        };
        thread.start();
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
       
       
   }
   
   public void saveAlien() {
       //TODO
   }
   
   public void editorOptions() {
       //TODO
       nifty.gotoScreen("editor_options");
       //Window window = nifty.getCurrentScreen().findNiftyControl("editor_options_window", Window.class);
       
   }
   /*
   public void addLimb() {
       app.addLimb();
       
       
   }*/
    public void onStartScreen() {
        
                
   }

    public void onEndScreen() {
  }
    
   public void toggleWireMesh() {
       app.toggleWireMesh();
   }
    
   public void setCurrentLimbShape() {
       DropDown shapeSelect = nifty.getCurrentScreen().findNiftyControl("shape_selector", DropDown.class);
       String current = "Box";
       current = (String) shapeSelect.getSelection();
       if (current.equals("Cuboid")) {
           current = "Box";
       }
      app.setCurrentShape(current);
       
   }
   
   public void setCurrentBodyShape() {
       DropDown shapeSelect = nifty.getCurrentScreen().findNiftyControl("shape_selector_body", DropDown.class);
       String current = "Box";
       current = (String) shapeSelect.getSelection();
       if (current.equals("Cuboid")) {
           current = "Box";
       }
      app.setCurrentShape(current);
       
   }
   
   public void toggleSmoothness() {
       app.toggleSmoothness();
   }
   
   public void resetAlien() {
       app.resetAlien();
       TabGroup tabs = nifty.getCurrentScreen().findNiftyControl("limb_body_tabs", TabGroup.class);
      // tabs.addTab(addBody);
       tabs.setSelectedTab(addBody);
        
       
   }
   
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
   // this.app = app;
    this.stateManager = stateManager;
  }
}

