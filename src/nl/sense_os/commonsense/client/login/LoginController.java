package nl.sense_os.commonsense.client.login;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;

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

    private static final String TAG = "LoginController";
    private View loginView;
    private boolean isCancelled;
    private boolean isLoggingIn;

    public LoginController() {

        registerEventTypes(MainEvents.Init);

        // general login events
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut, LoginEvents.RequestLogin,
                LoginEvents.RequestLogout);

        // local events
        registerEventTypes(LoginEvents.LoginError, LoginEvents.AuthenticationFailure,
                LoginEvents.CancelLogin, LoginEvents.LoginCancelled);

        // ajax events
        registerEventTypes(LoginEvents.AjaxLoginSuccess, LoginEvents.AjaxLoginFailure,
                LoginEvents.AjaxLogoutSuccess, LoginEvents.AjaxLogoutFailure,
                LoginEvents.AjaxUserSuccess, LoginEvents.AjaxUserFailure);

        // layout events
        registerEventTypes(LoginEvents.Show, LoginEvents.Hide);
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
        requestEvent.setData("onSuccess", new AppEvent(LoginEvents.AjaxUserSuccess));
        requestEvent.setData("onFailure", new AppEvent(LoginEvents.AjaxUserFailure));
        Dispatcher.forwardEvent(requestEvent);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType eventType = event.getType();

        if (eventType.equals(LoginEvents.RequestLogin)) {
            // Log.d(TAG, "RequestLogin");
            login(event);

        } else if (eventType.equals(LoginEvents.RequestLogout)) {
            // Log.d(TAG, "RequestLogout");
            logout(event);

        } else if (eventType.equals(LoginEvents.CancelLogin)) {
            // Log.d(TAG, "CancelLogin");
            onCancel(event);

        } else if (eventType.equals(LoginEvents.AjaxLoginSuccess)) {
            // Log.d(TAG, "AjaxLoginSuccess");
            final String response = event.<String> getData("response");
            parseLoginReponse(response);

        } else if (eventType.equals(LoginEvents.AjaxLoginFailure)) {
            Log.w(TAG, "AjaxLoginFailure");
            final int code = event.getData("code");
            onLoginError(code);

        } else if (eventType.equals(LoginEvents.AjaxLogoutSuccess)) {
            // Log.d(TAG, "AjaxLogoutSuccess");
            final String response = event.<String> getData("response");
            onLoggedOut(response);

        } else if (eventType.equals(LoginEvents.AjaxLogoutFailure)) {
            Log.w(TAG, "AjaxLogoutFailure");
            final int code = event.getData("code");
            onLogoutError(code);

        } else if (eventType.equals(LoginEvents.AjaxUserSuccess)) {
            // Log.d(TAG, "AjaxUserSuccess");
            final String response = event.<String> getData("response");
            parseUserReponse(response);

        } else if (eventType.equals(LoginEvents.AjaxUserFailure)) {
            Log.w(TAG, "AjaxUserFailure");
            final int code = event.getData("code");
            onLoginError(code);

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
        requestEvent.setData("onSuccess", new AppEvent(LoginEvents.AjaxLoginSuccess));
        requestEvent.setData("onFailure", new AppEvent(LoginEvents.AjaxLoginFailure));
        Dispatcher.forwardEvent(requestEvent);
    }

    private void logout(AppEvent event) {

        // prepare request properties
        String url = Constants.URL_LOGOUT;
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

    public void onAuthenticationFailure() {
        this.isLoggingIn = false;
        if (false == this.isCancelled) {
            forwardToView(this.loginView, new AppEvent(LoginEvents.AuthenticationFailure));
        } else {
            this.isCancelled = false;
        }
    }

    private void onCancel(AppEvent event) {
        if (true == this.isLoggingIn) {
            this.isCancelled = true;
            forwardToView(this.loginView, new AppEvent(LoginEvents.LoginCancelled));
        }
    }

    public void onCurrentUser(UserModel user) {
        this.isLoggingIn = false;
        if (false == this.isCancelled) {
            Registry.register(Constants.REG_USER, user);
            Dispatcher.forwardEvent(LoginEvents.LoggedIn, user);
        } else {
            this.isCancelled = false;
        }
    }

    public void onLoggedIn(String sessionId) {
        if (false == this.isCancelled) {
            Registry.register(Constants.REG_SESSION_ID, sessionId);
            getCurrentUser();
        } else {
            this.isLoggingIn = false;
            this.isCancelled = false;
        }
    }

    public void onLoggedOut(String response) {
        Registry.unregister(Constants.REG_SESSION_ID);
        Registry.unregister(Constants.REG_USER);
        Registry.unregister(Constants.REG_MY_SENSORS);
        Registry.unregister(Constants.REG_GROUP_SENSORS);
        Registry.unregister(Constants.REG_GROUPS);

        Dispatcher.forwardEvent(LoginEvents.LoggedOut);
    }

    public void onLoginError(int code) {
        this.isLoggingIn = false;
        if (false == this.isCancelled) {
            AppEvent errorEvent = new AppEvent(LoginEvents.LoginError);
            errorEvent.setData("code", code);
            forwardToView(this.loginView, errorEvent);
        } else {
            this.isCancelled = false;
        }
    }

    public void onLogoutError(int code) {
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
                    Log.e(TAG, "Error parsing login response: \"session_id\" is not a JSON String");
                    onLoginError(0);
                }
            } else {
                Log.e(TAG, "Error parsing login response: \"session_id\" is is not found");
                onLoginError(0);
            }
        } else {
            Log.e(TAG, "Error parsing login response: response=null");
            onLoginError(0);
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
                    onLoginError(0);
                }
            } else {
                Log.e(TAG, "Error parsing current user response: \"user\" JSONValue not found");
                onLoginError(0);
            }
        } else {
            Log.e(TAG, "Error parsing current user response: response=null");
            onLoginError(0);
        }
    }
}
