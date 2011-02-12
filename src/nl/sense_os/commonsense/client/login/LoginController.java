package nl.sense_os.commonsense.client.login;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class LoginController extends Controller {

    private static final String TAG = "LoginController";
    private LoginView loginView;
    private boolean isCancelled;
    private boolean isLoggingIn;

    public LoginController() {
        registerEventTypes(MainEvents.ShowLogin, MainEvents.HideLogin);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut, LoginEvents.LoginError,
                LoginEvents.AuthenticationFailure, LoginEvents.RequestLogin,
                LoginEvents.RequestLogout);
        registerEventTypes(LoginEvents.CancelLogin, LoginEvents.LoginCancelled);
        registerEventTypes(LoginEvents.LoginReqSuccess, LoginEvents.LoginReqError);
        registerEventTypes(LoginEvents.LogoutReqSuccess, LoginEvents.LogoutReqError);
        registerEventTypes(LoginEvents.UserReqSuccess, LoginEvents.UserReqError);
    }

    /**
     * Requests the current user's details from CommonSense. Only used to check if the login was
     * successful.
     */
    private void getCurrentUser() {
        final String url = Constants.URL_USERS + "/current";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // send request to AjaxController
        AppEvent requestEvent = new AppEvent(AjaxEvents.Request);
        requestEvent.setData("method", "GET");
        requestEvent.setData("url", url);
        requestEvent.setData("session_id", sessionId);
        requestEvent.setData("onSuccess", LoginEvents.UserReqSuccess);
        requestEvent.setData("onFailure", LoginEvents.UserReqError);
        Dispatcher.forwardEvent(requestEvent);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType eventType = event.getType();

        if (eventType.equals(LoginEvents.RequestLogin)) {
            login(event);
        } else if (eventType.equals(LoginEvents.RequestLogout)) {
            logout(event);
        } else if (eventType.equals(LoginEvents.CancelLogin)) {
            onCancel(event);
        } else if (eventType.equals(LoginEvents.LoginReqSuccess)) {
            parseLoginReponse(event.<String> getData());
        } else if (eventType.equals(LoginEvents.LoginReqError)) {
            onLoginError();
        } else if (eventType.equals(LoginEvents.LogoutReqSuccess)) {
            onLoggedOut();
        } else if (eventType.equals(LoginEvents.LogoutReqError)) {
            onLogoutError();
        } else if (eventType.equals(LoginEvents.UserReqSuccess)) {
            parseUserReponse(event.<String> getData());
        } else if (eventType.equals(LoginEvents.UserReqError)) {
            onLoginError();
        } else {
            forwardToView(this.loginView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.loginView = new LoginView(this);
    }

    private void login(AppEvent event) {

        // update controller status
        this.isLoggingIn = true;
        this.isCancelled = false;

        // prepare request properties
        String url = Constants.URL_LOGIN + ".json";
        String name = event.<String> getData("username");
        String pass = event.<String> getData("password");
        String hashPass = Md5Hasher.hash(pass);
        String body = "{\"username\":\"" + name + "\",\"password\":\"" + hashPass + "\"}";

        // send request to AjaxController
        AppEvent requestEvent = new AppEvent(AjaxEvents.Request);
        requestEvent.setData("method", "POST");
        requestEvent.setData("url", url);
        requestEvent.setData("body", body);
        requestEvent.setData("onSuccess", LoginEvents.LoginReqSuccess);
        requestEvent.setData("onFailure", LoginEvents.LoginReqError);
        Dispatcher.forwardEvent(requestEvent);
    }

    private void logout(AppEvent event) {
        String url = Constants.URL_LOGOUT;
        String sessionId = Registry.get(Constants.REG_SESSION_ID);

        // send request to AjaxController
        AppEvent requestEvent = new AppEvent(AjaxEvents.Request);
        requestEvent.setData("method", "GET");
        requestEvent.setData("url", url);
        requestEvent.setData("session_id", sessionId);
        requestEvent.setData("onSuccess", LoginEvents.LogoutReqSuccess);
        requestEvent.setData("onFailure", LoginEvents.LogoutReqError);
        Dispatcher.forwardEvent(requestEvent);
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

    public void onLogoutError() {
        // TODO handle logout error events
        Log.w(TAG, "LogoutError");
        onLoggedOut();
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
                    Log.e(TAG, "Error parsing login response: \"session_id\" is not a JSON String");
                    onLoginError();
                }
            } else {
                Log.e(TAG, "Error parsing login response: \"session_id\" is is not found");
                onLoginError();
            }
        } else {
            Log.e(TAG, "Error parsing login response: response=null");
            onLoginError();
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
                    JSONString jsonId = jsonUser.get("id").isString();
                    JSONString jsonEmail = jsonUser.get("email").isString();
                    JSONString jsonUsername = jsonUser.get("username").isString();
                    JSONString jsonName = jsonUser.get("name").isString();
                    JSONString jsonSurname = jsonUser.get("surname").isString();
                    JSONString jsonMobile = jsonUser.get("mobile").isString();
                    JSONString jsonUuid = jsonUser.get("UUID").isString();

                    int id = jsonId != null ? Integer.parseInt(jsonId.stringValue()) : -1;
                    String username = jsonUsername != null ? jsonUsername.stringValue() : "";
                    String name = jsonName != null ? jsonName.stringValue() : "";
                    String surname = jsonSurname != null ? jsonSurname.stringValue() : "";
                    String mobile = jsonMobile != null ? jsonMobile.stringValue() : "";
                    String email = jsonEmail != null ? jsonEmail.stringValue() : "";
                    String uuid = jsonUuid != null ? jsonUuid.stringValue() : "";
                    UserModel user = new UserModel(id, username, email, name, surname, mobile, uuid);

                    onCurrentUser(user);
                } else {
                    Log.e(TAG, "Error parsing current user response: "
                            + "\"user\" is not a valid JSONObject");
                    onLoginError();
                }
            } else {
                Log.e(TAG, "Error parsing current user response: \"user\" JSONValue not found");
                onLoginError();
            }
        } else {
            Log.e(TAG, "Error parsing current user response: response=null");
            onLoginError();
        }
    }
}
