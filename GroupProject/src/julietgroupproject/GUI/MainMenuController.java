package julietgroupproject.GUI;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;


import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.controls.Tab;
import de.lessvoid.nifty.controls.TabGroup;
import de.lessvoid.nifty.controls.TextField;

import de.lessvoid.nifty.controls.ScrollPanel;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.controls.Tab;
import de.lessvoid.nifty.controls.TabGroup;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;

import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.awt.Panel;

import java.util.ArrayList;

import java.util.Arrays;
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

    private String[] aliens;
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
    
    public void toggleArrow() {
        app.toggleArrow();
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
    }
    
    public void addLoadValues(final String[] aliens) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //Shhhhhh..... there's nothing to see here
                    boolean working = false;
                    Element loadScrollE = null;
                    while (!working) {
                        this.sleep(20);
                            loadScrollE = nifty.getCurrentScreen().findElementByName("loadScrollPanel");
                            
                        try {
                            loadScrollE.getClass();
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
                            addLoadButton(loadScrollE, aliens[i]);
                        }
                        alreadyAddedAliens.add(aliens[i]);
                    }
                    

                    initialising = false;
                } catch (InterruptedException e) {
                } catch (NullPointerException e2) {
                    System.out.println("HERE2");
                }

            }
        };
        thread.start();
    }
    

    public void addLoadButton(Element scroll, final String alien) {
        System.out.println(alien);
        scroll.add(new ButtonBuilder(alien+"LoadBut", alien){{

            //childLayoutCenter();
            valignTop();
            paddingTop("0px");
            paddingBottom("0px");
            marginTop("0px");
            marginBottom("0px");
            width("100%");

            //height("30px");
            //visibleToMouse(true);
            interactOnClick("handleControlOnClick(" + alien + ")");
            }}.build(nifty, nifty.getCurrentScreen(), scroll));
    }
    
    public void handleControlOnClick(String alienID) {
        confirmLoad(alienID);
        System.out.println(alienID);
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

                    textureBox.removeItem("Alien1");
                    textureBox.removeItem("Alien2");
                    textureBox.removeItem("Alien3");
                    textureBox.removeItem("Alien4");

                    textureBox.addItem("Alien1");
                    textureBox.addItem("Alien2");
                    textureBox.addItem("Alien3");
                    textureBox.addItem("Alien4");

                    String tex = "Alien1";
                    int textNo = app.getTextureNo();
                    if (textNo == 0) {
                        tex = "Alien1";
                    } else if (textNo == 1) {
                        tex = "Alien2";
                    } else if (textNo == 2) {
                        tex = "Alien3";
                    } else if (textNo == 3) {
                        tex = "Alien4";
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
                    while (!working) {
                        this.sleep(20);
                        shapeSelect = nifty.getScreen("start").findNiftyControl("shape_selector_body", DropDown.class);
                        try {
                            shapeSelect.removeItem("");
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
                    TabGroup tabs = nifty.getCurrentScreen().findNiftyControl("limb_body_tabs", TabGroup.class);
                    addLimb = nifty.getScreen("start").findNiftyControl("add_limb_tab", Tab.class);
                    addBody = nifty.getScreen("start").findNiftyControl("add_body_tab", Tab.class);

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
        if (app.beginTraining())
            nifty.gotoScreen("simulation");
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

       //String[] aliens = app.getLoadableAliens();
        //aliens = new String[]{"first","second","third"};
        addLoadValues(aliens);
        //ScrollPanel loadScroll = nifty.getCurrentScreen().findNiftyControl("loadScrollBar", ScrollPanel.class);
       /* Element loadScrollE = nifty.getCurrentScreen().findElementByName("loadScrollbar");
                

        loadScrollE.add(new ButtonBuilder("firstBut", aliens[0]){{
        childLayoutCenter();
        visibleToMouse(true);
        interactOnClick("handleControlOnClick(" + "testAlien" + ")");
        }}.build(nifty, nifty.getCurrentScreen(), loadScrollE));
        
        */
        
        
        System.out.println(Arrays.toString(aliens));
    }

    public void confirmLoad() {
        app.addKeyBindings();
        if ("alien".equals(loadType)) {
            //TODO
            if (app.loadAlien(sanitizeAlienName(nifty.getScreen("load_dialog").findNiftyControl("loadTextField", TextField.class).getRealText()))) {
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
    
    public void confirmLoad(String givenAlien) {
        app.addKeyBindings();
        if ("alien".equals(loadType)) {
            //TODO
            if (app.loadAlien(sanitizeAlienName(givenAlien))) {
                nifty.gotoScreen("start");
                screen = nifty.getScreen("start");
                firstBody = true;
                addAlienSpecificOptions();
                addValues();
                //TODO: inform user load was successful
            } else {
                nifty.gotoScreen("load_fail");
                //TODO: inform user load was unsuccessful
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
            if (selectedTex.equals("Alien1")) {
                textno = 0;
            } else if (selectedTex.equals("Alien2")) {
                textno = 1;
            } else if (selectedTex.equals("Alien3")) {
                textno = 2;
            } else if (selectedTex.equals("Alien4")) {
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
