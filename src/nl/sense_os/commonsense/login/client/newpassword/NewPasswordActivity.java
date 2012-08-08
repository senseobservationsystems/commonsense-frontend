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

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.CommonSense;
import nl.sense_os.commonsense.common.client.component.AlertDialogContent;
import nl.sense_os.commonsense.login.client.ClientFactory;
import nl.sense_os.commonsense.login.client.mvp.LoginPlace;
import nl.sense_os.commonsense.login.client.mvp.NewPasswordPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DialogBox;

public class NewPasswordActivity extends AbstractActivity implements NewPasswordView.Presenter,
		AlertDialogContent.Presenter {

	private static final Logger LOG = Logger.getLogger(NewPasswordActivity.class.getName());

	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private ClientFactory clientFactory;

	/**
	 * Token for password reset request
	 */
	private String token;

	private DialogBox alertDialog;
	private NewPasswordView view;

	public NewPasswordActivity(NewPasswordPlace place, ClientFactory clientFactory) {
		this.token = place.getToken();
		this.clientFactory = clientFactory;
	}

	@Override
	public void cancel() {
		clientFactory.getPlaceController().goTo(new LoginPlace(""));
	}

	@Override
	public void dismissAlert() {
		alertDialog.hide();
		clientFactory.getPlaceController().goTo(new LoginPlace(""));
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
		CommonSense.resetPassword(callback, password, token);
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
