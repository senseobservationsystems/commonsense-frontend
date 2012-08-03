package nl.sense_os.commonsense.main.client.auth.pwreset;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.common.client.util.Md5Hasher;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;

public class PwResetController extends Controller {

	private static final Logger LOG = Logger.getLogger(PwResetController.class.getName());

	public PwResetController() {
		registerEventTypes(PwResetEvents.ShowDialog, PwResetEvents.SubmitRequest);
		registerEventTypes(PwResetEvents.ShowNewPasswordForm, PwResetEvents.NewPasswordRequest);

		// LOG.setLevel(Level.ALL);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (PwResetEvents.SubmitRequest.equals(type)) {
			LOG.finest("SubmitRequest");
			String email = event.getData("email");
			String username = event.getData("username");
			View source = (View) event.getSource();
			requestReset(email, username, source);

		} else if (PwResetEvents.NewPasswordRequest.equals(type)) {
			LOG.finest("NewPasswordRequest");
			String password = event.getData("password");
			String token = event.getData("token");
			View source = (View) event.getSource();
			resetPassword(password, token, source);

		} else if (PwResetEvents.ShowNewPasswordForm.equals(type)) {
			LOG.finest("Create NewPwView...");
			forwardToView(new NewPwView(this), event);

		} else {
			LOG.finest("Create ForgotPwView...");
			forwardToView(new ForgotPwView(this), event);
		}
	}

	private void resetPassword(String password, String token, final View source) {

		// prepare request details
		final Method method = RequestBuilder.POST;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_PW_RESET + ".json");
		final String url = urlBuilder.buildString();
		String body = "{\"password\":\"" + Md5Hasher.hash(password) + "\",\"token\":\"" + token
				+ "\"}";

		// prepare request callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST password reset onError callback: " + exception.getMessage());
				onPasswordResetFailure(0, source);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("POST password reset response received: " + response.getStatusText());
				final int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onPasswordResetSuccess(response.getText(), source);
				} else {
					LOG.warning("POST password reset returned incorrect status: " + statusCode);
					onPasswordResetFailure(statusCode, source);
				}
			}
		};

		// send request
		RequestBuilder builder = new RequestBuilder(method, url);
		builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
		try {
			builder.sendRequest(body, callback);
		} catch (RequestException e) {
			LOG.warning("POST password reset request threw exception: " + e.getMessage());
			onPasswordResetFailure(0, source);
		}
	}

	private void onPasswordResetFailure(int status, View source) {
		forwardToView(source, new AppEvent(PwResetEvents.NewPasswordFailure));
	}

	private void onPasswordResetSuccess(String response, View source) {
		forwardToView(source, new AppEvent(PwResetEvents.NewPasswordSuccess));
	}

	private void requestReset(String email, String username, final View source) {

		// prepare request details
		final Method method = RequestBuilder.POST;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_PW_RESET_REQUEST + ".json");
		final String url = urlBuilder.buildString();
		String body = null;
		if (null != email) {
			body = "{\"email\":\"" + email + "\"}";
		} else {
			body = "{\"username\":\"" + username + "\"}";
		}

		// prepare request callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST password reset request onError callback: "
						+ exception.getMessage());
				onResetRequestFailure(0, source);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("POST password reset response received: " + response.getStatusText());
				final int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onResetRequestSuccess(response.getText(), source);
				} else if (Response.SC_NOT_FOUND == statusCode) {
					onUserNotFound(source);
				} else {
					LOG.warning("POST password reset request returned incorrect status: "
							+ statusCode);
					onResetRequestFailure(statusCode, source);
				}
			}
		};

		// send request
		RequestBuilder builder = new RequestBuilder(method, url);
		builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
		try {
			builder.sendRequest(body, callback);
		} catch (RequestException e) {
			LOG.warning("POST password reset request threw exception: " + e.getMessage());
			onResetRequestFailure(0, source);
		}
	}

	private void onUserNotFound(View source) {
		forwardToView(source, new AppEvent(PwResetEvents.PwRemindNotFound));
	}

	private void onResetRequestSuccess(String response, View source) {
		forwardToView(source, new AppEvent(PwResetEvents.PwRemindSuccess));
	}

	private void onResetRequestFailure(int statusCode, View source) {
		forwardToView(source, new AppEvent(PwResetEvents.PwRemindFailure));
	}
}
