package julietgroupproject.GUI;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.SliderChangedEvent;
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
        //TODO: factor out following code into a separate method
        addLimb = screen.findNiftyControl("add_limb_tab", Tab.class);
        addBody = screen.findNiftyControl("add_body_tab", Tab.class);
        tabs.setSelectedTab(addLimb);
        DropDown shapeSelect = nifty.getScreen("start").findNiftyControl("shape_selector", DropDown.class);
        shapeSelect.removeItem("Cuboid");
        shapeSelect.removeItem("Sphere");
        shapeSelect.removeItem("Cylinder");
        shapeSelect.removeItem("Torus");
        //shapeSelect.removeItem("Option3");
        shapeSelect.addItem("Cuboid");
        shapeSelect.addItem("Sphere");
        shapeSelect.addItem("Cylinder");
        shapeSelect.addItem("Torus");
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
                    this.sleep(150);
                    DropDown shapeSelect = nifty.getScreen("start").findNiftyControl("shape_selector_body", DropDown.class);

                    shapeSelect.removeItem("Cuboid");
                    shapeSelect.removeItem("Sphere");
                    shapeSelect.removeItem("Cylinder");
                    shapeSelect.removeItem("Torus");
                    //shapeSelect.removeItem("Option3");
                    shapeSelect.addItem("Cuboid");
                    shapeSelect.addItem("Sphere");
                    shapeSelect.addItem("Cylinder");
                    shapeSelect.addItem("Torus");
                    //shapeSelect.addItem("Option3");
                    shapeSelect.selectItemByIndex(0);
                    TabGroup tabs = nifty.getCurrentScreen().findNiftyControl("limb_body_tabs", TabGroup.class);
                    // tabs.addTab(addBody);
                    if (firstBody) {
                        tabs.setSelectedTab(addLimb);
                    } else {
                        tabs.setSelectedTab(addBody);
                    }

                } catch (InterruptedException e) {
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
        if (app.loadAlien("alien.sav")) {
            TabGroup tabs = nifty.getCurrentScreen().findNiftyControl("limb_body_tabs", TabGroup.class);
            addLimb = screen.findNiftyControl("add_limb_tab", Tab.class);
            addBody = screen.findNiftyControl("add_body_tab", Tab.class);
            tabs.setSelectedTab(addLimb);
            DropDown shapeSelect = nifty.getScreen("start").findNiftyControl("shape_selector", DropDown.class);
            shapeSelect.removeItem("Cuboid");
            shapeSelect.removeItem("Sphere");
            shapeSelect.removeItem("Cylinder");
            shapeSelect.removeItem("Torus");
            //shapeSelect.removeItem("Option3");
            shapeSelect.addItem("Cuboid");
            shapeSelect.addItem("Sphere");
            shapeSelect.addItem("Cylinder");
            shapeSelect.addItem("Torus");
            //TODO: inform user load was successful
        } else {
            //TODO: inform user load was unsuccessful
        }

    }

    public void saveAlien() {
        //TODO
        if (app.saveAlien("alien.sav")) {
            //TODO: inform user save was successful
        } else {
            //TODO: inform user save was unsuccessful
        }
    }

    public void editorOptions() {
        //TODO
        nifty.gotoScreen("editor_options");
        //Window window = nifty.getCurrentScreen().findNiftyControl("editor_options_window", Window.class);

    }

    public void attachLimb() {
        //TODO
    }

    public void saveLimb() {
        //TODO
    }

   @NiftyEventSubscriber(id="shape_selector")
    public void onDropDownLimbSelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event) {
        System.out.println(id);
        Element getWidth = nifty.getCurrentScreen().findElementByName("limbWidthSlider");
        Element getHeight = nifty.getCurrentScreen().findElementByName("limbHeightSlider");
        Element getLength = nifty.getCurrentScreen().findElementByName("limbLengthSlider");
        Element firstLabelE = nifty.getCurrentScreen().findElementByName("first_label");
        Element secondLabelE = nifty.getCurrentScreen().findElementByName("second_label");
        Element thirdLabelE = nifty.getCurrentScreen().findElementByName("third_label");

        Label firstLabel = nifty.getCurrentScreen().findNiftyControl("first_label", Label.class);
        Label secondLabel = nifty.getCurrentScreen().findNiftyControl("second_label", Label.class);
        Label thirdLabel = nifty.getCurrentScreen().findNiftyControl("third_label", Label.class);
        if (event.getSelection().equals("Cylinder")) {
            getWidth.show();
            getHeight.hide();
            getLength.show();
            firstLabelE.show();
            secondLabelE.hide();
            thirdLabelE.show();
            firstLabel.setText("Radius:");
            thirdLabel.setText("Height:");
        } else if (event.getSelection().equals("Sphere")) {
            getWidth.show();
            getHeight.hide();
            getLength.hide();
            firstLabelE.show();
            secondLabelE.hide();
            thirdLabelE.hide();
            firstLabel.setText("Radius:");
        } else if (event.getSelection().equals("Torus")) {
            getWidth.show();
            getHeight.hide();
            getLength.show();
            firstLabelE.show();
            secondLabelE.hide();
            thirdLabelE.show();
            firstLabel.setText("Ring Thickness:");
            thirdLabel.setText("Outer Radius:");
        } else if (event.getSelection().equals("Cuboid")) {
            getWidth.show();
            getHeight.show();
            getLength.show();
            firstLabelE.show();
            secondLabelE.show();
            thirdLabelE.show();
            firstLabel.setText("Width:");
            secondLabel.setText("Height:");
            thirdLabel.setText("Length:");

        }

    }
   
   @NiftyEventSubscriber(id="chaseCamCheckBox")
   public void onChaseCamChange(final String id, final CheckBoxStateChangedEvent event) {
       app.toggleSmoothness();
   }
   
   @NiftyEventSubscriber(id="wireMeshCheckBox")
   public void onWireMeshChange(final String id, final CheckBoxStateChangedEvent event) {
       app.toggleWireMesh();
   }
   
   @NiftyEventSubscriber(id="gravitySlider")
   public void onGravityChange(final String id, final SliderChangedEvent event) {
       app.setGravity(event.getValue());
   }
   
   @NiftyEventSubscriber(id="shape_selector_body")
   public void onDropDownBodySelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event) {
       System.out.println(id);
       Element getWidth = nifty.getCurrentScreen().findElementByName("bodyWidthSlider");
       Element getHeight = nifty.getCurrentScreen().findElementByName("bodyHeightSlider");
       Element getLength = nifty.getCurrentScreen().findElementByName("bodyLengthSlider");
       Element firstLabelE = nifty.getCurrentScreen().findElementByName("first_body_label");
       Element secondLabelE = nifty.getCurrentScreen().findElementByName("second_body_label");
       Element thirdLabelE = nifty.getCurrentScreen().findElementByName("third_body_label");
       
       Label firstLabel = nifty.getCurrentScreen().findNiftyControl("first_body_label", Label.class);
       Label secondLabel = nifty.getCurrentScreen().findNiftyControl("second_body_label", Label.class);
       Label thirdLabel = nifty.getCurrentScreen().findNiftyControl("third_body_label", Label.class);
       if (event.getSelection().equals("Cylinder")) {

            getWidth.show();
            getHeight.hide();
            getLength.show();
            firstLabelE.show();
            secondLabelE.hide();
            thirdLabelE.show();
            firstLabel.setText("Radius:");
            thirdLabel.setText("Height:");
        } else if (event.getSelection().equals("Sphere")) {
            getWidth.show();
            getHeight.hide();
            getLength.hide();
            firstLabelE.show();
            secondLabelE.hide();
            thirdLabelE.hide();
            firstLabel.setText("Radius:");
        } else if (event.getSelection().equals("Torus")) {
            getWidth.show();
            getHeight.hide();
            getLength.show();
            firstLabelE.show();
            secondLabelE.hide();
            thirdLabelE.show();
            firstLabel.setText("Ring Thickness:");
            thirdLabel.setText("Outer Radius:");
        } else if (event.getSelection().equals("Cuboid")) {
            getWidth.show();
            getHeight.show();
            getLength.show();
            firstLabelE.show();
            secondLabelE.show();
            thirdLabelE.show();
            firstLabel.setText("Width:");
            secondLabel.setText("Height:");
            thirdLabel.setText("Length:");
        }

    }

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
