package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;

public class Login extends LayoutContainer {

    private static final String TAG = "Login";

    /**
     * Requests current user info from CommonSense. Calls back to {@link #handleCurrentUser(String)}
     * or {@link #onLoginFailed()}.
     * 
     * @param url
     *            URL of /users/current GET method
     * @param sessionId
     *            session ID for authentication
     * @param handler
     *            Login object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    private native static void jsniCurrentUser(String url, String sessionId, Login handler) /*-{
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
            handler.@nl.sense_os.commonsense.client.widgets.Login::onLoginFailed()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.widgets.Login::handleCurrentUser(Ljava/lang/String;)(xhr.responseText);
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
     * Requests login at CommonSense. Calls back to {@link #onLoginSuccess(String)} or
     * {@link #onLoginFailed()}.
     * 
     * @param url
     *            URL of /login POST method
     * @param data
     *            String with JSON encoded login data
     * @param handler
     *            Login object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    private native static void jsniLogin(String url, String data, Login handler) /*-{
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
            handler.@nl.sense_os.commonsense.client.widgets.Login::onLoginIncorrect()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.widgets.Login::onLoginFailed()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.widgets.Login::handleLoginResponse(Ljava/lang/String;)(xhr.responseText);
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
     * Requests logout from CommonSense. Calls back to {@link #onLogoutSuccess()} or
     * {@link #onLogoutFailed()}.
     * 
     * @param url
     *            URL of logout method
     * @param handler
     *            Login object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    private static native void jsniLogout(String url, Login handler) /*-{
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
            handler.@nl.sense_os.commonsense.client.widgets.Login::onLogoutFailed()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.widgets.Login::onLogoutSuccess()();
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

    private final AsyncCallback<UserModel> callback;
    private final TextField<String> password = new TextField<String>();
    private final CheckBox rememberMe = new CheckBox();
    private final TextField<String> username = new TextField<String>();

    /**
     * Constructor for Login widget. Starts by logging out the user, before displaying the login
     * form.
     * 
     * @param callback
     */
    public Login(AsyncCallback<UserModel> callback) {

        this.callback = callback;

        // start by logging out (just in case)
        logout();
    }

