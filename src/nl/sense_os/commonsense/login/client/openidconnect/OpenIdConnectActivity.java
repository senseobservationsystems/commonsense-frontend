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
package nl.sense_os.commonsense.login.client.openidconnect;

import nl.sense_os.commonsense.common.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.lib.client.model.httpresponse.LoginResponse;
import nl.sense_os.commonsense.login.client.LoginClientFactory;
import nl.sense_os.commonsense.login.client.login.LoginPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Activities are started and stopped by an ActivityManager associated with a container Widget.
 */
public class OpenIdConnectActivity extends AbstractActivity implements OpenIdConnectView.Presenter {
	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private LoginClientFactory clientFactory;

	private String email;

	public OpenIdConnectActivity(OpenIdConnectPlace place, LoginClientFactory clientFactory) {
		this.email = place.getEmail();
		this.clientFactory = clientFactory;
	}

	@Override
	public void cancel() {
		clientFactory.getPlaceController().goTo(new LoginPlace(""));
	}

	@Override
	public void connectAccount(String username, String password) {

		// log in to get session ID
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onLoginError(0, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				onLoginResponse(response);
			}
		};
		CommonSenseApi.login(callback, username, password);
	}

	/**
	 * Connects the current CommonSense user account with a Google account, for easy logging in with
	 * OpenID.
	 */
	private void googleConnect() {
		CommonSenseApi.googleConnect();
	}

	private void onAuthenticationFailure() {
		// TODO Auto-generated method stub

	}

	private void onLoginError(int code, Throwable error) {
		// TODO Auto-generated method stub

	}

	private void onLoginResponse(Response response) {
		final int statusCode = response.getStatusCode();
		if (Response.SC_OK == statusCode) {
			onLoginSuccess(response.getText());
		} else if (Response.SC_FORBIDDEN == statusCode) {
			onAuthenticationFailure();
		} else {
			onLoginError(statusCode, new Throwable(response.getStatusText()));
		}
	}

	private void onLoginSuccess(String response) {
		if (response != null) {

			// try to get "session_id" object
			String sessionId = null;
			LoginResponse jso = LoginResponse.create(response).cast();
			if (null != jso) {
				sessionId = jso.getSessionId();
			}

			if (null != sessionId) {
				SessionManager.setSessionId(sessionId);
				googleConnect();

			} else {
				onLoginError(0, new Exception("Did not receive session ID"));
			}

		} else {
			onLoginError(0, new Exception("No response content"));
		}

	}

	@Override
	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
		OpenIdConnectView view = clientFactory.getOpenIdConnectView();
		view.setEmail(email);
		view.setPresenter(this);
		containerWidget.setWidget(view.asWidget());
	}
}
