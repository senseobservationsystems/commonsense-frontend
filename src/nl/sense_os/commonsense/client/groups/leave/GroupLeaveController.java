package nl.sense_os.commonsense.client.groups.leave;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.GroupModel;

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
import com.google.gwt.http.client.UrlBuilder;

public class GroupLeaveController extends Controller {

    private static final Logger LOG = Logger.getLogger(GroupLeaveController.class.getName());

    public GroupLeaveController() {
        LOG.setLevel(Level.ALL);
        registerEventTypes(GroupLeaveEvents.LeaveRequest, GroupLeaveEvents.Leave);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupLeaveEvents.Leave)) {
            LOG.finest("Leave");
            final GroupModel group = event.getData("group");
            View source = (View) event.getSource();
            leave(group, source);

        } else if (type.equals(GroupLeaveEvents.LeaveRequest)) {
            LOG.finest("LeaveRequest");
            GroupLeaveDialog view = new GroupLeaveDialog(this);
            forwardToView(view, event);

        } else {
            LOG.warning("Unexpected event: " + event);
        }
    }

    private void leave(GroupModel group, final View source) {

        // prepare request property
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_GROUPS + "/" + group.getId() + ".json");
        final String url = urlBuilder.buildString();
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("DELETE group onError callback: " + exception.getMessage());
                onLeaveFailure(source);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("DELETE group response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onLeaveSuccess(source);
                } else {
                    LOG.warning("DELETE group returned incorrect status: " + statusCode);
                    onLeaveFailure(source);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(null, reqCallback);
        } catch (RequestException e) {
            LOG.warning("DELETE group request threw exception: " + e.getMessage());
            onLeaveFailure(source);
        }
    }

    private void onLeaveFailure(View source) {
        forwardToView(source, new AppEvent(GroupLeaveEvents.LeaveFailed));
    }

    private void onLeaveSuccess(View source) {
        forwardToView(source, new AppEvent(GroupLeaveEvents.LeaveComplete));
        Dispatcher.forwardEvent(new AppEvent(GroupLeaveEvents.LeaveComplete));
    }
}