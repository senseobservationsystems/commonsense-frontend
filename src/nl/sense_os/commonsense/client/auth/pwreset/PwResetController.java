package nl.sense_os.commonsense.client.auth.pwreset;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.utility.Md5Hasher;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;

public class PwResetController extends Controller {

    private static final Logger LOG = Logger.getLogger(PwResetController.class.getName());
    private View requestDialog;
    private View newPasswordForm;

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
            FormPanel form = event.getData("form");
            requestReset(email, username, form);

        } else if (PwResetEvents.NewPasswordRequest.equals(type)) {
            LOG.finest("NewPasswordRequest");
            String password = event.getData("password");
            String token = event.getData("token");
            resetPassword(password, token);

        } else if (PwResetEvents.ShowNewPasswordForm.equals(type)) {
            forwardToView(newPasswordForm, event);

        } else {
            LOG.finest("Forward to requestDialog...");
            forwardToView(requestDialog, event);
        }
    }

    private void resetPassword(String password, String token) {

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
                onPasswordResetFailure(0);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST password reset response received: " + response.getStatusText());
                final int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onPasswordResetSuccess(response.getText());
                } else {
                    LOG.warning("POST password reset returned incorrect status: " + statusCode);
                    onPasswordResetFailure(statusCode);
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
            onPasswordResetFailure(0);
        }
    }

    private void onPasswordResetFailure(int status) {
        forwardToView(newPasswordForm, new AppEvent(PwResetEvents.NewPasswordFailure));
    }

    private void onPasswordResetSuccess(String response) {
        forwardToView(newPasswordForm, new AppEvent(PwResetEvents.NewPasswordSuccess));
    }

    private void requestReset(String email, String username, final FormPanel form) {

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
                onResetRequestFailure(0, form);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST password reset response received: " + response.getStatusText());
                final int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onResetRequestSuccess(response.getText(), form);
                } else if (Response.SC_NOT_FOUND == statusCode) {
                    onUserNotFound(form);
                } else {
                    LOG.warning("POST password reset request returned incorrect status: "
                            + statusCode);
                    onResetRequestFailure(statusCode, form);
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
            onResetRequestFailure(0, form);
        }
    }

    private void onUserNotFound(FormPanel form) {
        AppEvent event = new AppEvent(PwResetEvents.PwRemindNotFound);
        event.setData("form", form);
        forwardToView(requestDialog, event);
    }

    private void onResetRequestSuccess(String response, FormPanel form) {
        AppEvent event = new AppEvent(PwResetEvents.PwRemindSuccess);
        event.setData("form", form);
        forwardToView(requestDialog, event);
    }

    private void onResetRequestFailure(int statusCode, FormPanel form) {
        AppEvent event = new AppEvent(PwResetEvents.PwRemindFailure);
        event.setData("form", form);
        forwardToView(requestDialog, event);
    }

    @Override
    protected void initialize() {
        super.initialize();
        requestDialog = new PwResetDialog(this);
        newPasswordForm = new NewPwForm(this);
    }
}
