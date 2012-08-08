package nl.sense_os.commonsense.main.client.main;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.CommonSense;
import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.communication.httpresponse.CurrentUserResponseJso;
import nl.sense_os.commonsense.common.client.constant.Constants;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.common.client.model.UserModel;
import nl.sense_os.commonsense.main.client.main.components.NavPanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window.Location;

public class MainController extends Controller implements ValueChangeHandler<String> {

	private static final Logger LOG = Logger.getLogger(MainController.class.getName());

	/**
	 * Redirects the user to the login page
	 */
	public static void goToLoginPage() {
		UrlBuilder builder = new UrlBuilder();
		builder.setProtocol(Location.getProtocol());
		builder.setHost(Location.getHost());
		String path = Location.getPath().contains("index.html") ? Location.getPath().replace(
				"index.html", "login.html") : Location.getPath() + "login.html";
		builder.setPath(path);
		for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
			if ("session_id".equals(entry.getKey()) || "error".equals(entry.getKey())) {
				// do not copy the session id parameter
			} else {
				builder.setParameter(entry.getKey(), entry.getValue().toArray(new String[0]));
			}
		}
		Location.replace(builder.buildString().replace("127.0.0.1%3A", "127.0.0.1:"));
	}

	private View mainView;

	private String currentToken;

	public MainController() {
		registerEventTypes(MainEvents.Error, MainEvents.Init, MainEvents.UiReady,
				MainEvents.RequestLogout, MainEvents.LoggedIn);
	}

	/**
	 * Requests the current user's details from CommonSense. Only used to check if the login was
	 * successful.
	 */
	private void getCurrentUser() {
		LOG.finest("Get current user");

		// prepare request details
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		final String url = urlBuilder.setPath(Urls.PATH_USERS + "/current.json").buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET current user onError callback: " + exception.getMessage());
				SessionManager.removeSessionId();
				goToLoginPage();
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET current user response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					parseUserReponse(response.getText());
				} else if (Response.SC_FORBIDDEN == statusCode) {
					onLoginFailure(statusCode);
				} else {
					LOG.warning("GET current user returned incorrect status: " + statusCode);
					onLoginFailure(statusCode);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, callback);
		} catch (Exception e) {
			LOG.warning("GET current user request threw exception: " + e.getMessage());
			callback.onError(null, e);
		}
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type.equals(MainEvents.UiReady)) {
			forwardToView(mainView, event);
			onUiReady();

		} else if (type.equals(MainEvents.RequestLogout)) {
			logout();

		} else if (type.equals(MainEvents.LoggedIn)) {
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
		if (token.contains("session_id")) {
			String sessionId = token.substring("session_id=".length());

			if (null != sessionId && sessionId.length() > 0) {
				SessionManager.setSessionId(sessionId);
				getCurrentUser();

			} else {
				LOG.warning("Did not find Session ID after google authentication");
				onLoginFailure(-1);
			}

		} else if (Location.getParameter("session_id") != null) {
			LOG.fine("Google authentication landing");
			String newUrl = urlParameterToFragment(Location.getHref(), "session_id");

			// reload the app at the hacked URL
			Location.replace(newUrl);

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
		boolean loginRequired = token.equals(NavPanel.VISUALIZATION);
		return loginRequired;
	}

	private boolean isValidLocation(String token) {
		boolean valid = token.equals(NavPanel.SIGN_OUT);
		valid = valid || token.equals(NavPanel.HELP);
		valid = valid || token.equals(NavPanel.VISUALIZATION);
		return valid;
	}

	private void logout() {
		LOG.finest("Log out");
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onLogoutError(-1, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				if (response.getStatusCode() == Response.SC_OK) {
					onLoggedOut();
				} else {
					onLogoutError(response.getStatusCode(), new Throwable(response.getStatusText()));
				}
			}
		};
		CommonSense.logout(callback);
	}

	private void onCurrentUser(UserModel user) {

		Registry.register(Constants.REG_USER, user);
		Dispatcher.forwardEvent(new AppEvent(MainEvents.LoggedIn, user));

		History.newItem(NavPanel.VISUALIZATION);
		History.fireCurrentHistoryState();
	}

	private void onLoggedOut() {
		SessionManager.removeSessionId();
		goToLoginPage();
	}

	private void onLoginFailure(int i) {
		LOG.warning("Login failure: " + i);
		goToLoginPage();
	}

	private void onLogoutError(int code, Throwable error) {
		// TODO Auto-generated method stub
		LOG.warning("Logout failure! Code: " + code + " " + error);
		onLoggedOut();
	}

	private void onUiReady() {
		LOG.finest("UI ready");
		String sessionId = SessionManager.getSessionId();
		if (null != sessionId && sessionId.length() > 0) {
			getCurrentUser();
		} else {
			handleStartLocation();
		}
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		LOG.info("History value change: " + token);
		if (token.equals("") || false == isValidLocation(token)) {
			History.newItem(NavPanel.VISUALIZATION);
			History.fireCurrentHistoryState();
			return;
		}

		if (isLoginRequired(token) && null == SessionManager.getSessionId()) {
			LOG.warning("Not signed in: refusing new history token " + token);
			goToLoginPage();
			return;
		}

		AppEvent navEvent = new AppEvent(MainEvents.Navigate);
		navEvent.setData("old", currentToken);
		navEvent.setData("new", token);
		currentToken = token;
		forwardToView(mainView, navEvent);
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
				onLoginFailure(-1);
			}

		} else {
			LOG.severe("Error parsing current user response: response=null");
			onLoginFailure(-1);
		}
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
