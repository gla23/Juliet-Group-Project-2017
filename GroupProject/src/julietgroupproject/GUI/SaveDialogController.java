package julietgroupproject.GUI;

import com.jme3.app.SimpleApplication;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import julietgroupproject.UIAppState;
import julietgroupproject.AlienHelper;

public class SaveDialogController implements ScreenController {

    private UIAppState app;
    private Screen screen;
    private SimpleApplication mainApp;
    private Nifty nifty;

    public SaveDialogController(UIAppState App) {
        this.app = App;
        this.mainApp = app.getApp();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void confirmSave() {
        String name = screen.findNiftyControl("saveTextField", TextField.class).getRealText();
        String sanitisedName = AlienHelper.sanitiseAlienName(name);
        if (sanitisedName.length() > 0 && app.saveAlien(sanitisedName)) { //TODO fix concurrency
            nifty.gotoScreen("save_success");
        } else {
            nifty.gotoScreen("save_fail");
        }
    }
    
    public void cancel()
    {
        nifty.gotoScreen("editor");
    }
    
    @Override
    public void onStartScreen() {
        screen.findNiftyControl("saveTextField", TextField.class).setText(app.savedAlien.getName());
    }

    @Override
    public void onEndScreen() {
    }
}
