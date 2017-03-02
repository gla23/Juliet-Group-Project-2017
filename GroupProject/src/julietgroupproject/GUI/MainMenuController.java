package julietgroupproject.GUI;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.RadioButton;
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
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import julietgroupproject.DrawGraph;
import julietgroupproject.UIAppState;

public class MainMenuController extends AbstractAppState implements ScreenController {

    private UIAppState app;
    private SimpleApplication mainApp;
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
    private boolean showArrow = true;
    boolean alienNamed = false;
    private String[] aliens;
    private String currentlySelectedLoadAlien = "";
    private ArrayList<String> alreadyAddedAliens = new ArrayList<String>();

    public MainMenuController(UIAppState App) {
        this.app = App;
        this.mainApp = app.getApp();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void newBody() {
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
        if (alienNamed) {
            app.addKeyBindings();
            nifty.gotoScreen("start");
            addValues();
            checkPrevSim();
        } else {
            nifty.gotoScreen("begin");
            checkPrevSim();
        }
    }

    public void checkPrevSim() {
        Button resume = nifty.getScreen("start").findNiftyControl("new_sim", Button.class);
        if (app.savedAlien.pop != null) {

            resume.setText("Resume Training");
        } else {
            resume.setText("Start Training");
        }
    }

    public void stopSimulation() {
        this.app.endSimulation();
        nifty.gotoScreen("start");
        addValues();
        checkPrevSim();
        if (!showArrow) {
            showArrow = app.hideArrow();
        }
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
                    System.out.println("Already Added: " + alreadyAddedAliens.toString());
                    for (int i = 0; i < aliens.length; i++) {
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
        } else {
            nifty.gotoScreen("simulate_fail");
        }
    }

    public void showBest() {
        if (this.app.savedAlien.savedEntryCount() > 0) {
            nifty.gotoScreen("simulation");
            this.app.showBest();
        } else {
            nifty.gotoScreen("show_best_fail");
        }
    }

    private String sanitizeAlienName(String rawName) {
        String sanitizedAlienName;
        sanitizedAlienName = rawName.replaceAll("[^A-Za-z0-9 \\-_]", "");
        return sanitizedAlienName;
    }

    public void gotoLoadScreen() {
        app.removeKeyBindings();
        nifty.gotoScreen("load_dialog");



        addLoadValues(aliens);


        System.out.println(Arrays.toString(aliens));
    }

    public void gotoNameScreen() {
        app.removeKeyBindings();
        nifty.gotoScreen("name_dialog");
    }

    public void confirmName() {
        app.addKeyBindings();

        String name = nifty.getScreen("name_dialog").findNiftyControl("nameTextField", TextField.class).getRealText();
        String sanitizedName = sanitizeAlienName(name);
        if (sanitizedName.length() > 0) {
            app.savedAlien.setName(sanitizedName);
            alienNamed = true;
            app.savedAlien.alienChanged();
            showEditor();
            showArrow = app.showArrow();
        }
    }

    @NiftyEventSubscriber(id = "alien_selector")
    public void setCurrentLoadAlien(final String id, final DropDownSelectionChangedEvent<String> event) {
        currentlySelectedLoadAlien = event.getSelection();
    }

    public void confirmLoad() {
        app.addKeyBindings();

        System.out.println(currentlySelectedLoadAlien);
        //TODO
        if (app.loadAlien(sanitizeAlienName(currentlySelectedLoadAlien))) {
            alienNamed = true;
            app.addKeyBindings();
            nifty.gotoScreen("start");
            checkPrevSim();
            screen = nifty.getScreen("start");
            firstBody = true;
            addAlienSpecificOptions();
            addValues();
            showArrow = app.showArrow();
        } else {
            nifty.gotoScreen("load_fail");
        }
    }

    public void gotoSaveScreen() {
        app.removeKeyBindings();
        Thread thread = new Thread() {
            @Override
            public void run() {
                boolean worked = false;
                while (!worked) {
                    try {
                        nifty.getScreen("save_dialog").findNiftyControl("saveTextField", TextField.class).setText(app.savedAlien.getName());
                        worked = true;
                    } catch (NullPointerException e) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        worked = false;
                    }
                }
            }
        };
        thread.start();
        nifty.gotoScreen("save_dialog");
    }

    public void confirmSave() {

        System.out.println("save confirmed");

        app.addKeyBindings();

        String name = nifty.getScreen("save_dialog").findNiftyControl("saveTextField", TextField.class).getRealText();
        String sanitizedName = sanitizeAlienName(name);
        if (sanitizedName.length() > 0 && app.saveAlien(sanitizedName)) {
            alienNamed = true;
            ArrayList<String> aliensTemp = new ArrayList<String>(Arrays.asList(aliens));
            aliensTemp.add(name);

            aliensTemp.toArray(aliens);
            nifty.gotoScreen("save_success");
        } else {
            nifty.gotoScreen("save_fail");
        }
    }

    public void editorOptions() {
        //TODO
        nifty.gotoScreen("editor_options");
        //Window window = nifty.getCurrentScreen().findNiftyControl("editor_options_window", Window.class);
        addOptionsValues();
        nifty.getScreen("editor_options").findNiftyControl("directionArrowCheckBox", CheckBox.class).setChecked(showArrow);
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
        String current = event.getSelection();
        switch (current) {
            case "Cuboid":
                current = "Box";
                break;
            case "Ellipsoid":
                current = "Sphere";
                break;
        }
        setFieldSafe("currentLimbShape",current);
    }

