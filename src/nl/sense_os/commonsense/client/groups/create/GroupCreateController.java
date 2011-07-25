package nl.sense_os.commonsense.client.groups.create;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.utility.Md5Hasher;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class GroupCreateController extends Controller {

    private static final Logger LOG = Logger.getLogger(GroupCreateController.class.getName());
    private View creator;

    public GroupCreateController() {
        registerEventTypes(GroupCreateEvents.ShowCreator, GroupCreateEvents.CreateComplete,
                GroupCreateEvents.CreateRequested);
    }

    private void createGroup(String name, String username, String password) {

        // prepare request properties
        final String url = Urls.GROUPS + ".json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // prepare request body
        String body = "{\"group\":{";
        body += "\"name\":\"" + name + "\"";
        if (null != username) {
            body += ",\"username\":\"" + username + "\"";
            body += ",\"password\":\"" + Md5Hasher.hash(password) + "\"";
        }
        body += "}}";

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("POST group onError callback: " + exception.getMessage());
                onCreateFailure();
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST group response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_CREATED == statusCode) {
                    onCreateSuccess();
                } else {
                    LOG.warning("POST group returned incorrect status: " + statusCode);
                    onCreateFailure();
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(body, reqCallback);
        } catch (RequestException e) {
            LOG.warning("POST group request threw exception: " + e.getMessage());
            onCreateFailure();
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(GroupCreateEvents.CreateRequested)) {
            // LOG.fine( "CreateRequested");
            final String name = event.getData("name");
            final String username = event.getData("username");
            final String password = event.getData("password");
            createGroup(name, username, password);

        } else

        /*
         * Pass through to view
         */
        {
            forwardToView(creator, event);

        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        creator = new GroupCreator(this);
    }

    private void onCreateFailure() {
        forwardToView(creator, new AppEvent(GroupCreateEvents.CreateFailed));
    }

    private void onCreateSuccess() {
        Dispatcher.forwardEvent(GroupCreateEvents.CreateComplete);
    }
}
