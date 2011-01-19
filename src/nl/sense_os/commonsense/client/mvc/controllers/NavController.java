package nl.sense_os.commonsense.client.mvc.controllers;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.NavEvents;
import nl.sense_os.commonsense.client.mvc.views.NavView;

public class NavController extends Controller {

    private NavView navView;
    
    public NavController() {
        registerEventTypes(MainEvents.Init, MainEvents.UiReady);
        registerEventTypes(NavEvents.NavChanged);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut);
    }
    
    @Override
    public void handleEvent(AppEvent event) {
        forwardToView(this.navView, event);
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        this.navView = new NavView(this);
    }

}
