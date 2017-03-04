package julietgroupproject.GUI;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;
import de.lessvoid.nifty.controls.Slider;
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
import julietgroupproject.UIAppState;

public class EditorController implements ScreenController {

    private UIAppState app;
    private SimpleApplication mainApp;
    private AppStateManager stateManager;
    private Nifty nifty;
    private Screen screen;
    private TabGroup tabs;
    private Tab addLimb;
    private Tab addBody;
    private DropDown limbShapeSelect;
    private DropDown bodyShapeSelect;

    public EditorController(UIAppState App) {
        this.app = App;
        this.mainApp = app.getApp();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
        
        addValues();
        app.setArrow(true);
    }

    public void newBody() {
        app.createNewBody();
        tabs.setSelectedTab(addLimb);
    }

    public void hideEditor() {
        nifty.gotoScreen("hidden");
    }

    public void addValues() {

        bodyShapeSelect = screen.findNiftyControl("body_shape_selector", DropDown.class);
        tabs = screen.findNiftyControl("limb_body_tabs", TabGroup.class);
        addLimb = screen.findNiftyControl("add_limb_tab", Tab.class);
        addBody = screen.findNiftyControl("add_body_tab", Tab.class);

        bodyShapeSelect.clear();
        bodyShapeSelect.addItem("Cuboid");
        bodyShapeSelect.addItem("Ellipsoid");
        bodyShapeSelect.addItem("Cylinder");
        bodyShapeSelect.addItem("Torus");
        bodyShapeSelect.selectItemByIndex(0);

        limbShapeSelect = screen.findNiftyControl("limb_shape_selector", DropDown.class);
        addLimb = screen.findNiftyControl("add_limb_tab", Tab.class);
        addBody = screen.findNiftyControl("add_body_tab", Tab.class);

        limbShapeSelect.clear();
        limbShapeSelect.addItem("Cuboid");
        limbShapeSelect.addItem("Ellipsoid");
        limbShapeSelect.addItem("Cylinder");
        limbShapeSelect.addItem("Torus");
        limbShapeSelect.selectItemByIndex(0);
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

    public void gotoLoadScreen() {
        nifty.gotoScreen("load_dialog");
    }

    public void gotoNameScreen() {
        nifty.gotoScreen("name_dialog");
    }

    public void gotoSaveScreen() {
        nifty.gotoScreen("save_dialog");
    }

    public void editorOptions() {
        nifty.gotoScreen("editor_options");
    }

    public void pulsateToggle() {
        if (screen.findElementByName("panel_background") != null){
            nifty.getScreen("editor").findElementByName("panel_background").startEffect(EffectEventId.onCustom);
        }
    }

    @NiftyEventSubscriber(id = "limb_shape_selector")
    public void onDropDownLimbSelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event) {
        Element getWidth = screen.findElementByName("limbWidthSlider");
        Element getHeight = screen.findElementByName("limbHeightSlider");
        Element getLength = screen.findElementByName("limbLengthSlider");
        Element firstLabelE = screen.findElementByName("first_label");
        Element secondLabelE = screen.findElementByName("second_label");
        Element thirdLabelE = screen.findElementByName("third_label");

        Label firstLabel = screen.findNiftyControl("first_label", Label.class);
        Label secondLabel = screen.findNiftyControl("second_label", Label.class);
        Label thirdLabel = screen.findNiftyControl("third_label", Label.class);
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
        app.setFieldSafe("currentLimbShape", current);
    }

    public void restartAlien() {
        app.restartAlien();
    }

