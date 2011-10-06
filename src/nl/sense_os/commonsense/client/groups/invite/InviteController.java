package nl.sense_os.commonsense.client.groups.invite;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;

public class InviteController extends Controller {

    private static final Logger LOG = Logger.getLogger(InviteController.class.getName());
    private View inviter;

    public InviteController() {
        registerEventTypes(InviteEvents.ShowInviter, InviteEvents.InviteComplete,
                InviteEvents.InviteRequested);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(InviteEvents.InviteRequested)) {
            // LOG.fine( "InviteRequested");
            final int groupId = event.getData("groupId");
            final String email = event.getData("username");
            inviteUser(groupId, email);

        } else

        /*
         * Pass through to view
         */
        {
            forwardToView(this.inviter, event);
        }
    }

    private void onInviteSuccess() {
        Dispatcher.forwardEvent(InviteEvents.InviteComplete);
    }

    private void onInviteFailure() {
        forwardToView(this.inviter, new AppEvent(InviteEvents.InviteFailed));
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.inviter = new GroupInviter(this);
    }

    private void inviteUser(int groupId, String username) {

        // prepare request properties
        final Method method = RequestBuilder.POST;
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_GROUPS + "/" + groupId + "/users.json");
        final String url = urlBuilder.buildString();
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // prepare request body
        String body = "{\"user\":{\"username\":\"" + username + "\"}}";

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("POST group user onError callback: " + exception.getMessage());
                onInviteFailure();
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST group user response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_CREATED == statusCode) {
                    onInviteSuccess();
                } else {
                    LOG.warning("POST group user returned incorrect status: " + statusCode);
                    onInviteFailure();
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(method, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
        try {
            builder.sendRequest(body, reqCallback);
        } catch (RequestException e) {
            LOG.warning("POST group user request threw exception: " + e.getMessage());
            onInviteFailure();
        }
    }

}
