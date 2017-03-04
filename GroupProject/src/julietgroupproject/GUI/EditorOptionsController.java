package julietgroupproject.GUI;

import com.jme3.app.SimpleApplication;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import julietgroupproject.UIAppState;
import julietgroupproject.AlienHelper;

public class EditorOptionsController implements ScreenController {

    private UIAppState app;
    private Screen screen;
    private SimpleApplication mainApp;
    private Nifty nifty;
    DropDown textureBox;
    CheckBox wireMeshCheckBox;
    CheckBox arrowCheckBox;
    CheckBox smoothCameraCheckBox;

    public EditorOptionsController(UIAppState App) {
        this.app = App;
        this.mainApp = app.getApp();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

        addOptionsValues();
    }

    public void showEditor() {
        nifty.gotoScreen("editor");
    }

    @Override
    public void onStartScreen() {
        arrowCheckBox.setChecked(app.getArrowOn());
        smoothCameraCheckBox.setChecked(app.getSmoothCameraOn());
        wireMeshCheckBox.setChecked(app.getWireMeshOn());
    }

    @NiftyEventSubscriber(id = "textureDropDown")
    public void onDropDownTextureSelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event) {
        String selectedTex = event.getSelection();
        int textno = 3;
        switch (selectedTex) {
            case "Plant":
                textno = 0;
                break;
            case "Snake":
                textno = 1;
                break;
            case "Mosaic":
                textno = 2;
                break;
            case "Zebra":
                textno = 3;
                break;
        }
        app.setTexture(textno);
    }

    @NiftyEventSubscriber(id = "chaseCamCheckBox")
    public void onChaseCamChange(final String id, final CheckBoxStateChangedEvent event) {
        app.setSmoothCamera(event.isChecked());
    }

    @NiftyEventSubscriber(id = "wireMeshCheckBox")
    public void onWireMeshChange(final String id, final CheckBoxStateChangedEvent event) {
        app.setWireMesh(event.isChecked());
    }

    @NiftyEventSubscriber(id = "directionArrowCheckBox")
    public void onDirectionArrowChange(final String id, final CheckBoxStateChangedEvent event) {
        app.setArrow(event.isChecked());
    }

    @NiftyEventSubscriber(id = "gravitySlider")
    public void onGravityChange(final String id, final SliderChangedEvent event) {
        app.setGravity(event.getValue());
    }

    public void addOptionsValues() {
        textureBox = screen.findNiftyControl("textureDropDown", DropDown.class);
        wireMeshCheckBox = screen.findNiftyControl("wireMeshCheckBox", CheckBox.class);
        smoothCameraCheckBox = screen.findNiftyControl("chaseCamCheckBox", CheckBox.class);
        arrowCheckBox = screen.findNiftyControl("directionArrowCheckBox", CheckBox.class);
       
        textureBox.clear();

        textureBox.addItem("Plant");
        textureBox.addItem("Snake");
        textureBox.addItem("Mosaic");
        textureBox.addItem("Zebra");

        String tex = "Zebra";
        int textNo = app.getTextureNo();
        switch (textNo) {
            case 0:
                tex = "Plant";
                break;
            case 1:
                tex = "Snake";
                break;
            case 2:
                tex = "Mosaic";
                break;
            case 3:
                tex = "Zebra";
                break;
        }
        textureBox.selectItem(tex);
    }

    @Override
    public void onEndScreen() {
    }
}
