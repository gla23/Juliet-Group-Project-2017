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
    private boolean X = true;
    private boolean Y = false;
    private boolean Z = false;
    private boolean auto = false;
    private volatile boolean initialising = false;
    private String saveType = "";
    private String loadType = "";

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

                    textureBox.addItem("Alien1");
                    textureBox.addItem("Alien2");
                    textureBox.addItem("Alien3");

                    String tex = "Alien1";
                    int textNo = app.getTextureNo();
                    if (textNo == 0) {
                        tex = "Alien1";
                    } else if (textNo == 1) {
                        tex = "Alien2";
                    } else if (textNo == 2) {
                        tex = "Alien3";
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

    public void runPreviousSimulation() {
        //TODO
    }

    public void runNewSimulation() {
        if (app.beginTraining())
            nifty.gotoScreen("simulation");
    }

    public void gotoLoadScreen() {
        loadType = "alien";
        app.removeKeyBindings();
        nifty.gotoScreen("load_dialog");
    }

    public void confirmLoad() {
        app.addKeyBindings();
        if ("alien".equals(loadType)) {
            //TODO
            if (app.loadAlien(nifty.getScreen("load_dialog").findNiftyControl("loadTextField", TextField.class).getRealText())) {
                nifty.gotoScreen("start");
                screen = nifty.getScreen("start");
                firstBody = true;
                addAlienSpecificOptions();
                addValues();
                //TODO: inform user load was successful
            } else {
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
            if (app.saveAlien(screen.findNiftyControl("saveTextField", TextField.class).getRealText())) {
                //TODO: inform user save was successful
            } else {
                //TODO: inform user save was unsuccessful
            }
        }

        nifty.gotoScreen("start");
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
            int textno = 1;
            if (selectedTex.equals("Alien1")) {
                textno = 0;
            } else if (selectedTex.equals("Alien2")) {
                textno = 1;
            } else if (selectedTex.equals("Alien3")) {
                textno = 2;
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
