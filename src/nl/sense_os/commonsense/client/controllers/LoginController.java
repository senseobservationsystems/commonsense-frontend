package nl.sense_os.commonsense.client.controllers;

import nl.sense_os.commonsense.client.controllers.cors.LoginJsniRequests;
import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.client.views.LoginView;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

public class LoginController extends Controller {

    private static final String TAG = "LoginController";
    private LoginView loginView;
    private boolean isCancelled;
    private boolean isLoggingIn;

    public LoginController() {
        registerEventTypes(MainEvents.ShowLogin);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut, LoginEvents.LoginError,
                LoginEvents.AuthenticationFailure, LoginEvents.RequestLogin,
                LoginEvents.RequestLogout, LoginEvents.CancelLogin, LoginEvents.LoginCancelled);
    }

    /**
     * Requests the current user's details from CommonSense. Only used to check if the login was
     * successful.
     */
    private void getCurrentUser() {
        final String url = Constants.URL_USERS + "/current";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        LoginJsniRequests.getCurrentUser(url, sessionId, this);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType eventType = event.getType();

        if (eventType.equals(LoginEvents.RequestLogin)) {
            onLoginRequest(event);
        } else if (eventType.equals(LoginEvents.RequestLogout)) {
            onLogoutRequest(event);
        } else if (eventType.equals(LoginEvents.CancelLogin)) {
            onCancel(event);
        } else {
            forwardToView(this.loginView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.loginView = new LoginView(this);
    }

    public void onAuthenticationFailure() {
        if (false == this.isCancelled) {
            this.isLoggingIn = false;
            Dispatcher.forwardEvent(LoginEvents.AuthenticationFailure);
        }
    }

    private void onCancel(AppEvent event) {
        if (true == this.isLoggingIn) {
            this.isCancelled = true;
            Dispatcher.forwardEvent(LoginEvents.LoginCancelled);
        }
    }

    public void onCurrentUser(UserModel user) {
        if (false == isCancelled) {
            Registry.register(Constants.REG_USER, user);
            this.isLoggingIn = false;
            // forwardToView(loginView, LoginEvents.LoggedIn, user);
            Dispatcher.forwardEvent(LoginEvents.LoggedIn, user);
        }
    }

    public void onLoggedIn(String sessionId) {
        if (false == isCancelled) {
            Registry.register(Constants.REG_SESSION_ID, sessionId);
            getCurrentUser();
        }
    }

    public void onLoggedOut() {
        Registry.unregister(Constants.REG_SESSION_ID);
        Registry.unregister(Constants.REG_USER);
        Registry.unregister(Constants.REG_MY_SENSORS);
        Registry.unregister(Constants.REG_GROUP_SENSORS);
        Registry.unregister(Constants.REG_GROUPS);

        Dispatcher.forwardEvent(LoginEvents.LoggedOut);
    }

    public void onLoginError() {
        if (false == isCancelled) {
            this.isLoggingIn = false;
            Dispatcher.forwardEvent(LoginEvents.LoginError);
        }
    }

    private void onLoginRequest(AppEvent event) {
        String url = Constants.URL_LOGIN;
        String name = event.<String> getData("username");
        String pass = event.<String> getData("password");
        String hashPass = Md5Hasher.hash(pass);
        String data = "{\"username\":\"" + name + "\",\"password\":\"" + hashPass + "\"}";

        this.isLoggingIn = true;
        this.isCancelled = false;

        LoginJsniRequests.login(url, data, this);
    }

    public void onLogoutError() {
        // TODO handle logout error events
        Log.w(TAG, "LogoutError");
        onLoggedOut();
    }

    private void onLogoutRequest(AppEvent event) {
        String url = Constants.URL_LOGOUT;
        LoginJsniRequests.logout(url, this);
    }
}
