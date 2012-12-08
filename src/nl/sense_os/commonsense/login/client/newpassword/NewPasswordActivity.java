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
package nl.sense_os.commonsense.login.client.newpassword;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.login.client.LoginClientFactory;
import nl.sense_os.commonsense.shared.client.communication.SessionManager;
import nl.sense_os.commonsense.shared.client.component.AlertDialogContent;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DialogBox;

public class NewPasswordActivity extends AbstractActivity implements NewPasswordView.Presenter,
		AlertDialogContent.Presenter {

	/**
	 * Removes any URL parameters that are not useful anymore after a new password was set.
	 */
	private static void cleanUrlParameters() {

		// clear any session ID to prevent from bouncing back immediately
		SessionManager.removeSessionId();

		UrlBuilder builder = new UrlBuilder();
		builder.setProtocol(Location.getProtocol());
		builder.setHost(Location.getHost());
		builder.setPath(Location.getPath());
		for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
			if ("session_id".equals(entry.getKey()) || "error".equals(entry.getKey())
					|| "token".equals(entry.getKey())) {
				// do not copy the session id, error, or token parameters
			} else {
				builder.setParameter(entry.getKey(), entry.getValue().toArray(new String[0]));
			}
		}
		String newLocation = builder.buildString();

		// do not mangle the GWT development server parameter
		newLocation = newLocation.replace("127.0.0.1%3A", "127.0.0.1:");

		// relocate
		Location.replace(newLocation);
	}

	private static final Logger LOG = Logger.getLogger(NewPasswordActivity.class.getName());

	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private LoginClientFactory clientFactory;

	/**
	 * Token for password reset request
	 */
	private String token;

	private DialogBox alertDialog;
	private NewPasswordView view;

	public NewPasswordActivity(NewPasswordPlace place, LoginClientFactory clientFactory) {
		this.token = place.getToken();
		this.clientFactory = clientFactory;
	}

	@Override
	public void cancel() {
		cleanUrlParameters();
	}

	@Override
	public void dismissAlert() {
		cleanUrlParameters();
	}

	private void onPasswordResetFailure(int code, Throwable error) {

		alertDialog = new DialogBox();
		alertDialog.setHTML(SafeHtmlUtils.fromSafeConstant("<b>Request failed</b>"));

		AlertDialogContent content = new AlertDialogContent();
		content.setMessage("Your password was not changed. Error: " + code + " ("
				+ error.getMessage() + "). Please try to reset your password again.");
		content.setPresenter(this);

		alertDialog.setWidget(content);
		alertDialog.center();
	}

	private void onPasswordResetResponse(Response response) {
		LOG.finest("POST password reset response received: " + response.getStatusText());
		final int statusCode = response.getStatusCode();
		if (Response.SC_OK == statusCode) {
			onPasswordResetSuccess(response.getText());
		} else {
			LOG.warning("POST password reset returned incorrect status: " + statusCode);
			onPasswordResetFailure(statusCode, new Exception(response.getStatusText()));
		}
	}

	private void onPasswordResetSuccess(String text) {

		alertDialog = new DialogBox();
		alertDialog.setHTML(SafeHtmlUtils.fromSafeConstant("<b>Password changed</b>"));

		AlertDialogContent content = new AlertDialogContent();
		content.setMessage("Your password was succesfully changed. You can now log in.");
		content.setPresenter(this);

		alertDialog.setWidget(content);
		alertDialog.center();
	}

	private void resetPassword(String password) {

		// prepare request callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST password reset onError callback: " + exception.getMessage());
				onPasswordResetFailure(0, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				onPasswordResetResponse(response);

			}
		};

		// send request
        CommonSenseClient.getClient().resetPassword(callback, password, token);
	}

	@Override
	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {

		view = clientFactory.getNewPasswordView();
		view.setPresenter(this);
		view.reset();

		containerWidget.setWidget(view);
	}

	@Override
	public void submit(String password) {
		resetPassword(password);
	}
}