    @NiftyEventSubscriber(id = "body_shape_selector")
    public void onDropDownBodySelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event) {
        Element getWidth = screen.findElementByName("bodyWidthSlider");
        Element getHeight = screen.findElementByName("bodyHeightSlider");
        Element getLength = screen.findElementByName("bodyLengthSlider");
        Element firstLabelE = screen.findElementByName("first_body_label");
        Element secondLabelE = screen.findElementByName("second_body_label");
        Element thirdLabelE = screen.findElementByName("third_body_label");

        Label firstLabel = screen.findNiftyControl("first_body_label", Label.class);
        Label secondLabel = screen.findNiftyControl("second_body_label", Label.class);
        Label thirdLabel = screen.findNiftyControl("third_body_label", Label.class);
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
        app.setFieldSafe("currentBodyShape", current);

    }

    @NiftyEventSubscriber(id = "bodyWidthSlider")
    public void onBodyWidthSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("bodyWidth", event.getValue());
    }

    @NiftyEventSubscriber(id = "bodyHeightSlider")
    public void onBodyHeightSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("bodyHeight", event.getValue());
    }

    @NiftyEventSubscriber(id = "bodyLengthSlider")
    public void onBodyLengthSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("bodyLength", event.getValue());
    }

    @NiftyEventSubscriber(id = "bodyWeightSlider")
    public void onBodyWeightSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("bodyWeight", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbWidthSlider")
    public void onLimbWidthSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbWidth", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbHeightSlider")
    public void onLimbHeightSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbHeight", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbLengthSlider")
    public void onLimbLengthSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbLength", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbWeightSlider")
    public void onLimbWeightSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbWeight", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbFrictionSlider")
    public void onLimbFrictionSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbFriction", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbStrengthSlider")
    public void onLimbStrengthSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbStrength", event.getValue());
    }

    @NiftyEventSubscriber(id = "limbSeparationSlider")
    public void onLimbSeparationSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbSeparation", event.getValue());
    }

    @NiftyEventSubscriber(id = "symmetricCheckBox")
    public void onSymmetricCheckBoxChanged(final String id, final CheckBoxStateChangedEvent event) {
        app.setFieldSafe("symmetric", event.isChecked());
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
        app.setFieldSafe("currentHingeAxis", selected);
    }

    @NiftyEventSubscriber(id = "jointRestrictionCheckBox")
    public void onJointRestrictionCheckBoxChanged(final String id, final CheckBoxStateChangedEvent event) {
        app.setFieldSafe("jointRestrictionCheckBox", event.isChecked());
        if (event.isChecked()) {
            screen.findNiftyControl("minHingeSlider", Slider.class).enable();
            screen.findNiftyControl("maxHingeSlider", Slider.class).enable();
        } else {
            screen.findNiftyControl("minHingeSlider", Slider.class).disable();
            screen.findNiftyControl("maxHingeSlider", Slider.class).disable();
        }
    }

    @NiftyEventSubscriber(id = "minHingeSlider")
    public void onLimbMinHingeSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("minHingeJoint", event.getValue());
    }

    @NiftyEventSubscriber(id = "maxHingeSlider")
    public void onLimbMaxHingeSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("maxHingeJoint", event.getValue());
    }

    @NiftyEventSubscriber(id = "yawSlider")
    public void onLimbYawSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbYaw", event.getValue());
    }

    @NiftyEventSubscriber(id = "pitchSlider")
    public void onLimbPitchSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbPitch", event.getValue());
    }

    @NiftyEventSubscriber(id = "rollSlider")
    public void onLimbRollSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("limbRoll", event.getValue());
    }

    @NiftyEventSubscriber(id = "jointPosSlider")
    public void onJointPosSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("jointPositionFraction", event.getValue());
    }

    @NiftyEventSubscriber(id = "jointRotSlider")
    public void onJointRotSliderChanged(final String id, final SliderChangedEvent event) {
        app.setFieldSafe("jointStartRotation", event.getValue());
    }

    @Override
    public void onStartScreen() {
        app.addKeyBindings();
        if (app.savedAlien.body == null)
        {
            tabs.setSelectedTab(addBody);
        }
        else
        {
            tabs.setSelectedTab(addLimb);
        }
    }

    @Override
    public void onEndScreen() {
        app.removeKeyBindings();
    }

    public void newAlien() {
        app.resetAlien();

        nifty.gotoScreen("name_dialog");

        tabs.setSelectedTab(addBody);
    }
}
