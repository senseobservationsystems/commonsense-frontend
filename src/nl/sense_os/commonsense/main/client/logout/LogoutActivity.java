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
package nl.sense_os.commonsense.main.client.logout;

import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.MainEntryPoint;
import nl.sense_os.commonsense.shared.client.communication.SessionManager;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Activities are started and stopped by an ActivityManager associated with a container Widget.
 */
public class LogoutActivity extends AbstractActivity implements LogoutView.Presenter {

	private static final Logger LOG = Logger.getLogger(LogoutActivity.class.getName());

	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private final MainClientFactory clientFactory;

	public LogoutActivity(LogoutPlace place, MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * Requests logout of session from CommonSense
	 */
	private void logout() {
		LOG.finest("Log out");
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onLogoutError(-1, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				onLogoutResponse(response);
			}
		};
        CommonSenseClient.getClient().logout(callback);
	}

	/**
	 * Handles response to logout request
	 * 
	 * @param response
	 */
	private void onLogoutResponse(Response response) {
		if (Response.SC_OK == response.getStatusCode()) {
			onLogoutSuccess();
		} else {
			onLogoutError(response.getStatusCode(), new Throwable(response.getStatusText()));
		}
	}

	/**
	 * Handles successful logout
	 */
	private void onLogoutSuccess() {
		SessionManager.removeSessionId();
		MainEntryPoint.goToLoginPage();
	}

	/**
	 * Handles failed logout requests
	 * 
	 * @param code
	 *            Error code
	 * @param error
	 *            Error message
	 */
	private void onLogoutError(int code, Throwable error) {
		// TODO Handle logout failure
		LOG.warning("Logout failure! Code: " + code + " " + error);
		onLogoutSuccess();
	}

	@Override
	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
		LOG.info("Start 'logout' activity");

		// show view
		LogoutView view = clientFactory.getLogoutView();
		view.setPresenter(this);
		containerWidget.setWidget(view.asWidget());

		// log out
		logout();
	}
}
