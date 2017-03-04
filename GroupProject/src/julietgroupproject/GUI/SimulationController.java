package julietgroupproject.GUI;

import com.jme3.app.SimpleApplication;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import julietgroupproject.UIAppState;
import julietgroupproject.AlienHelper;

public class SimulationController implements ScreenController {

    private UIAppState app;
    private Screen screen;
    private SimpleApplication mainApp;
    private Nifty nifty;

    public SimulationController(UIAppState App) {
        this.app = App;
        this.mainApp = app.getApp();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }
    
    @NiftyEventSubscriber(id = "logger_listbox")
    public void logItemSelected(final String id, final ListBoxSelectionChangedEvent<julietgroupproject.GenerationResult> event)
    {
        if (event.getSelection().size() > 0) {
            app.showOffGeneration(event.getSelection().get(0).generation - 1);
        }
    }

    public void stopSimulation() {
        this.app.endSimulation();
        nifty.gotoScreen("editor");
    }
    
    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
}
