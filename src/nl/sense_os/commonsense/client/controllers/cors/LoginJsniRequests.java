package nl.sense_os.commonsense.client.controllers.cors;

import nl.sense_os.commonsense.client.controllers.LoginController;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.UserModel;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * Helper class for cross-origin login requests using the JSNI. Has strict ties to
 * {@link LoginController}.
 */
public class LoginJsniRequests {

    private static final String TAG = "LoginJsniRequests";

    /**
     * Requests current user info from CommonSense. Calls back to
     * {@link #parseCurrentUser(String, LoginController)} or {@link LoginController#onLoginError()}.
     * 
     * @param url
     *            URL of /users/current GET method
     * @param sessionId
     *            session ID for authentication
     * @param handler
     *            LoginController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public native static void getCurrentUser(String url, String sessionId, LoginController handler) /*-{
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
            handler.@nl.sense_os.commonsense.client.controllers.LoginController::onLoginError()();
        }

        function outputResult() {
            @nl.sense_os.commonsense.client.controllers.cors.LoginJsniRequests::parseCurrentUser(Ljava/lang/String;Lnl/sense_os/commonsense/client/controllers/LoginController;)(xhr.responseText, handler);
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
    // @formatter:on

    /**
     * Requests login at CommonSense. Calls back to {@link #parseLogin(String, LoginController)},
     * {@link LoginController#onAuthenticationFailure()} or {@link LoginController#onLoginError()}.
     * 
     * @param url
     *            URL of /login POST method
     * @param data
     *            String with JSON encoded login data
     * @param handler
     *            LoginController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public native static void login(String url, String data, LoginController handler) /*-{
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
            handler.@nl.sense_os.commonsense.client.controllers.LoginController::onAuthenticationFailure()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.LoginController::onLoginError()();
        }

        function outputResult() {
            @nl.sense_os.commonsense.client.controllers.cors.LoginJsniRequests::parseLogin(Ljava/lang/String;Lnl/sense_os/commonsense/client/controllers/LoginController;)(xhr.responseText, handler);
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("POST", url + ".json");
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
    // @formatter:on

    /**
     * Requests logout from CommonSense. Calls back to {@link LoginController#onLoggedOut()} or
     * {@link LoginController#onLogoutError()}.
     * 
     * @param url
     *            URL of logout method
     * @param handler
     *            LoginController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public static native void logout(String url, LoginController handler) /*-{
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
            handler.@nl.sense_os.commonsense.client.controllers.LoginController::onLogoutError()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.controllers.LoginController::onLoggedOut()();
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
    // @formatter:on

    /**
     * Parses the response to the request for the current user from the remote server. Calls back to
     * {@link LoginController#onCurrentUser(UserModel)} if the user is retrieved successfully.
     * 
     * @param response
     *            the HTTP content String that was received from the server, should be something
     *            like "{"user":{"id":"1", ...}}"
     */
    private static void parseCurrentUser(String response, LoginController handler) {

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

                    handler.onCurrentUser(user);
                } else {
                    Log.e(TAG, "Error parsing current user response: "
                            + "\"user\" is not a valid JSONObject");
                    handler.onLoginError();
                }
            } else {
                Log.e(TAG, "Error parsing current user response: \"user\" JSONValue not found");
                handler.onLoginError();
            }
        } else {
            Log.e(TAG, "Error parsing current user response: response=null");
            handler.onLoginError();
        }
    }

    /**
     * Parses the response to the login request from the remote server. Calls through to
     * {@link LoginController#onLoggedIn(String)} if the server returned a session ID, and
     * {@link LoginController#onLoginError()} otherwise.
     * 
     * @param response
     *            the HTTP content String that was received from the server, should be something
     *            like "{"session_id":"1234ab"}"
     */
    private static void parseLogin(String response, LoginController handler) {

        if (response != null) {

            // try to get "session_id" object
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonVal = json.get("session_id");
            if (null != jsonVal) {
                JSONString jsonString = jsonVal.isString();
                if (null != jsonString) {
                    String sessionId = jsonString.stringValue();

                    handler.onLoggedIn(sessionId);
                } else {
                    Log.e(TAG, "Error parsing login response: \"session_id\" is not a JSON Object");
                    handler.onLoginError();
                }
            } else {
                Log.e(TAG, "Error parsing login response: \"session_id\" is is not found");
                handler.onLoginError();
            }
        } else {
            Log.e(TAG, "Error parsing login response: response=null");
            handler.onLoginError();
        }
    }
}
