package julietgroupproject.GUI;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.controls.Tab;
import de.lessvoid.nifty.controls.TabGroup;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import julietgroupproject.DrawGraph;
import julietgroupproject.UIAppState;

public class MainMenuController extends AbstractAppState implements ScreenController {

    private UIAppState app;
    private AppStateManager stateManager;
    private Nifty nifty;
    private Screen screen;
    private Tab addLimb;
    private Tab addBody;
    private boolean firstBody = false;
    private boolean X = true;
    private boolean Y = false;
    private boolean Z = false;
    private boolean auto = false;
    private volatile boolean initialising = false;
    private String saveType = "";
    private String loadType = "";
    private boolean showArrow = true;

    private String[] aliens;
    private String currentlySelectedLoadAlien = "";
    private ArrayList<String> alreadyAddedAliens = new ArrayList<String>();


    public MainMenuController(UIAppState App) {
        this.app = App;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }
    
    public void newBody() {
        setCurrentBodyShape();
        app.createNewBody();
        addAlienSpecificOptions();
    }
    

    public void addAlienSpecificOptions() {
        firstBody = true;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //Shhhhhh..... there's nothing to see here
                    boolean working = false;
                    DropDown shapeSelect = null;
                    TabGroup tabs = null;
                    while (!working) {
                        this.sleep(20);
                        shapeSelect = nifty.getScreen("start").findNiftyControl("shape_selector", DropDown.class);
                        try {
                            shapeSelect.removeItem("");
                            tabs = nifty.getCurrentScreen().findNiftyControl("limb_body_tabs", TabGroup.class);
                            addLimb = nifty.getScreen("start").findNiftyControl("add_limb_tab", Tab.class);
                            addBody = nifty.getScreen("start").findNiftyControl("add_body_tab", Tab.class);
                            tabs.setSelectedTab(addLimb);
                            working = true;
                        } catch (NullPointerException e) {
                        }
                    }

                    shapeSelect.removeItem("Cuboid");
                    shapeSelect.removeItem("Ellipsoid");
                    shapeSelect.removeItem("Cylinder");
                    shapeSelect.removeItem("Torus");
                    shapeSelect.addItem("Cuboid");
                    shapeSelect.addItem("Ellipsoid");
                    shapeSelect.addItem("Cylinder");
                    shapeSelect.addItem("Torus");
                    shapeSelect.selectItemByIndex(0);
                    tabs.setSelectedTab(addLimb);

                } catch (InterruptedException e) {
                }

            }
        };
        thread.start();
    }

    public void hideEditor() {
        nifty.gotoScreen("hidden");
    }

    public void showEditor() {
        nifty.gotoScreen("start");

        addValues();
        app.addKeyBindings();
    }

    public void stopSimulation() {
        this.app.endTraining();
        nifty.gotoScreen("start");
        addValues();
        if (!showArrow) showArrow = app.hideArrow();
    }
    
    public void addLoadValues(final String[] aliens) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //Shhhhhh..... there's nothing to see here
                    boolean working = false;
                    DropDown loadScrollE = null;
                    while (!working) {
                        this.sleep(20);
                            loadScrollE = nifty.getCurrentScreen().findNiftyControl("alien_selector", DropDown.class);
                            
                        try {
                            loadScrollE.removeItem("");
                            working = true;
                        } catch (NullPointerException e) {
                        }
                    }

                    initialising = true;
                    //loadScrollE.setHeight(200);
                    System.out.println("Aliens " + Arrays.toString(aliens));
                    System.out.println("Already Added: "  + alreadyAddedAliens.toString());
                    for (int i =0; i<aliens.length; i++) {
                        if (!alreadyAddedAliens.contains(aliens[i])) {
                            System.out.println(i);
                            loadScrollE.addItem(aliens[i]);
                        }
                        alreadyAddedAliens.add(aliens[i]);
                    }
                    loadScrollE.selectItemByIndex(0);
                    
                    initialising = false;
                } catch (InterruptedException e) {
                } catch (NullPointerException e2) {
                    System.out.println("HERE2");
                }

            }
        };
        thread.start();
    }
   
    

    public void addOptionsValues() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //Shhhhhh..... there's nothing to see here
                    boolean working = false;
                    DropDown textureBox = null;
                    while (!working) {
                        this.sleep(20);
                        textureBox = nifty.getScreen("editor_options").findNiftyControl("textureDropDown", DropDown.class);
                        try {
                            textureBox.removeItem("");
                            working = true;
                        } catch (NullPointerException e) {
                        }
                    }

                    initialising = true;

                    textureBox.removeItem("Plant");
                    textureBox.removeItem("Snake");
                    textureBox.removeItem("Mosaic");
                    textureBox.removeItem("Zebra");

                    textureBox.addItem("Plant");
                    textureBox.addItem("Snake");
                    textureBox.addItem("Mosaic");
                    textureBox.addItem("Zebra");

                    String tex = "Zebra";
                    int textNo = app.getTextureNo();
                    if (textNo == 0) {
                        tex = "Plant";
                    } else if (textNo == 1) {
                        tex = "Snake";
                    } else if (textNo == 2) {
                        tex = "Mosaic";
                    } else if (textNo == 3) {
                        tex = "Zebra";
                    }
                    textureBox.selectItem(tex);

                    initialising = false;
                } catch (InterruptedException e) {
                }

            }
        };
        thread.start();
    }

    public void addValues() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //Shhhhhh..... there's nothing to see here
                    boolean working = false;
                    DropDown shapeSelect = null;
                    TabGroup tabs = null;
                    while (!working) {
                        this.sleep(20);
                        shapeSelect = nifty.getScreen("start").findNiftyControl("shape_selector_body", DropDown.class);
                        tabs = nifty.getCurrentScreen().findNiftyControl("limb_body_tabs", TabGroup.class);
                        addLimb = nifty.getScreen("start").findNiftyControl("add_limb_tab", Tab.class);
                        addBody = nifty.getScreen("start").findNiftyControl("add_body_tab", Tab.class);
                        try {
                            shapeSelect.removeItem("");
                            tabs.getSelectedTabIndex();
                            working = true;
                        } catch (NullPointerException e) {
                        }
                    }

                    shapeSelect.removeItem("Cuboid");
                    shapeSelect.removeItem("Ellipsoid");
                    shapeSelect.removeItem("Cylinder");
                    shapeSelect.removeItem("Torus");
                    shapeSelect.addItem("Cuboid");
                    shapeSelect.addItem("Ellipsoid");
                    shapeSelect.addItem("Cylinder");
                    shapeSelect.addItem("Torus");
                    shapeSelect.selectItemByIndex(0);

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

    public void resetTraining() {
        if (app.resetTraining()) {
            nifty.gotoScreen("training_reset_success");
        } else {
            nifty.gotoScreen("training_reset_fail");
        }
    }

    public void startTraining() {
        if (app.beginTraining()) {
            nifty.gotoScreen("simulation");
            showArrow = true;
        }
        else
            nifty.gotoScreen("simulate_fail");
    }

    private String sanitizeAlienName(String rawName) {
        String sanitizedAlienName;
        sanitizedAlienName = rawName.replaceAll("[^A-Za-z0-9 \\-_]", "");
        return sanitizedAlienName;
    }
    
    public void gotoLoadScreen() {
        loadType = "alien";
        app.removeKeyBindings();
        nifty.gotoScreen("load_dialog");
           
        

        addLoadValues(aliens);
        
        
        System.out.println(Arrays.toString(aliens));
    }
    
    @NiftyEventSubscriber (id = "alien_selector")
    public void setCurrentLoadAlien(final String id, final DropDownSelectionChangedEvent<String> event) {
        currentlySelectedLoadAlien = event.getSelection();
    }

    public void confirmLoad() {
        app.addKeyBindings();
        
        if ("alien".equals(loadType)) {
            System.out.println(currentlySelectedLoadAlien);
            //TODO
            if (app.loadAlien(sanitizeAlienName(currentlySelectedLoadAlien))) {
                nifty.gotoScreen("start");
                screen = nifty.getScreen("start");
                firstBody = true;
                addAlienSpecificOptions();
                //addValues();
            } else {
                nifty.gotoScreen("load_fail");
            }
        }
    }
   
    public void gotoSaveScreen() {
        this.saveType = "alien";
        app.removeKeyBindings();
        nifty.gotoScreen("save_dialog");
    }

    public void confirmSave() {
        app.addKeyBindings();
        if ("alien".equals(saveType)) {

            String name = screen.findNiftyControl("saveTextField", TextField.class).getRealText();
            if (app.saveAlien(sanitizeAlienName(name))) {
                ArrayList<String> aliensTemp = new ArrayList<String>(Arrays.asList(aliens));
                aliensTemp.add(name);
                
                aliensTemp.toArray(aliens);
                nifty.gotoScreen("save_success");
                //TODO: inform user save was successful

            } else {
                nifty.gotoScreen("save_fail");
            }
        }
        //TODO limbs
    }

    public void editorOptions() {
        //TODO
        nifty.gotoScreen("editor_options");
        //Window window = nifty.getCurrentScreen().findNiftyControl("editor_options_window", Window.class);
        addOptionsValues();
        nifty.getScreen("editor_options").findNiftyControl("directionArrowCheckBox", CheckBox.class).setChecked(showArrow);
    }
    
    public boolean arrowShown() {
        return showArrow;
    }

    public void attachLimb() {
        //TODO     
        /*Slider getWidth = nifty.getCurrentScreen().findNiftyControl("limbWidthSlider", Slider.class);
        getWidth.setValue(5.0f);*/
        /*CheckBox getAuto = nifty.getCurrentScreen().findNiftyControl("AutoCheckBox", CheckBox.class);

        boolean checked = !getAuto.isChecked();

        app.setAttaching(checked);
        getAuto.setChecked(checked);*/
    }

    public void setLimbCheckbox() {
    }

    public void saveLimb() {
        this.saveType = "limb";
        nifty.gotoScreen("save");
    }

    public void pulsateToggle() {
        nifty.getScreen("start").findElementByName("panel_background").startEffect(EffectEventId.onCustom);
    }

    @NiftyEventSubscriber(id = "textureDropDown")
    public void onDropDownTextureSelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event) {
        if (!initialising) {
            String selectedTex = event.getSelection();
            int textno = 3;
            if (selectedTex.equals("Plant")) {
                textno = 0;
            } else if (selectedTex.equals("Snake")) {
                textno = 1;
            } else if (selectedTex.equals("Mosaic")) {
                textno = 2;
            } else if (selectedTex.equals("Zebra")) {
                textno = 3;
            }
            app.setTexture(textno);
        }
    }

    @NiftyEventSubscriber(id = "shape_selector")
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
        switch (event.getSelection()) {
            case "Cylinder":
                getWidth.show();
                getHeight.hide();
                getLength.show();
                firstLabelE.show();
                secondLabelE.hide();
                thirdLabelE.show();
                firstLabel.setText("Height:");
                thirdLabel.setText("Radius:");
                break;
            case "Ellipsoid":
                getWidth.show();
                getHeight.show();
                getLength.show();
                firstLabelE.show();
                secondLabelE.show();
                thirdLabelE.show();
                firstLabel.setText("X-Radius:");
                secondLabel.setText("Y-Radius");
                thirdLabel.setText("Z-Radius");
                break;
            case "Torus":
                getWidth.show();
                getHeight.hide();
                getLength.show();
                firstLabelE.show();
                secondLabelE.hide();
                thirdLabelE.show();
                firstLabel.setText("Outer Radius:");
                thirdLabel.setText("Ring Thickness:");
                break;
            case "Cuboid":
                getWidth.show();
                getHeight.show();
                getLength.show();
                firstLabelE.show();
                secondLabelE.show();
                thirdLabelE.show();

                firstLabel.setText("Width:");
                secondLabel.setText("Height:");
                thirdLabel.setText("Length:");
                break;
        }

    }

    @NiftyEventSubscriber(id = "chaseCamCheckBox")
    public void onChaseCamChange(final String id, final CheckBoxStateChangedEvent event) {
        app.toggleSmoothness();
    }

    public void restartAlien() {
        app.restartAlien();
    }

    @NiftyEventSubscriber(id = "hingeAxisButtons")
    public void onXChange(final String id, final RadioButtonGroupStateChangedEvent event) {
        switch (event.getSelectedId()) {
            case "XCheckBox":
                app.setCurrentHingeAxis("XAxis");
                break;
            case "YCheckBox":
                app.setCurrentHingeAxis("YAxis");
                break;
            case "ZCheckBox":
                app.setCurrentHingeAxis("ZAxis");
                break;
            case "AutoCheckBox":
                app.setCurrentHingeAxis("A");
                break;
        }

    }

    @NiftyEventSubscriber(id = "wireMeshCheckBox")
    public void onWireMeshChange(final String id, final CheckBoxStateChangedEvent event) {
        app.toggleWireMesh();
        //makeGraph();
    }
    
    @NiftyEventSubscriber(id = "directionArrowCheckBox")
    public void onDirectionArrowChange(final String id, final CheckBoxStateChangedEvent event) {
        showArrow = event.getCheckBox().isChecked();
        if (showArrow) {
            showArrow = app.showArrow();
        } else {
            showArrow = app.hideArrow();
        }
    }

    @NiftyEventSubscriber(id = "gravitySlider")
    public void onGravityChange(final String id, final SliderChangedEvent event) {
        app.setGravity(event.getValue());
    }

    @NiftyEventSubscriber(id = "shape_selector_body")
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
        switch (event.getSelection()) {
            case "Cylinder":
                getWidth.show();
                getHeight.hide();
                getLength.show();
                firstLabelE.show();
                secondLabelE.hide();
                thirdLabelE.show();
                firstLabel.setText("Radius:");
                thirdLabel.setText("Height:");
                break;
            case "Ellipsoid":
                getWidth.show();
                getHeight.show();
                getLength.show();
                firstLabelE.show();
                secondLabelE.show();
                thirdLabelE.show();
                firstLabel.setText("X-Radius:");
                secondLabel.setText("Y-Radius");
                thirdLabel.setText("Z-Radius");
                break;
            case "Torus":
                getWidth.show();
                getHeight.hide();
                getLength.show();
                firstLabelE.show();
                secondLabelE.hide();
                thirdLabelE.show();
                firstLabel.setText("Ring Thickness:");
                thirdLabel.setText("Outer Radius:");
                break;
            case "Cuboid":
                getWidth.show();
                getHeight.show();
                getLength.show();
                firstLabelE.show();
                secondLabelE.show();
                thirdLabelE.show();
                firstLabel.setText("Width:");
                secondLabel.setText("Height:");
                thirdLabel.setText("Length:");
                break;
        }

    }

    @Override
    public void onStartScreen() {
        aliens = app.getLoadableAliens();        
    }
    
    public void makeGraph(List<Float> data) {
        DrawGraph test = new DrawGraph(data, "assets/Graphs/test1.png");
        test.showIt();
    }

    @Override
    public void onEndScreen() {
    }

    public void toggleWireMesh() {
        app.toggleWireMesh();
    }

    @NiftyEventSubscriber (id = "shape_selector")
    public void setCurrentLimbShape(final String id, final DropDownSelectionChangedEvent<String> event) {
        String current = event.getSelection();
        switch (current) {
            case "Cuboid":
                current = "Box";
                break;
            case "Ellipsoid":
                current = "Sphere";
                break;
        }
        app.setCurrentShape(current);

    }

    public void setCurrentBodyShape() {
        DropDown shapeSelect = nifty.getCurrentScreen().findNiftyControl("shape_selector_body", DropDown.class);
        String current = (String) shapeSelect.getSelection();
        switch (current) {
            case "Cuboid":
                current = "Box";
                break;
            case "Ellipsoid":
                current = "Sphere";
                break;
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
