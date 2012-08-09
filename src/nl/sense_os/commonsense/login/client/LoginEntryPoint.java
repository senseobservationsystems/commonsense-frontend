/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nl.sense_os.commonsense.login.client;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.login.client.application.LoginApplicationView;
import nl.sense_os.commonsense.login.client.login.LoginPlace;
import nl.sense_os.commonsense.login.client.loginerror.LoginErrorPlace;
import nl.sense_os.commonsense.login.client.openidconnect.OpenIdConnectPlace;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;

public class LoginEntryPoint implements EntryPoint {

	private static final Logger LOG = Logger.getLogger(LoginEntryPoint.class.getName());

	/**
	 * Tries to get a session ID from either the URL parameters or from the domain cookies.
	 * 
	 * @return The session ID or null
	 */
	private String getAvailableSessionId() {

		String sessionId = Location.getParameter("session_id");
		if (null != sessionId && sessionId.length() > 0) {
			LOG.info("Got session ID from URL parameter");
			return sessionId;
		}

		sessionId = SessionManager.getSessionId();
		if (null != sessionId && sessionId.length() > 0) {
			LOG.info("Got session ID from cookie");
			return sessionId;
		}

		return null;
	}

	/**
	 * @return The value of the 'error' URL parameter, or null
	 */
	private String getErrorParameter() {
		String error = Location.getParameter("error");
		return error != null && error.length() > 0 ? error : null;
	}

	public void onModuleLoad() {

		String sessionId = getAvailableSessionId();
		if (null != sessionId) {
			SessionManager.setSessionId(sessionId);
			skipToMainPage();
		} else {
			startApplication();
		}
	}

	/**
	 * Redirects the user to the main page
	 */
	private void skipToMainPage() {
		UrlBuilder builder = new UrlBuilder();
		builder.setProtocol(Location.getProtocol());
		builder.setHost(Location.getHost());
		String path = Location.getPath().contains("login.html") ? Location.getPath().replace(
				"login.html", "index.html") : Location.getPath() + "index.html";
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

	/**
	 * Starts the application. Initializes the views and starts the default activity.
	 */
	private void startApplication() {

		// Create ClientFactory using deferred binding
		LoginClientFactory clientFactory = GWT.create(LoginClientFactory.class);
		EventBus eventBus = clientFactory.getEventBus();
		PlaceController placeController = clientFactory.getPlaceController();

		// prepare UI
		LoginApplicationView main = clientFactory.getMainView();
		SimplePanel appWidget = main.getActivityPanel();

		// Start ActivityManager for the main widget with our ActivityMapper
		ActivityMapper activityMapper = new LoginActivityMapper(clientFactory);
		ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
		activityManager.setDisplay(appWidget);

		// Start PlaceHistoryHandler with our PlaceHistoryMapper
		LoginPlaceHistoryMapper historyMapper = GWT.create(LoginPlaceHistoryMapper.class);
		PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
		historyHandler.register(placeController, eventBus, new LoginPlace(""));
		RootLayoutPanel.get().add(main);

		String errorMessage = getErrorParameter();
		if (null != errorMessage) {

			if (errorMessage.contains("e-mail address:")
					&& errorMessage.contains("is already registered")) {

				// parse email address
				int startIndex = errorMessage.indexOf("e-mail address: ")
						+ "e-mail address: ".length();
				int endIndex = errorMessage.indexOf(" is already registered");
				String email = errorMessage.substring(startIndex, endIndex);

				// TODO start google connect activity
				placeController.goTo(new OpenIdConnectPlace(email));

			} else {

				// handle error (probably from failed OpenID attempt)
				placeController.goTo(new LoginErrorPlace(errorMessage));
			}
		} else {
			// Goes to place represented on URL or default place
			historyHandler.handleCurrentHistory();
		}
	}
}
