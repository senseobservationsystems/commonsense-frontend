package nl.sense_os.commonsense.client.groups.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.NewGroupModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.groups.create.GroupCreateEvents;
import nl.sense_os.commonsense.client.groups.invite.InviteEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Registry;
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
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupController extends Controller {

    private static final Logger LOG = Logger.getLogger(GroupController.class.getName());
    private View tree;

    public GroupController() {

        registerEventTypes(GroupEvents.ShowGrid);

        // events to update the list of groups
        registerEventTypes(GroupEvents.LoadRequest, GroupEvents.ListUpdated, GroupEvents.Working);

        // events to leave a group
        registerEventTypes(GroupEvents.LeaveComplete, GroupEvents.LeaveFailed,
                GroupEvents.LeaveRequested);

        registerEventTypes(VizEvents.Show);
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(InviteEvents.InviteComplete);
        registerEventTypes(GroupCreateEvents.CreateComplete);
    }

    /**
     * Gets the members of a group (UserModels) from CommonSense, using an Ajax request. The
     * response is handled by {@link #onGroupMembersSuccess(String, List, int, AsyncCallback)} or
     * {@link #onGroupMembersFailure(List, int, AsyncCallback)}. If the members for all groups are
     * complete, the requests are finished and {@link GroupEvents#ListUpdated} will be dispatched.
     * 
     * @param count
     *            Count for the number of groups that already have members. The details are
     *            requested one at a time, so this method is called once for each group.
     * @param groups
     *            List of group details that were received earlier.
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of sensors is
     *            complete.
     */
    private void getGroupMembers(final NewGroupModel group,
            final AsyncCallback<List<UserModel>> callback) {

        forwardToView(this.tree, new AppEvent(GroupEvents.Working));

        // prepare request properties
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_GROUPS + "/" + group.getId() + "/users.json");
        final String url = urlBuilder.buildString();
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("GET group users onError callback: " + exception.getMessage());
                onGroupMembersFailure(callback);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("GET group users response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onGroupMembersSuccess(response.getText(), group, callback);
                } else {
                    LOG.warning("GET group users returned incorrect status: " + statusCode);
                    onGroupMembersFailure(callback);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(null, reqCallback);
        } catch (RequestException e) {
            LOG.warning("GET group users request threw exception: " + e.getMessage());
            onGroupMembersFailure(callback);
        }
    }

    /**
     * Gets a list of groups that the user is a member of, using an Ajax request to CommonSense. The
     * response is handled by {@link #onGroupsSuccess(String, AsyncCallback)} or
     * {@link #onGroupsFailure(AsyncCallback)}. Afterwards, the members of the group are fetched by
     * {@link #getGroupMembers(int, List, AsyncCallback)}.
     * 
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of sensors is
     *            complete.
     */
    private void getGroups(final AsyncCallback<List<UserModel>> callback) {

        forwardToView(this.tree, new AppEvent(GroupEvents.Working));
        Registry.<List<NewGroupModel>> get(Constants.REG_GROUPS).clear();

        // prepare request properties
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_GROUPS + ".json");
        final String url = urlBuilder.buildString();
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("GET groups onError callback: " + exception.getMessage());
                onGroupsFailure(callback);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("GET groups response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onGroupsSuccess(response.getText(), callback);
                } else {
                    LOG.warning("GET groups returned incorrect status: " + statusCode);
                    onGroupsFailure(callback);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(null, reqCallback);
        } catch (RequestException e) {
            LOG.warning("GET groups request threw exception: " + e.getMessage());
            onGroupsFailure(callback);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        /*
         * Load list of groups
         */
        if (type.equals(GroupEvents.LoadRequest)) {
            // LOG.fine( "LoadRequest");
            final Object loadConfig = event.getData("loadConfig");
            final AsyncCallback<List<UserModel>> callback = event
                    .<AsyncCallback<List<UserModel>>> getData("callback");
            onLoadRequest(loadConfig, callback);

        } else

        /*
         * Leave a group
         */
        if (type.equals(GroupEvents.LeaveRequested)) {
            // LOG.fine( "LeaveRequested");
            final int groupId = event.getData();
            leaveGroup(groupId);

        } else

        /*
         * Clear data after logout
         */
        if (type.equals(LoginEvents.LoggedOut)) {
            // LOG.fine( "LoggedOut");
            onLogout();

        } else

        /*
         * Pass through to view
         */
        {
            forwardToView(this.tree, event);
        }
    }

    /**
     * Clears the list of groups from the Registry.
     */
    private void onLogout() {
        Registry.<List<NewGroupModel>> get(Constants.REG_GROUPS).clear();
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.tree = new GroupGrid(this);
        Registry.register(Constants.REG_GROUPS, new ArrayList<NewGroupModel>());
    }

    private void leaveGroup(int groupId) {

        // prepare request property
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_GROUPS + "/" + groupId + ".json");
        final String url = urlBuilder.buildString();
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("DELETE group onError callback: " + exception.getMessage());
                onLeaveFailure();
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("DELETE group response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onLeaveSuccess();
                } else {
                    LOG.warning("DELETE group returned incorrect status: " + statusCode);
                    onLeaveFailure();
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
            onLeaveFailure();
        }
    }

    private void onLeaveFailure() {
        forwardToView(tree, new AppEvent(GroupEvents.LeaveFailed));
    }

    private void onLeaveSuccess() {
        Dispatcher.forwardEvent(new AppEvent(GroupEvents.LeaveComplete));
    }

    private void onGroupMembersFailure(AsyncCallback<List<UserModel>> callback) {
        Dispatcher.forwardEvent(GroupEvents.ListUpdated);

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    /**
     * Handles the response from CommonSense to the request for group members. Parses the JSON array
     * with user information, and calls back to {@link #getGroupMembers(int, List, AsyncCallback)}
     * to get the members for the next group.
     * 
     * @param response
     *            Response from CommonSense (JSON String).
     * @param details
     *            List of group details that were received earlier. The new details will be added to
     *            this list.
     * @param count
     *            Count for the number of groups that already have members.
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of groups is
     *            complete.
     */
    private void onGroupMembersSuccess(String response, NewGroupModel group,
            AsyncCallback<List<UserModel>> callback) {

        // parse list of users from the response
        List<UserModel> users = new ArrayList<UserModel>();
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetGroupUsersResponseJso jso = JsonUtils.unsafeEval(response);
            users = jso.getUsers();
        }

        // add users to the group
        for (UserModel user : users) {
            group.add(user);
        }

        Dispatcher.forwardEvent(GroupEvents.ListUpdated);

        if (null != callback) {
            callback.onSuccess(new ArrayList<UserModel>(users));
        }
    }

    private void onGroupsFailure(AsyncCallback<List<UserModel>> callback) {
        Dispatcher.forwardEvent(GroupEvents.ListUpdated);

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    /**
     * Handles the response from CommonSense to the request for groups. Parses the JSON array with
     * group details, and calls through to {@link #getGroupMembers(int, List, AsyncCallback)}.
     * 
     * @param response
     *            Response from CommonSense (JSON String).
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of groups is
     *            complete.
     */
    private void onGroupsSuccess(String response, AsyncCallback<List<UserModel>> callback) {

        // parse list of groups from the response
        List<NewGroupModel> groups = new ArrayList<NewGroupModel>();
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetGroupsResponseJso jso = JsonUtils.unsafeEval(response);
            groups = jso.getGroups();
        }

        Registry.<List<NewGroupModel>> get(Constants.REG_GROUPS).addAll(groups);
        Dispatcher.forwardEvent(GroupEvents.ListUpdated);

        callback.onSuccess(new ArrayList<UserModel>(groups));
    }

    private void onLoadRequest(Object loadConfig, AsyncCallback<List<UserModel>> callback) {

        if (null == loadConfig) {
            getGroups(callback);

        } else if (loadConfig instanceof NewGroupModel) {
            NewGroupModel group = (NewGroupModel) loadConfig;
            getGroupMembers(group, callback);

        } else {
            callback.onSuccess(new ArrayList<UserModel>());
        }
    }
}
