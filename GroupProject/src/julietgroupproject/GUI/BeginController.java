package julietgroupproject.GUI;

import com.jme3.app.SimpleApplication;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import julietgroupproject.UIAppState;
import julietgroupproject.AlienHelper;

public class BeginController implements ScreenController {

    private UIAppState app;
    private Screen screen;
    private SimpleApplication mainApp;
    private Nifty nifty;

    public BeginController(UIAppState App) {
        this.app = App;
        this.mainApp = app.getApp();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }
    
    public void gotoNameScreen()
    {
        nifty.gotoScreen("name_dialog");
    }
    
    public void gotoLoadScreen()
    {
        nifty.gotoScreen("load_dialog");
    }
    
    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
}
