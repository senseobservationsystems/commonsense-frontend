package nl.sense_os.commonsense.client.main;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.SessionManager;
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
import com.google.gwt.user.client.Window.Location;

public class MainController extends Controller implements ValueChangeHandler<String> {

    private static final Logger LOG = Logger.getLogger(MainController.class.getName());
    private View mainView;
    private String currentToken;

    public MainController() {
	registerEventTypes(MainEvents.Error, MainEvents.Init, MainEvents.UiReady);
	registerEventTypes(LoginEvents.LoginSuccess, LoginEvents.LoggedOut);
	// LOG.setLevel(Level.ALL);
    }

    @Override
    public void handleEvent(AppEvent event) {
	EventType type = event.getType();
	if (type.equals(MainEvents.UiReady)) {
	    forwardToView(mainView, event);
	    handleStartLocation();

	} else if (type.equals(LoginEvents.LoginSuccess)) {
	    onLoggedIn();
	    forwardToView(mainView, event);

	} else if (type.equals(LoginEvents.LoggedOut)) {
	    onLoggedOut();
	    forwardToView(mainView, event);

	} else {
	    forwardToView(mainView, event);
	}
    }

    /**
     * Handles the start location of the app, i.e. the initial URL where the user came in. After
     * Google authentication, the start location should contain session ID information and we can
     * log in immediately, otherwise the user will be redirected toward the home view.
     */
    private void handleStartLocation() {

	String token = History.getToken();
	if (token != null && token.contains("session_id=")) {
	    LOG.fine("Google authentication landing");

	    String sessionId = token.substring("session_id=".length());

	    if (null != sessionId && sessionId.length() > 0) {
		AppEvent authenticated = new AppEvent(LoginEvents.GoogleAuthResult);
		authenticated.setData("sessionId", sessionId);
		Dispatcher.forwardEvent(authenticated);
	    } else {
		LOG.warning("Did not find Session ID after google authentication");
		navigateHome();
	    }

	} else if (token != null && token.contains("error=")) {
	    LOG.warning("Google authentication error landing");
	    String errorMsg = token.substring("error=".length());
	    onError(errorMsg);

	    // } else if (!GWT.isProdMode() && Location.getParameter("session_id") != null) {
	} else if (Location.getParameter("session_id") != null) {
	    LOG.fine("Google authentication landing");
	    String newUrl = urlParameterToFragment(Location.getHref(), "session_id");

	    // reload the app at the hacked URL
	    Location.replace(newUrl);

	    // } else if (Location.getParameter("error") != null) {
	} else if (Location.getParameter("error") != null) {
	    LOG.warning("Google authentication error landing");
	    String newUrl = urlParameterToFragment(Location.getHref(), "error");

	    // reload the app at the hacked URL
	    Location.replace(newUrl);

	} else if (token != null && token.equals("resetPassword")) {
	    LOG.warning("Reset password landing");

	    History.fireCurrentHistoryState();

	} else {
	    History.fireCurrentHistoryState();
	}
    }

    @Override
    protected void initialize() {
	mainView = new MainView(this);

	History.addValueChangeHandler(this);

	super.initialize();
    }

    private boolean isLoginRequired(String token) {
	boolean loginRequired = token.equals(NavPanel.ACCOUNT)
		|| token.equals(NavPanel.VISUALIZATION);
	return loginRequired;
    }

    private boolean isValidLocation(String token) {
	boolean valid = token.equals(NavPanel.SIGN_OUT);
	valid = valid || token.equals(NavPanel.DEMO);
	valid = valid || token.equals(NavPanel.HOME);
	valid = valid || token.equals(NavPanel.HELP);
	valid = valid || token.equals(NavPanel.ACCOUNT);
	valid = valid || token.equals(NavPanel.VISUALIZATION);
	valid = valid || token.equals(NavPanel.RESET_PASSWORD);
	return valid;
    }

    /**
     * Navigates the application to the home view
     */
    private void navigateHome() {
	String startLocation = NavPanel.HOME;
	History.newItem(startLocation);
	History.fireCurrentHistoryState();
    }

    private void onError(String errorMsg) {

	if (errorMsg.contains("e-mail address:") && errorMsg.contains("is already registered")) {

	    // parse email address
	    int startIndex = errorMsg.indexOf("e-mail address: ") + "e-mail address: ".length();
	    int endIndex = errorMsg.indexOf(" is already registered");
	    String email = errorMsg.substring(startIndex, endIndex);

	    // dispatch event
	    AppEvent connect = new AppEvent(LoginEvents.GoogleAuthConflict);
	    connect.setData("email", email);
	    Dispatcher.forwardEvent(connect);

	} else {

	    // dispatch event
	    AppEvent error = new AppEvent(LoginEvents.GoogleAuthError);
	    error.setData("msg", errorMsg);
	    Dispatcher.forwardEvent(error);
	}
    }

    private void onLoggedIn() {
	History.newItem(NavPanel.VISUALIZATION);
	History.fireCurrentHistoryState();
    }

    private void onLoggedOut() {
	History.newItem(NavPanel.HOME);
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
	    String sessionId = SessionManager.getSessionId();
	    UserModel user = Registry.<UserModel> get(Constants.REG_USER);
	    if (null != sessionId) {
		if (null == user) {
		    AppEvent authenticated = new AppEvent(LoginEvents.GoogleAuthResult);
		    authenticated.setData("sessionId", sessionId);
		    Dispatcher.forwardEvent(authenticated);
		    return;
		}

	    } else {
		LOG.warning("Not signed in: refusing new history token " + token);
		History.newItem(NavPanel.HOME);
		History.fireCurrentHistoryState();
		return;
	    }
	}

	AppEvent navEvent = new AppEvent(MainEvents.Navigate);
	navEvent.setData("old", currentToken);
	navEvent.setData("new", token);
	currentToken = token;

	forwardToView(mainView, navEvent);
    }

    private String urlParameterToFragment(String url, String parameterToFragment) {
	String fragmentContent = Location.getParameter(parameterToFragment);

	// hack together a new URL without the session_id parameter
	String newUrl = Location.getProtocol() + "//" + Location.getHost() + Location.getPath();

	// append any other parameters
	String paramString = "?";
	Map<String, List<String>> params = Location.getParameterMap();
	for (Entry<String, List<String>> parameter : params.entrySet()) {
	    if (!parameter.getKey().equals(parameterToFragment)) {
		paramString += parameter.getKey() + "=" + parameter.getValue().get(0) + "&";
	    }
	}
	paramString = paramString.substring(0, paramString.length() - 1);
	newUrl += paramString;
	newUrl += "#" + parameterToFragment + "=" + fragmentContent;

	return newUrl;
    }
}
