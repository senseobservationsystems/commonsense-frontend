package nl.sense_os.commonsense.client.mvc.controllers;

import nl.sense_os.commonsense.client.components.NavPanel;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.views.MainView;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

public class MainController extends Controller implements ValueChangeHandler<String> {

    private View mainView;

    public MainController() {
        registerEventTypes(MainEvents.Error, MainEvents.Init, MainEvents.UiReady);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MainEvents.UiReady)) {
            forwardToView(mainView, event);
            goToFirstScreen();
        } else {
            forwardToView(this.mainView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.mainView = new MainView(this);

        History.addValueChangeHandler(this);
    }
    
    private void goToFirstScreen() {

        // supply initial History token
        String token = History.getToken();
        boolean needsLogin = (token.equals(NavPanel.SIGN_IN) || token.equals(NavPanel.SETTINGS)
                || token.equals(NavPanel.SHARE_DATA) || token.equals(NavPanel.TRAINING_DATA) || token
                .equals(NavPanel.VISUALIZATION));
        String startLocation = needsLogin ? NavPanel.SIGN_IN : NavPanel.HOME;
        History.newItem(startLocation);
        History.fireCurrentHistoryState();
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        String token = event.getValue();
        if (token.equals("")) {
            token = NavPanel.HOME;
        }
        forwardToView(this.mainView, MainEvents.Navigate, token);
    }
}
