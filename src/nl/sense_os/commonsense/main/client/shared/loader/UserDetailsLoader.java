package nl.sense_os.commonsense.main.client.shared.loader;

import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.shared.client.communication.httpresponse.CurrentUserResponse;
import nl.sense_os.commonsense.shared.client.model.User;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class UserDetailsLoader implements Loader {

    private static final Logger LOG = Logger.getLogger(UserDetailsLoader.class.getName());
    private Callback callback;

    /**
     * Requests the current user's details from CommonSense
     */
    private void getCurrentUser() {
        LOG.fine("Get current user");

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                onGetCurrentUserFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                onGetCurrentUserResponse(response);
            }
        };

        CommonSenseApi.getCurrentUser(callback);
    }

    @Override
    public void load(Callback callback) {
        this.callback = callback;
        getCurrentUser();
    }

    /**
     * Handles failed request to get the current user details by redirecting to the login page.
     * 
     * @param code
     * @param error
     */
    private void onGetCurrentUserFailure(int code, Throwable error) {
        if (null != callback) {
            callback.onFailure(code, error);
        }
    }

    /**
     * Parses the response from CommonSense
     * 
     * @param response
     */
    private void onGetCurrentUserResponse(Response response) {
        int statusCode = response.getStatusCode();
        if (Response.SC_OK == statusCode) {
            CurrentUserResponse jso = JsonUtils.safeEval(response.getText());
            onGetCurrentUserSuccess(jso.getUser());
        } else {
            onGetCurrentUserFailure(statusCode, new Throwable(response.getStatusText()));
        }
    }

    /**
     * Handles the new user details
     * 
     * @param user
     */
    private void onGetCurrentUserSuccess(User user) {
        if (null != callback) {
            callback.onSuccess(user);
        }
    }
}
