package nl.sense_os.commonsense.client.mvc.controllers;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.views.LoginView;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;

public class LoginController extends Controller {

    private static final String TAG = "LoginController";

    /**
     * Requests current user info from CommonSense. Calls back to {@link #handleCurrentUser(String)}
     * or {@link #onLoginError()}.
     * 
     * @param url
     *            URL of /users/current GET method
     * @param sessionId
     *            session ID for authentication
     * @param handler
     *            LoginController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    private native static void jsniCurrentUser(String url, String sessionId, LoginController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else { outputError(); }
            }
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.LoginController::onLoginError()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.LoginController::handleCurrentUser(Ljava/lang/String;)(xhr.responseText);
        }

        if (xhr) {
            if (isIE8) {
                url = url + ".json" + "?session_id=" + sessionId;
                xhr.open("GET", url);
                xhr.onload = outputResult;
                xhr.onerror = outputError;
                xhr.ontimeout = outputError;
                xhr.send();
            } else {
                xhr.open('GET', url, true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID", sessionId);
                xhr.setRequestHeader("Accept", "application/json");
                xhr.send();
            }
        } else {
            outputError();
        }
    }-*/;

    /**
     * Requests login at CommonSense. Calls back to {@link #handleLoginResponse(String)},
     * {@link #onAuthenticationFailure()} or {@link #onLoginError()}.
     * 
     * @param url
     *            URL of /login POST method
     * @param data
     *            String with JSON encoded login data
     * @param handler
     *            LoginController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    private native static void jsniLogin(String url, String data, LoginController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else if (xhr.status == 403) { outputIncorrect(); }
                else { outputError(); }
            }
        }

        function outputIncorrect() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.LoginController::onAuthenticationFailure()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.LoginController::onLoginError()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.LoginController::handleLoginResponse(Ljava/lang/String;)(xhr.responseText);
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("POST", url + ".json", true);
                xhr.onload = outputResult;
                xhr.onerror = outputIncorrect;
                xhr.ontimeout = outputError;
                xhr.send(data);
            } else {
                xhr.open('POST', url + ".json", true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("Content-Type","application/json");
                xhr.send(data);
            }
        } else {
            outputError();
        }
    }-*/;

    /**
     * Requests logout from CommonSense. Calls back to {@link #onLoggedOut()} or
     * {@link #onLogoutError()}.
     * 
     * @param url
     *            URL of logout method
     * @param handler
     *            LoginController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    private static native void jsniLogout(String url, LoginController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else { outputError(); }
            }
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.LoginController::onLogoutError()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.LoginController::onLoggedOut()();
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("GET", url + ".json");
                xhr.onload = outputResult;
                xhr.onerror = outputError;
                xhr.ontimeout = outputError;
                xhr.send();
            } else {
                xhr.open('GET', url, true);
                xhr.setRequestHeader("Accept","application/json");
                xhr.onreadystatechange = readyStateHandler;
                xhr.send();
            }
        } else {
            outputError();
        }
    }-*/;

    private LoginView loginView;

    public LoginController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut, LoginEvents.LoginError,
                LoginEvents.AuthenticationFailure, LoginEvents.RequestLogin,
                LoginEvents.RequestLogout);
    }

    /**
     * Requests the current user's details from CommonSense. Only used to check if the login was
     * successful.
     */
    private void getCurrentUser() {
        final String url = Constants.URL_USERS + "/current";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        jsniCurrentUser(url, sessionId, this);
    }

    /**
     * Handles the response to the request for the current user from the remote server. Calls back
     * to {@link #onCurrentUser(UserModel)} if the user is retrieved successfully.
     * 
     * @param response
     *            the HTTP content String that was received from the server, should be something
     *            like "{"user":{"id":"1", ...}}"
     */
    private void handleCurrentUser(String response) {

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

                    // Log.d(TAG, "Current user: \'" + user + "\'");

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

    @Override
    public void handleEvent(AppEvent event) {
        EventType eventType = event.getType();

        if (eventType.equals(LoginEvents.RequestLogin)) {
            requestLogin(event);
        } else if (eventType.equals(LoginEvents.RequestLogout)) {
            requestLogout(event);
        } else {
            forwardToView(this.loginView, event);
        }
    }

    /**
     * Handles the response to the login request from the remote server. Calls through to
     * {@link #onLoggedIn(String)} if the server returned a session ID, and {@link #onLoginError()}
     * otherwise.
     * 
     * @param response
     *            the HTTP content String that was received from the server, should be something
     *            like "{"session_id":"1234ab"}"
     */
    private void handleLoginResponse(String response) {

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
                    Log.e(TAG, "Error parsing login response: \"session_id\" is not a JSON Object");
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

    /**
     * Creates an MD5 hash of a String, for hashing the password before sending it.
     * 
     * @param s
     *            String to hash
     * @return the hashed String, zero-padded to make it always 32 characters long
     */
    private String hash(String s) {
        String hashed = null;
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(s.getBytes("UTF-8"));
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            hashed = bigInt.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashed.length() < 32) {
                hashed = "0" + hashed;
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException hashing password: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException hashing password: " + e.getMessage());
        }
        return hashed;
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.loginView = new LoginView(this);
    }

    private void onAuthenticationFailure() {
        Log.w(TAG, "Authentication failure");
        Dispatcher.forwardEvent(LoginEvents.AuthenticationFailure);
    }

    private void onCurrentUser(UserModel user) {
        Registry.register(Constants.REG_USER, user);
        Dispatcher.forwardEvent(LoginEvents.LoggedIn, user);
    }

    private void onLoggedIn(String sessionId) {
        Registry.register(Constants.REG_SESSION_ID, sessionId);
        getCurrentUser();
    }

    private void onLoggedOut() {
        Registry.unregister(Constants.REG_SESSION_ID);
        Registry.unregister(Constants.REG_USER);
        Dispatcher.forwardEvent(LoginEvents.LoggedOut);
    }

    private void onLoginError() {
        Log.e(TAG, "Login error");
        Dispatcher.forwardEvent(LoginEvents.LoginError);
    }

    private void onLogoutError() {
        // TODO handle logout error events
        Log.e(TAG, "Logout error");
        onLoggedOut();
    }

    private void requestLogin(AppEvent event) {
        String url = Constants.URL_LOGIN;
        String name = event.<String> getData("username");
        String pass = event.<String> getData("password");
        String hashPass = hash(pass);
        String data = "{\"username\":\"" + name + "\",\"password\":\"" + hashPass + "\"}";

        jsniLogin(url, data, this);
    }

    private void requestLogout(AppEvent event) {
        String url = Constants.URL_LOGOUT;
        jsniLogout(url, this);
    }
}
