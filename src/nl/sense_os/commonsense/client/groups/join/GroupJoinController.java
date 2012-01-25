package nl.sense_os.commonsense.client.groups.join;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.groups.list.GetGroupsResponseJso;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupJoinController extends Controller {

    private static final Logger LOG = Logger.getLogger(GroupJoinController.class.getName());
    private static final int PER_PAGE = 1000;

    public GroupJoinController() {
        LOG.setLevel(Level.ALL);
        registerEventTypes(GroupJoinEvents.Show, GroupJoinEvents.JoinRequest,
                GroupJoinEvents.PublicGroupsRequested);
    }

    private void getPublicGroups(final List<GroupModel> groups, final int page,
            final AsyncCallback<ListLoadResult<GroupModel>> callback) {

        // prepare request properties
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_GROUPS + "/all");
        urlBuilder.setParameter("per_page", "" + PER_PAGE);
        final String url = urlBuilder.buildString();
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("GET public groups onError callback: " + exception.getMessage());
                onPublicGroupsFailure(callback);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("GET public groups response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onPublicGroupsSuccess(response.getText(), groups, page, callback);
                } else {
                    LOG.warning("GET group users returned incorrect status: " + statusCode);
                    onPublicGroupsFailure(callback);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(null, reqCallback);
        } catch (RequestException e) {
            LOG.warning("GET public groups request threw exception: " + e.getMessage());
            onPublicGroupsFailure(callback);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(GroupJoinEvents.Show)) {
            View view = new GroupJoinView(this);
            forwardToView(view, event);

        } else if (type.equals(GroupJoinEvents.JoinRequest)) {
            GroupModel group = event.getData("group");
            View source = (View) event.getSource();
            join(group, source);

        } else if (type.equals(GroupJoinEvents.PublicGroupsRequested)) {
            LOG.finest("PublicGroupsRequested");
            AsyncCallback<ListLoadResult<GroupModel>> callback = event.getData("callback");
            onPublicGroupsRequest(callback);

        } else {
            LOG.warning("Unexpected event: " + event);
        }
    }

    private void join(GroupModel group, final View source) {

        // prepare request properties
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_GROUPS + "/" + group.getId() + "/users.json");
        final String url = urlBuilder.buildString();
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);

        UserModel user = Registry.<UserModel> get(Constants.REG_USER);

        JSONObject userJson = new JSONObject();
        userJson.put("id", new JSONNumber(user.getId()));
        userJson.put("username", new JSONString(user.getUsername()));
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("user", userJson);
        String body = bodyJson.toString();

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("POST group user onError callback: " + exception.getMessage());
                onJoinFailure(source);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST group user response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_CREATED == statusCode) {
                    onJoinSuccess(source);
                } else {
                    LOG.warning("POST group user returned incorrect status: " + statusCode);
                    onJoinFailure(source);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(body, reqCallback);
        } catch (RequestException e) {
            LOG.warning("POST group user request threw exception: " + e.getMessage());
            onJoinFailure(source);
        }
    }

    private void onJoinFailure(View source) {
        forwardToView(source, new AppEvent(GroupJoinEvents.JoinFailure));
    }

    private void onJoinSuccess(View source) {
        forwardToView(source, new AppEvent(GroupJoinEvents.JoinSuccess));
        Dispatcher.forwardEvent(GroupJoinEvents.JoinSuccess);
    }

    private void onPublicGroupsFailure(AsyncCallback<ListLoadResult<GroupModel>> callback) {
        callback.onFailure(new Throwable(""));
    }

    private void onPublicGroupsRequest(AsyncCallback<ListLoadResult<GroupModel>> callback) {
        List<GroupModel> groups = new ArrayList<GroupModel>();
        int page = 0;
        getPublicGroups(groups, page, callback);
    }

    private void onPublicGroupsSuccess(String response, List<GroupModel> groups, int page,
            AsyncCallback<ListLoadResult<GroupModel>> callback) {

        // parse list of groups from the response
        int total = -1;
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetGroupsResponseJso jso = JsonUtils.unsafeEval(response);
            total = jso.getGroups().size();
            groups.addAll(jso.getGroups());
        }

        // check if there are more pages left
        if (total == PER_PAGE) {
            page++;
            getPublicGroups(groups, page, callback);
        } else {
            callback.onSuccess(new BaseListLoadResult<GroupModel>(groups));
        }
    }
}
