package nl.sense_os.commonsense.client.auth.login;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.json.parsers.UserParser;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.shared.constants.Constants;
import nl.sense_os.commonsense.shared.constants.Urls;
import nl.sense_os.commonsense.shared.models.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class LoginController extends Controller {

    private static final Logger logger = Logger.getLogger("LoginController");
    private View loginView;

    public LoginController() {

        registerEventTypes(MainEvents.Init);

        // general login events
        registerEventTypes(LoginEvents.LoginSuccess, LoginEvents.LoggedOut,
                LoginEvents.LoginRequest, LoginEvents.RequestLogout);

        // local events
        registerEventTypes(LoginEvents.LoginFailure, LoginEvents.AuthenticationFailure);

        // ajax events
        registerEventTypes(LoginEvents.AjaxLoginSuccess, LoginEvents.AjaxLoginFailure,
                LoginEvents.AjaxLogoutSuccess, LoginEvents.AjaxLogoutFailure,
                LoginEvents.AjaxUserSuccess, LoginEvents.AjaxUserFailure);

        // layout events
        registerEventTypes(LoginEvents.Show);
    }

    /**
     * Requests the current user's details from CommonSense. Only used to check if the login was
     * successful.
     */
    private void getCurrentUser() {
        final String url = Urls.USERS + "/current";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // send request to AjaxController
        AppEvent requestEvent = new AppEvent(AjaxEvents.Request);
        requestEvent.setData("method", "GET");
        requestEvent.setData("url", url);
        requestEvent.setData("session_id", sessionId);
        requestEvent.setData("onSuccess", new AppEvent(LoginEvents.AjaxUserSuccess));
        requestEvent.setData("onFailure", new AppEvent(LoginEvents.AjaxUserFailure));
        Dispatcher.forwardEvent(requestEvent);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType eventType = event.getType();

        if (eventType.equals(LoginEvents.LoginRequest)) {
            // logger.fine( "LoginRequest");
            final String username = event.<String> getData("username");
            final String password = event.<String> getData("password");
            login(username, password);

        } else if (eventType.equals(LoginEvents.RequestLogout)) {
            // logger.fine( "RequestLogout");
            logout(event);

        } else if (eventType.equals(LoginEvents.AjaxLoginSuccess)) {
            // logger.fine( "AjaxLoginSuccess");
            final String response = event.<String> getData("response");
            parseLoginReponse(response);

        } else if (eventType.equals(LoginEvents.AjaxLoginFailure)) {
            logger.warning("AjaxLoginFailure");
            final int code = event.getData("code");
            if (code == 403) {
                onAuthenticationFailure();
            } else {
                onLoginFailure(code);
            }

        } else if (eventType.equals(LoginEvents.AjaxLogoutSuccess)) {
            // logger.fine( "AjaxLogoutSuccess");
            final String response = event.<String> getData("response");
            onLoggedOut(response);

        } else if (eventType.equals(LoginEvents.AjaxLogoutFailure)) {
            logger.warning("AjaxLogoutFailure");
            final int code = event.getData("code");
            onLogoutFailure(code);

        } else if (eventType.equals(LoginEvents.AjaxUserSuccess)) {
            // logger.fine( "AjaxUserSuccess");
            final String response = event.<String> getData("response");
            parseUserReponse(response);

        } else if (eventType.equals(LoginEvents.AjaxUserFailure)) {
            logger.warning("AjaxUserFailure");
            final int code = event.getData("code");
            onLoginFailure(code);

        } else {
            forwardToView(this.loginView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.loginView = new LoginPanel(this);
    }

    private void login(String username, String password) {

        // prepare request properties
        String url = Urls.LOGIN + ".json";
        String hashPass = Md5Hasher.hash(password);
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + hashPass + "\"}";

        // send request to AjaxController
        AppEvent requestEvent = new AppEvent(AjaxEvents.Request);
        requestEvent.setData("method", "POST");
        requestEvent.setData("url", url);
        requestEvent.setData("body", body);
        requestEvent.setData("onSuccess", new AppEvent(LoginEvents.AjaxLoginSuccess));
        requestEvent.setData("onFailure", new AppEvent(LoginEvents.AjaxLoginFailure));
        Dispatcher.forwardEvent(requestEvent);
    }

    private void logout(AppEvent event) {

        // prepare request properties
        String url = Urls.LOGOUT;
        String sessionId = Registry.get(Constants.REG_SESSION_ID);

        // send request to AjaxController
        AppEvent requestEvent = new AppEvent(AjaxEvents.Request);
        requestEvent.setData("method", "GET");
        requestEvent.setData("url", url);
        requestEvent.setData("session_id", sessionId);
        requestEvent.setData("onSuccess", new AppEvent(LoginEvents.AjaxLogoutSuccess));
        requestEvent.setData("onFailure", new AppEvent(LoginEvents.AjaxLogoutFailure));
        Dispatcher.forwardEvent(requestEvent);
    }

    private void onAuthenticationFailure() {
        forwardToView(this.loginView, new AppEvent(LoginEvents.AuthenticationFailure));
    }

    private void onCurrentUser(UserModel user) {
        Registry.register(Constants.REG_USER, user);
        Dispatcher.forwardEvent(LoginEvents.LoginSuccess, user);
    }

    private void onLoggedIn(String sessionId) {
        Registry.register(Constants.REG_SESSION_ID, sessionId);
        getCurrentUser();
    }

    @SuppressWarnings("deprecation")
    private void onLoggedOut(String response) {
        Registry.unregister(Constants.REG_SESSION_ID);
        Registry.unregister(Constants.REG_USER);

        Registry.unregister(Constants.REG_SERVICES);
        Registry.unregister(Constants.REG_MY_SENSORS_TREE);
        Registry.unregister(Constants.REG_GROUP_SENSORS);

        Dispatcher.forwardEvent(LoginEvents.LoggedOut);
    }

    private void onLoginFailure(int code) {
        AppEvent errorEvent = new AppEvent(LoginEvents.LoginFailure);
        errorEvent.setData("code", code);
        forwardToView(this.loginView, errorEvent);
    }

    private void onLogoutFailure(int code) {
        // TODO handle logout error events
        onLoggedOut("Status code: " + code);
    }

    private void parseLoginReponse(String response) {
        if (response != null) {

            // try to get "session_id" object
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonVal = json.get("session_id");
            if (null != jsonVal) {
                JSONString jsonString = jsonVal.isString();
                if (null != jsonString) {
                    String sessionId = jsonString.stringValue();

                    onLoggedIn(sessionId);
                } else {
                    logger.severe("Error parsing login response: \"session_id\" is not a JSON String");
                    onLoginFailure(0);
                }
            } else {
                logger.severe("Error parsing login response: \"session_id\" is is not found");
                onLoginFailure(0);
            }
        } else {
            logger.severe("Error parsing login response: response=null");
            onLoginFailure(0);
        }
    }

    private void parseUserReponse(String response) {
        if (response != null) {

            // try to get "user" object
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonValue = json.get("user");
            if (null != jsonValue) {
                JSONObject jsonUser = jsonValue.isObject();

                if (null != jsonUser) {
                    UserModel user = UserParser.parseUser(jsonUser);
                    onCurrentUser(user);
                } else {
                    logger.severe("Error parsing current user response: "
                            + "\"user\" is not a valid JSONObject");
                    onLoginFailure(0);
                }
            } else {
                logger.severe("Error parsing current user response: \"user\" JSONValue not found");
                onLoginFailure(0);
            }
        } else {
            logger.severe("Error parsing current user response: response=null");
            onLoginFailure(0);
        }
    }
}
