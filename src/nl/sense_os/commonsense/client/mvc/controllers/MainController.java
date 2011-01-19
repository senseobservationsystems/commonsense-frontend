package nl.sense_os.commonsense.client.mvc.controllers;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.NavEvents;
import nl.sense_os.commonsense.client.mvc.views.MainView;

public class MainController extends Controller {

    private View mainView;

    public MainController() {
        registerEventTypes(MainEvents.Error, MainEvents.Init, MainEvents.UiReady);
        registerEventTypes(NavEvents.NavReady, NavEvents.NavChanged);
        registerEventTypes(LoginEvents.LoginPanelReady, LoginEvents.LoggedIn, LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        forwardToView(this.mainView, event);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.mainView = new MainView(this);
    }
}
