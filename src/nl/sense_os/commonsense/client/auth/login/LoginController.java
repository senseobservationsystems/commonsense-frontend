package nl.sense_os.commonsense.client.auth.login;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.common.utility.Md5Hasher;
import nl.sense_os.commonsense.client.main.MainEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window.Location;

public class LoginController extends Controller {

    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());
    private View loginView;

    public LoginController() {

        // LOG.setLevel(Level.ALL);

        registerEventTypes(MainEvents.Init);

        // general login events
        registerEventTypes(LoginEvents.LoginSuccess, LoginEvents.LoggedOut,
                LoginEvents.LoginRequest, LoginEvents.RequestLogout);
        registerEventTypes(LoginEvents.GoogleAuthRequest, LoginEvents.GoogleAuthResult,
                LoginEvents.GoogleAuthConflict, LoginEvents.GoogleAuthError,
                LoginEvents.GoogleConnectRequest);

        // local events
        registerEventTypes(LoginEvents.LoginFailure, LoginEvents.AuthenticationFailure);

        // layout events
        registerEventTypes(LoginEvents.Show);
    }

    /**
     * Requests the current user's details from CommonSense. Only used to check if the login was
     * successful.
     */
    private void getCurrentUser() {

        // prepare request details
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        final String url = urlBuilder.setPath(Urls.PATH_USERS + "/current.json").buildString();
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("GET current user onError callback: " + exception.getMessage());
                onLoginFailure(0);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("GET current user response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    parseUserReponse(response.getText());
                } else if (Response.SC_FORBIDDEN == statusCode) {
                    onAuthenticationFailure();
                } else {
                    LOG.warning("GET current user returned incorrect status: " + statusCode);
                    onLoginFailure(statusCode);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(null, callback);
        } catch (RequestException e) {
            LOG.warning("GET current user request threw exception: " + e.getMessage());
            onLoginFailure(0);
        }
    }

    /**
     * Connects the current CommonSense user account with a Google account, for easy logging in with
     * OpenID.
     * 
     * @param sessionId
     *            The session ID of the current user.
     */
    private void googleConnect(String sessionId) {

        final String callback = Location.getProtocol() + "//" + Location.getHost()
                + Location.getPath() + Location.getQueryString();

        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_LOGIN_GOOGLE + ".json");
        urlBuilder.setParameter("callback_url", callback);
        urlBuilder.setParameter("session_id", sessionId);

        Location.replace(urlBuilder.buildString());
    }

    /**
     * When CommonSense detects that a user already has a "regular" account after authentication
     * through Google's OpenID server, we have to confirm this and connect the old account to the
     * new authentication. The user has to log in with his old password and then we can continue.
     * 
     * @param username
     *            The username to use for log in.
     * @param password
     *            The password to user for log in. Will be hashed before submission.
     */
    private void googleConnectConfirm(String username, String password) {

        // prepare request details
        final Method method = RequestBuilder.POST;
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_LOGIN + ".json");
        final String url = urlBuilder.buildString();
        final String body = "{\"username\":\"" + username + "\",\"password\":\""
                + Md5Hasher.hash(password) + "\"}";

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("POST login onError callback: " + exception.getMessage());
                onLoginFailure(0);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST login response received: " + response.getStatusText());
                final int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onGoogleConnectConfirmSuccess(response.getText());
                } else if (Response.SC_FORBIDDEN == statusCode) {
                    onAuthenticationFailure();
                } else {
                    LOG.warning("POST login returned incorrect status: " + statusCode);
                    onLoginFailure(statusCode);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(method, url);
        builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
        try {
            builder.sendRequest(body, callback);
        } catch (RequestException e) {
            LOG.warning("POST login request threw exception: " + e.getMessage());
            onLoginFailure(0);
        }
    }
    /**
     * Requests a session ID by authentication through Google's OpenID server.
     */
    private void googleLogin() {

        final String callback = Location.getProtocol() + "//" + Location.getHost()
                + Location.getPath() + Location.getQueryString();

        final UrlBuilder urlBuilder = new UrlBuilder().setHost("api.sense-os.nl");
        urlBuilder.setPath(Urls.PATH_LOGIN_GOOGLE + ".json");
        urlBuilder.setParameter("callback_url", callback);

        Location.replace(urlBuilder.buildString());
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType eventType = event.getType();

        if (eventType.equals(LoginEvents.LoginRequest)) {
            LOG.finest("LoginRequest");
            final String username = event.<String> getData("username");
            final String password = event.<String> getData("password");
            login(username, password);

        } else if (eventType.equals(LoginEvents.GoogleAuthRequest)) {
            LOG.finest("GoogleAuthRequest");
            googleLogin();

        } else if (eventType.equals(LoginEvents.GoogleAuthResult)) {
            LOG.finest("GoogleAuthResult");
            final String sessionId = event.getData("sessionId");
            onGoogleAuthResult(sessionId);

        } else if (eventType.equals(LoginEvents.GoogleConnectRequest)) {
            LOG.finest("GoogleConnectRequest");
            final String username = event.getData("username");
            final String password = event.getData("password");
            googleConnectConfirm(username, password);

        } else if (eventType.equals(LoginEvents.RequestLogout)) {
            LOG.finest("RequestLogout");
            logout();

        } else {
            forwardToView(loginView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        loginView = new LoginPanel(this);
    }

    /**
     * Sends a login request to the CommonSense API.
     * 
     * @param username
     *            The username to use for log in.
     * @param password
     *            The password to user for log in. Will be hashed before submission.
     */
    private void login(String username, String password) {

        // prepare request details
        final Method method = RequestBuilder.POST;
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_LOGIN + ".json");
        final String url = urlBuilder.buildString();
        String body = "{\"username\":\"" + username + "\",\"password\":\""
                + Md5Hasher.hash(password) + "\"}";

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("POST login onError callback: " + exception.getMessage());
                onLoginFailure(0);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST login response received: " + response.getStatusText());
                final int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onLoginSuccess(response.getText());
                } else if (Response.SC_FORBIDDEN == statusCode) {
                    onAuthenticationFailure();
                } else {
                    LOG.warning("POST login returned incorrect status: " + statusCode);
                    onLoginFailure(statusCode);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(method, url);
        builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
        try {
            builder.sendRequest(body, callback);
        } catch (RequestException e) {
            LOG.warning("POST login request threw exception: " + e.getMessage());
            onLoginFailure(0);
        }
    }

    /**
     * Sends a logout request for the current session to the CommonSense API.
     */
    private void logout() {

        // prepare request properties
        final String url = new UrlBuilder().setHost(Urls.HOST).setPath(Urls.PATH_LOGOUT + ".json")
                .buildString();
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);

        // prepare callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("GET logout onError callback: " + exception.getMessage());
                onLogoutFailure(0);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("GET logout response received: " + response.getStatusText());
                if (Response.SC_OK == response.getStatusCode()) {
                    onLoggedOut(response.getText());
                } else {
                    LOG.warning("GET logout returned incorrect status: " + response.getStatusCode());
                    onLogoutFailure(0);
                }
            }
        };

        // perform request
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(null, callback);
        } catch (RequestException e) {
            LOG.warning("GET logout request threw exception: " + e.getMessage());
            onLogoutFailure(0);
        }
    }

    private void onAuthenticationFailure() {
        forwardToView(loginView, new AppEvent(LoginEvents.AuthenticationFailure));
    }

    private void onCurrentUser(UserModel user) {
        Registry.register(Constants.REG_USER, user);
        Dispatcher.forwardEvent(LoginEvents.LoginSuccess, user);
    }

    private void onGoogleAuthResult(String sessionId) {
        LOG.fine("Session ID: " + sessionId);
        Registry.register(Constants.REG_SESSION_ID, sessionId);
        getCurrentUser();
    }

    private void onGoogleConnectConfirmSuccess(String response) {
        if (response != null) {

            // try to get "session_id" object
            String sessionId = null;
            if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
                LoginResponseJso jso = JsonUtils.unsafeEval(response);
                sessionId = jso.getSessionId();
            }

            if (null != sessionId) {
                Registry.register(Constants.REG_SESSION_ID, sessionId);
                googleConnect(sessionId);

            } else {
                onLoginFailure(0);
            }

        } else {
            LOG.severe("Error parsing login response: response=null");
            onLoginFailure(0);
        }
    }

    private void onLoggedOut(String response) {
        Registry.unregister(Constants.REG_SESSION_ID);
        Registry.unregister(Constants.REG_USER);
        Registry.unregister(Constants.REG_SERVICES);
        Dispatcher.forwardEvent(LoginEvents.LoggedOut);
    }

    private void onLoginFailure(int code) {
        AppEvent errorEvent = new AppEvent(LoginEvents.LoginFailure);
        errorEvent.setData("code", code);
        forwardToView(loginView, errorEvent);
    }

    private void onLoginSuccess(String response) {

        // LOG.setLevel (Level.ALL);
        if (response != null) {
            LOG.fine("LOGIN Success...");
            // try to get "session_id" object
            String sessionId = null;
            if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
                LoginResponseJso jso = JsonUtils.unsafeEval(response);
                sessionId = jso.getSessionId();
            }

            // LOG.fine ("sessionId is " + sessionId);

            if (null != sessionId) {
                Registry.register(Constants.REG_SESSION_ID, sessionId);

                // / mine!!!!
                Cookies.setCookie(Constants.REG_SESSION_ID, sessionId);

                getCurrentUser();

            } else {
                onLoginFailure(0);
            }

        } else {
            LOG.severe("Error parsing login response: response=null");
            onLoginFailure(0);
        }
    }

    private void onLogoutFailure(int code) {
        // TODO handle logout error events
        onLoggedOut("Status code: " + code);
    }

    private void parseUserReponse(String response) {
        if (response != null) {

            // try to get "user" object
            UserModel user = null;
            if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
                CurrentUserResponseJso jso = JsonUtils.unsafeEval(response);
                user = jso.getUser();
            }

            if (null != user) {
                onCurrentUser(user);
            } else {
                LOG.severe("Unexpected current user response");
                onLoginFailure(0);
            }

        } else {
            LOG.severe("Error parsing current user response: response=null");
            onLoginFailure(0);
        }
    }

    // private void xhrGetCurrentUser() {
    // final String url = Urls.USERS + "/current.json";
    // final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
    //
    // // send request to AjaxController
    // AppEvent requestEvent = new AppEvent(AjaxEvents.Request);
    // requestEvent.setData("method", "GET");
    // requestEvent.setData("url", url);
    // requestEvent.setData("session_id", sessionId);
    // requestEvent.setData("onSuccess", new AppEvent(LoginEvents.AjaxUserSuccess));
    // requestEvent.setData("onFailure", new AppEvent(LoginEvents.AjaxUserFailure));
    // Dispatcher.forwardEvent(requestEvent);
    // }

    // private void xhrLogin(String username, String password) {
    //
    // // prepare request properties
    // String url = Urls.LOGIN + ".json";
    // String hashPass = Md5Hasher.hash(password);
    // String body = "{\"username\":\"" + username + "\",\"password\":\"" + hashPass + "\"}";
    //
    // // send request to AjaxController
    // AppEvent requestEvent = new AppEvent(AjaxEvents.Request);
    // requestEvent.setData("method", "POST");
    // requestEvent.setData("url", url);
    // requestEvent.setData("body", body);
    // requestEvent.setData("onSuccess", new AppEvent(LoginEvents.AjaxLoginSuccess));
    // requestEvent.setData("onFailure", new AppEvent(LoginEvents.AjaxLoginFailure));
    // Dispatcher.forwardEvent(requestEvent);
    // }

    // private void xhrLogout(AppEvent event) {
    //
    // // prepare request properties
    // String url = Urls.LOGOUT + ".json";
    // String sessionId = Registry.get(Constants.REG_SESSION_ID);
    //
    // // send request to AjaxController
    // AppEvent requestEvent = new AppEvent(AjaxEvents.Request);
    // requestEvent.setData("method", "GET");
    // requestEvent.setData("url", url);
    // requestEvent.setData("session_id", sessionId);
    // requestEvent.setData("onSuccess", new AppEvent(LoginEvents.AjaxLogoutSuccess));
    // requestEvent.setData("onFailure", new AppEvent(LoginEvents.AjaxLogoutFailure));
    // Dispatcher.forwardEvent(requestEvent);
    // }
}