    @NiftyEventSubscriber(id = "chaseCamCheckBox")
    public void onChaseCamChange(final String id, final CheckBoxStateChangedEvent event) {
        app.toggleSmoothness();
    }

    public void restartAlien() {
        app.restartAlien();
    }

    @NiftyEventSubscriber(id = "logger_listbox")
    public void logItemSelected(final String id, final ListBoxSelectionChangedEvent<julietgroupproject.GenerationResult> event) {
        System.out.println("Callback!" + event.getSelection().size());
        if (event.getSelection().size() > 0) {
            app.showOffGeneration(event.getSelection().get(0).generation);
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

        String current = event.getSelection();
        switch (current) {
            case "Cuboid":
                current = "Box";
                break;
            case "Ellipsoid":
                current = "Sphere";
                break;
        }
        setFieldSafe("currentBodyShape",current);

    }

    @NiftyEventSubscriber(id = "bodyWidthSlider")
    public void onBodyWidthSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("bodyWidth", event.getValue());
    }

    @NiftyEventSubscriber(id = "bodyHeightSlider")
    public void onBodyHeightSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("bodyHeight", event.getValue());
    }

    @NiftyEventSubscriber(id = "bodyLengthSlider")
    public void onBodyLengthSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("bodyLength", event.getValue());
    }

    @NiftyEventSubscriber(id = "bodyWeightSlider")
    public void onBodyWeightSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("bodyWeight", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbWidthSlider")
    public void onLimbWidthSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbWidth", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbHeightSlider")
    public void onLimbHeightSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbHeight", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbLengthSlider")
    public void onLimbLengthSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbLength", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbWeightSlider")
    public void onLimbWeightSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbWeight", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbFrictionSlider")
    public void onLimbFrictionSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbFriction", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbStrengthSlider")
    public void onLimbStrengthSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbStrength", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbSeparationSlider")
    public void onLimbSeparationSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbSeparation", event.getValue());
    }

    @NiftyEventSubscriber(id = "symmetricCheckBox")
    public void onSymmetricCheckBoxChanged(final String id, final CheckBoxStateChangedEvent event) {
        this.setFieldSafe("symmetric", event.isChecked());
    }

    @NiftyEventSubscriber(id = "hingeAxisButtons")
    public void onHingeAxisButtonChanged(final String id, final RadioButtonGroupStateChangedEvent event) {
        String selected;
        switch (event.getSelectedId()) {
            case "AutoCheckBox":
                selected = "A";
                break;
            case "XCheckBox":
                selected = "XAxis";
                break;
            case "YCheckBox":
                selected = "YAxis";
                break;
            case "ZCheckBox":
                selected = "ZAxis";
                break;
            default:
                selected = "A";
        }
        this.setFieldSafe("currentHingeAxis", selected);
    }
    
    @NiftyEventSubscriber(id = "yawSlider")
    public void onLimbYawSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbYaw", event.getValue());
    }

    @NiftyEventSubscriber(id = "pitchSlider")
    public void onLimbPitchSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbPitch", event.getValue());
    }

    @NiftyEventSubscriber(id = "rollSlider")
    public void onLimbRollSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("limbRoll", event.getValue());
    }

    @NiftyEventSubscriber(id = "jointPosSlider")
    public void onJointPosSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("jointPositionFraction", event.getValue());
    }

    @NiftyEventSubscriber(id = "jointRotSlider")
    public void onJointRotSliderChanged(final String id, final SliderChangedEvent event) {
        this.setFieldSafe("jointStartRotation", event.getValue());
    }

    @Override
    public void onStartScreen() {
        aliens = app.getLoadableAliens();
    }

    public void makeGraph(List<Float> data) {
        /*
         DrawGraph test = new DrawGraph(data, "assets/Graphs/test1.png");
         test.showIt();*/
    }

    @Override
    public void onEndScreen() {
    }

    public void toggleWireMesh() {
        app.toggleWireMesh();
    }

    public void toggleSmoothness() {
        app.toggleSmoothness();
    }

    public void resetAlien() {
        app.resetAlien();
        alienNamed = false;

        app.removeKeyBindings();
        nifty.gotoScreen("name_dialog");

        TabGroup tabs = nifty.getScreen("start").findNiftyControl("limb_body_tabs", TabGroup.class);
        // tabs.addTab(addBody);
        tabs.setSelectedTab(addBody);


    }

    private void setFieldSafe(final String fieldName, final String value) {
        mainApp.enqueue(Executors.callable(new Runnable() {
            @Override
            public void run() {
                app.setNiftyField(fieldName, value);
            }
        }));
    }

    private void setFieldSafe(final String fieldName, final float value) {
        mainApp.enqueue(Executors.callable(new Runnable() {
            @Override
            public void run() {
                app.setNiftyField(fieldName, value);
            }
        }));
    }

    private void setFieldSafe(final String fieldName, final boolean value) {
        mainApp.enqueue(Executors.callable(new Runnable() {
            @Override
            public void run() {
                app.setNiftyField(fieldName, value);
            }
        }));
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        // this.app = app;
        this.stateManager = stateManager;
    }
}