    private FormPanel createForm() {
        final FormData formData = new FormData("-10");

        // username field
        username.setFieldLabel("Username:");
        username.setAllowBlank(false);

        // auto-fill username from cookie
        final String cookieName = Cookies.getCookie("username");
        if (null != cookieName) {
            username.setValue(cookieName);
            username.setOriginalValue(cookieName);
        }

        // password field (will not be POSTed)
        password.setFieldLabel("Password:");
        password.setAllowBlank(false);
        password.setPassword(true);

        rememberMe.setLabelSeparator("");
        rememberMe.setBoxLabel("Remember username");
        rememberMe.setValue(true);

        // main form panel
        final FormPanel form = new FormPanel();
        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.setLabelSeparator("");
        form.setFrame(true);
        form.setHeading("CommonSense Login");
        form.setLabelWidth(100);
        form.setWidth(350);

        setupSubmitAction(form);

        form.add(username, formData);
        form.add(password, formData);
        form.add(rememberMe);

        return form;
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
     * to the {@link #callback} Object if the user is retrieved successfully.
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

                    callback.onSuccess(user);
                } else {
                    Log.e(TAG, "Error parsing result: \"user\" is not a valid JSONObject");
                    onLoginFailed();
                }
            } else {
                Log.e(TAG, "Error parsing result: \"user\" JSONValue not found");
                onLoginFailed();
            }
        } else {
            Log.e(TAG, "Error parsing result: response=null");
            onLoginFailed();
        }
    }

    /**
     * Handles the response to the login request from the remote server. Calls through to
     * {@link #onLoginSuccess(String)} if the server returned a session ID, and
     * {@link #onLoginFailed()} otherwise.
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

                    onLoginSuccess(sessionId);
                } else {
                    Log.e(TAG, "Error parsing login response: \"user\" is not a JSON Object");
                    onLoginFailed();
                }
            } else {
                Log.e(TAG, "Error parsing login response: \"user\" is is not found");
                onLoginFailed();
            }
        } else {
            Log.e(TAG, "Error parsing login response: response=null");
            onLoginFailed();
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

    /**
     * Calls the JSNI login method with the login URL, user name and hashed password. This will call
     * back to {@link #onLoginSuccess(String)}, {@link #onLoginFailed()}, or
     * {@link #onLoginIncorrect()}.
     */
    private void login() {
        String url = Constants.URL_LOGIN;
        String name = username.getValue();
        String hashPass = hash(password.getValue());
        String data = "{\"username\":\"" + name + "\",\"password\":\"" + hashPass + "\"}";

        jsniLogin(url, data, this);
    }

    /**
     * Logs out from CommonSense using JSNI. Calls back to {@link #onLogoutSuccess()} or
     * {@link #onLogoutFailed()} after request.
     */
    private void logout() {
        jsniLogout(Constants.URL_LOGOUT, this);
    }

    /**
     * Call back method for login requests that failed to complete. Clears the password field and
     * notifies the user to retry or give up.
     */
    private void onLoginFailed() {
        password.clear();
        MessageBox.alert("Login failed!", "Connection error. Please try again.", null);
    }

    /**
     * Call back method for login requests that returned an authorization error. Clears the password
     * field and notifies the user that his user name or password were incorrect.
     */
    private void onLoginIncorrect() {
        password.clear();
        MessageBox.alert("Login failed!", "Incorrect user name or password. Please try again.",
                null);
    }

    /**
     * Call back method for successful login requests. Saves the session ID and start the request
     * for the details of the current user.
     * 
     * @param sessionId
     *            the session ID that was retrieved from the server's response.
     * @see #getCurrentUser()
     */
    private void onLoginSuccess(String sessionId) {

        Registry.register(Constants.REG_SESSION_ID, sessionId);

        if (rememberMe.getValue()) {
            long expiry = 1000l * 60 * 60 * 24 * 14; // 2 weeks
            Date expires = new Date(new Date().getTime() + expiry);
            Cookies.setCookie("username", username.getValue(), expires);
        }

        getCurrentUser();
    }

    /**
     * Call back method for failed logout requests. Simply ignores the failure and calls through to
     * {@link #onLogoutSuccess()}.
     */
    // TODO find out whether it is a problem when the logout fails.
    private void onLogoutFailed() {
        Log.e(TAG, "Logout failed!");
        onLogoutSuccess();
    }

    /**
     * Call back method for successful logout requests. Displays the login form.
     */
    private void onLogoutSuccess() {
        showLoginForm();
    }

    /**
     * Defines how to submit the form, and the actions to take when the form is submitted.
     */
    private void setupSubmitAction(final FormPanel form) {

        // submit button
        final Button b = new Button("Submit");
        b.setType("submit");
        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent be) {
                form.submit();
            }
        });
        form.addButton(b);
        form.setButtonAlign(HorizontalAlignment.CENTER);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(b);

        // ENTER-key listener to submit the form using the keyboard
        final KeyListener submitListener = new KeyListener() {
            @Override
            public void componentKeyDown(ComponentEvent event) {
                if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (form.isValid()) {
                        form.submit();
                    }
                }
            }
        };
        username.addKeyListener(submitListener);
        password.addKeyListener(submitListener);

        // form action is not a regular URL, but we perform a custom login request instead
        form.setAction("javascript:;");
        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                login();
            }

        });
    }

    /**
     * Creates and adds the login form to the window. Should be done after the logout request is
     * finished.
     */
    private void showLoginForm() {
        final FormPanel form = createForm();
        this.removeAll();
        this.setLayout(new CenterLayout());
        this.add(form);
        layout();
    }
}
