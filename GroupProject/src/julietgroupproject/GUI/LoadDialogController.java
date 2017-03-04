package julietgroupproject.GUI;

import com.jme3.app.SimpleApplication;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.Tab;
import de.lessvoid.nifty.controls.TabGroup;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import java.util.List;
import julietgroupproject.UIAppState;
import julietgroupproject.AlienHelper;

public class LoadDialogController implements ScreenController {

    private UIAppState app;
    private Screen screen;
    private SimpleApplication mainApp;
    private Nifty nifty;
    private List<String> aliens;
    private DropDown loadScrollE;

    public LoadDialogController(UIAppState App) {
        this.app = App;
        this.mainApp = app.getApp();
    }

    public void addLoadValues(final String[] aliens) {
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void confirmLoad() {
        if (app.loadAlien(AlienHelper.sanitiseAlienName((String)loadScrollE.getSelection()))) {
            nifty.gotoScreen("editor");
        } else {
            nifty.gotoScreen("load_fail");
        }
    }
    
    public void cancelLoad(){
        if (app.savedAlien.getName() == null)
        {
            nifty.gotoScreen("name_dialog");
        }
        else
        {
            nifty.gotoScreen("editor");
        }
    }

    @Override
    public void onStartScreen() {
        aliens = AlienHelper.getLoadableAliens();

        loadScrollE = screen.findNiftyControl("alien_selector", DropDown.class);

        loadScrollE.clear();
        loadScrollE.addAllItems(aliens);
        loadScrollE.selectItemByIndex(0);
    }

    @Override
    public void onEndScreen() {
    }
}
