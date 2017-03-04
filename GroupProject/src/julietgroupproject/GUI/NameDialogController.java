package julietgroupproject.GUI;

import com.jme3.app.SimpleApplication;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import julietgroupproject.UIAppState;
import julietgroupproject.AlienHelper;

public class NameDialogController implements ScreenController {

    private UIAppState app;
    private Screen screen;
    private SimpleApplication mainApp;
    private Nifty nifty;

    public NameDialogController(UIAppState App) {
        this.app = App;
        this.mainApp = app.getApp();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
        
    }

    public void confirmName() {
        String name = screen.findNiftyControl("nameTextField", TextField.class).getRealText();
        String sanitisedName = AlienHelper.sanitiseAlienName(name);
        if (sanitisedName.length() > 0) {
            app.savedAlien.setName(sanitisedName);
            nifty.gotoScreen("editor");
        }
    }
    
    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
}
