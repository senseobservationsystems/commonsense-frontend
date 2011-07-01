package nl.sense_os.commonsense.client.main;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.main.components.NavPanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

public class MainController extends Controller implements ValueChangeHandler<String> {

    private static final Logger LOG = Logger.getLogger(MainController.class.getName());
    private View mainView;
    private String currentToken;

    public MainController() {
        registerEventTypes(MainEvents.Error, MainEvents.Init, MainEvents.UiReady);
        registerEventTypes(LoginEvents.LoginSuccess, LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MainEvents.UiReady)) {
            forwardToView(mainView, event);
            goToFirstScreen();
        } else if (type.equals(LoginEvents.LoginSuccess)) {
            onLoggedIn();
            forwardToView(this.mainView, event);
        } else if (type.equals(LoginEvents.LoggedOut)) {
            onLoggedOut();
            forwardToView(this.mainView, event);
        } else {
            forwardToView(this.mainView, event);
        }
    }

    private void onLoggedOut() {
        // History.newItem(NavPanel.HOME);
        // History.fireCurrentHistoryState();
        Window.Location.reload();
    }

    private void onLoggedIn() {
        History.newItem(NavPanel.VISUALIZATION);
        History.fireCurrentHistoryState();
    }

    @Override
    protected void initialize() {
        this.mainView = new MainView(this);

        History.addValueChangeHandler(this);

        super.initialize();
    }

    private void goToFirstScreen() {

        String token = History.getToken();
        if (token != null && token.contains("session_id=")) {
            LOG.fine("Google auth redirect");

            String sessionId = token.substring(10);

            if (null != sessionId && sessionId.length() > 0) {
                AppEvent authenticated = new AppEvent(LoginEvents.GoogleAuthResult);
                authenticated.setData("sessionId", sessionId);
                Dispatcher.forwardEvent(authenticated);
            }
        } else {
            // supply initial History token
            String startLocation = NavPanel.HOME;
            History.newItem(startLocation);
            History.fireCurrentHistoryState();
        }
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
                LOG.warning("Not signed in: refusing new history token " + token);
                History.newItem(NavPanel.HOME);
                History.fireCurrentHistoryState();
                return;
            }
        }

        AppEvent navEvent = new AppEvent(MainEvents.Navigate);
        navEvent.setData("old", this.currentToken);
        navEvent.setData("new", token);
        this.currentToken = token;

        forwardToView(this.mainView, navEvent);
    }

    private boolean isLoginRequired(String token) {
        boolean loginRequired = token.equals(NavPanel.SETTINGS)
                || token.equals(NavPanel.VISUALIZATION);
        return loginRequired;
    }

    private boolean isValidLocation(String token) {
        boolean valid = token.equals(NavPanel.SIGN_OUT);
        valid = valid || token.equals(NavPanel.DEMO);
        valid = valid || token.equals(NavPanel.HOME);
        valid = valid || token.equals(NavPanel.HELP);
        valid = valid || token.equals(NavPanel.SETTINGS);
        valid = valid || token.equals(NavPanel.VISUALIZATION);
        return valid;
    }
}
