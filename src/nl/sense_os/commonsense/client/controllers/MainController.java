package nl.sense_os.commonsense.client.controllers;

import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.views.MainView;
import nl.sense_os.commonsense.client.views.components.NavPanel;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

public class MainController extends Controller implements ValueChangeHandler<String> {

    private MainView mainView;

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
        boolean needsLogin = (token.equals(NavPanel.SIGN_IN) || token.equals(NavPanel.SETTINGS) || token
                .equals(NavPanel.VISUALIZATION));
        String startLocation = needsLogin ? NavPanel.SIGN_IN : NavPanel.HOME;
        History.newItem(startLocation);
        History.fireCurrentHistoryState();
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        String token = event.getValue();
        if (token.equals("") || false == isValidLocation(token)) {
            History.newItem(NavPanel.HOME);
            History.fireCurrentHistoryState();
            return;
        }

        if (isLoginRequired(token)) {
            UserModel user = Registry.<UserModel> get(Constants.REG_USER);
            if (null == user) {
                History.newItem(NavPanel.SIGN_IN);
                History.fireCurrentHistoryState();
                return;
            }
        }
        forwardToView(this.mainView, MainEvents.Navigate, token);
    }

    private boolean isLoginRequired(String token) {
        boolean loginRequired = token.equals(NavPanel.SETTINGS) || token.equals(NavPanel.VISUALIZATION);
        return loginRequired;
    }

    private boolean isValidLocation(String token) {
        boolean valid = token.equals(NavPanel.SIGN_IN);
        valid = valid || token.equals(NavPanel.SIGN_OUT);
        valid = valid || token.equals(NavPanel.HOME);
        valid = valid || token.equals(NavPanel.HELP);
        valid = valid || token.equals(NavPanel.SETTINGS);
        valid = valid || token.equals(NavPanel.VISUALIZATION);
        return valid;
    }
}
