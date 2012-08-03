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
package nl.sense_os.commonsense.login.client.forgotpassword;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.component.AlertDialogContent;
import nl.sense_os.commonsense.common.client.util.CommonSense;
import nl.sense_os.commonsense.login.client.ClientFactory;
import nl.sense_os.commonsense.login.client.mvp.ForgotPasswordPlace;
import nl.sense_os.commonsense.login.client.mvp.LoginPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * Activities are started and stopped by an ActivityManager associated with a container Widget.
 */
public class ForgotPasswordActivity extends AbstractActivity implements
		ForgotPasswordView.Presenter, AlertDialogContent.Presenter {

	private static final Logger LOG = Logger.getLogger(ForgotPasswordActivity.class.getName());

	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private ClientFactory clientFactory;

	private ForgotPasswordView view;
	private DialogBox successDialog;
	private DialogBox errorDialog;

	public ForgotPasswordActivity(ForgotPasswordPlace place, ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void cancel() {
		clientFactory.getPlaceController().goTo(new LoginPlace(""));
	}

	@Override
	public void dismissAlert() {
		if (null != successDialog) {
			successDialog.hide();
			successDialog = null;
			clientFactory.getPlaceController().goTo(new LoginPlace(""));
		}
		if (null != errorDialog) {
			errorDialog.hide();
			errorDialog = null;
		}
	}

	@Override
	public void forgotPassword(String username, String email) {

		// prepare request callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onForgotPasswordFailure(0, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				onForgotPasswordResponse(response);
			}
		};

		CommonSense.forgotPassword(callback, username, email);
	}

	private void onForgotPasswordFailure(int code, Throwable error) {
		errorDialog = new DialogBox();
		errorDialog.setHTML(SafeHtmlUtils.fromSafeConstant("<b>Request failed</b>"));

		AlertDialogContent content = new AlertDialogContent();
		content.setMessage("The request to reset your password failed! Error: " + code + " ("
				+ error.getMessage() + ")");
		content.setPresenter(this);

		errorDialog.setWidget(content);
		errorDialog.center();
	}

	private void onForgotPasswordResponse(Response response) {
		if (Response.SC_OK == response.getStatusCode()) {
			onForgotRequestSuccess();
		} else if (Response.SC_NOT_FOUND == response.getStatusCode()) {
			onUserNotFound();
		} else {
			onForgotPasswordFailure(response.getStatusCode(),
					new Throwable(response.getStatusText()));
		}
	}

	private void onForgotRequestSuccess() {
		successDialog = new DialogBox();
		successDialog.setHTML(SafeHtmlUtils.fromSafeConstant("<b>Requested password reset</b>"));

		AlertDialogContent content = new AlertDialogContent();
		content.setMessage("You will receive an email with a link to reset your password.");
		content.setPresenter(this);

		successDialog.setWidget(content);
		successDialog.center();
	}

	private void onUserNotFound() {
		errorDialog = new DialogBox();
		errorDialog.setHTML(SafeHtmlUtils.fromSafeConstant("<b>User not found</b>"));

		AlertDialogContent content = new AlertDialogContent();
		content.setMessage("We could not find a user in CommonSense with that username or email address.");
		content.setPresenter(this);

		errorDialog.setWidget(content);
		errorDialog.center();
	}

	@Override
	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
		LOG.info("Start activity: Forgot Password");

		view = clientFactory.getForgotPasswordView();
		view.reset();
		view.setPresenter(this);

		containerWidget.setWidget(view);
	}
}
