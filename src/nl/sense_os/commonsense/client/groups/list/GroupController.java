package nl.sense_os.commonsense.client.groups.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.json.parsers.GroupParser;
import nl.sense_os.commonsense.client.common.json.parsers.UserParser;
import nl.sense_os.commonsense.client.groups.invite.InviteEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.constants.Constants;
import nl.sense_os.commonsense.shared.constants.Urls;
import nl.sense_os.commonsense.shared.models.GroupModel;
import nl.sense_os.commonsense.shared.models.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupController extends Controller {

    private static final Logger logger = Logger.getLogger("GroupController");
    private View tree;

    public GroupController() {

        registerEventTypes(GroupEvents.ShowGrid);

        // events to update the list of groups
        registerEventTypes(GroupEvents.LoadRequest, GroupEvents.ListUpdated, GroupEvents.Working,
                GroupEvents.GroupsAjaxSuccess, GroupEvents.GroupsAjaxSuccess,
                GroupEvents.GroupMembersAjaxSuccess, GroupEvents.GroupMembersAjaxFailure);

        // events to leave a group
        registerEventTypes(GroupEvents.LeaveComplete, GroupEvents.LeaveFailed,
                GroupEvents.LeaveRequested, GroupEvents.AjaxLeaveFailure,
                GroupEvents.AjaxLeaveSuccess);

        registerEventTypes(VizEvents.Show);
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(InviteEvents.InviteComplete);
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
    private void getGroupMembers(GroupModel group, AsyncCallback<List<TreeModel>> callback) {

        forwardToView(this.tree, new AppEvent(GroupEvents.Working));

        // prepare request properties
        final String method = "GET";
        final String url = Urls.GROUPS + "/" + group.getId() + "/users";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(GroupEvents.GroupMembersAjaxSuccess);
        onSuccess.setData("callback", callback);
        onSuccess.setData("group", group);
        final AppEvent onFailure = new AppEvent(GroupEvents.GroupMembersAjaxFailure);
        onFailure.setData("callback", callback);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
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
    private void getGroups(AsyncCallback<List<TreeModel>> callback) {

        forwardToView(this.tree, new AppEvent(GroupEvents.Working));
        Registry.<List<GroupModel>> get(Constants.REG_GROUPS).clear();

        // prepare request properties
        final String method = "GET";
        final String url = Urls.GROUPS;
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(GroupEvents.GroupsAjaxSuccess);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(GroupEvents.GroupsAjaxFailure);
        onFailure.setData("callback", callback);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        /*
         * Load list of groups
         */
        if (type.equals(GroupEvents.LoadRequest)) {
            // logger.fine( "LoadRequest");
            final Object loadConfig = event.getData("loadConfig");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            onLoadRequest(loadConfig, callback);

        } else if (type.equals(GroupEvents.GroupsAjaxFailure)) {
            logger.warning("GroupsAjaxFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            onGroupsFailure(callback);

        } else if (type.equals(GroupEvents.GroupsAjaxSuccess)) {
            // logger.fine( "GroupsAjaxSuccess");
            final String response = event.getData("response");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            onGroupsSuccess(response, callback);

        } else if (type.equals(GroupEvents.GroupMembersAjaxFailure)) {
            logger.warning("GroupMembersAjaxFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            onGroupMembersFailure(callback);

        } else if (type.equals(GroupEvents.GroupMembersAjaxSuccess)) {
            // logger.fine( "GroupMembersAjaxSuccess");
            final String response = event.getData("response");
            final GroupModel group = event.getData("group");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            onGroupMembersSuccess(response, group, callback);

        } else

        /*
         * Leave a group
         */
        if (type.equals(GroupEvents.LeaveRequested)) {
            // logger.fine( "LeaveRequested");
            final String groupId = event.<String> getData();
            leaveGroup(groupId);

        } else if (type.equals(GroupEvents.AjaxLeaveFailure)) {
            logger.warning("AjaxLeaveFailure");
            forwardToView(this.tree, new AppEvent(GroupEvents.LeaveFailed));

        } else if (type.equals(GroupEvents.AjaxLeaveSuccess)) {
            // logger.fine( "AjaxLeaveSuccess");
            forwardToView(this.tree, new AppEvent(GroupEvents.LeaveComplete));

        } else

        /*
         * Clear data after logout
         */
        if (type.equals(LoginEvents.LoggedOut)) {
            // logger.fine( "LoggedOut");
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
        Registry.<List<GroupModel>> get(Constants.REG_GROUPS).clear();
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.tree = new GroupGrid(this);
        Registry.register(Constants.REG_GROUPS, new ArrayList<GroupModel>());
    }

    private void leaveGroup(String groupId) {

        // prepare request property
        final String method = "DELETE";
        final String url = Urls.GROUPS + "/" + groupId;
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(GroupEvents.AjaxLeaveSuccess);
        final AppEvent onFailure = new AppEvent(GroupEvents.AjaxLeaveFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void onGroupMembersFailure(AsyncCallback<List<TreeModel>> callback) {
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
    private void onGroupMembersSuccess(String response, GroupModel group,
            AsyncCallback<List<TreeModel>> callback) {

        List<UserModel> users = UserParser.parseGroupUsers(response);
        for (UserModel user : users) {
            group.add(user);
        }

        Dispatcher.forwardEvent(GroupEvents.ListUpdated);

        if (null != callback) {
            callback.onSuccess(new ArrayList<TreeModel>(users));
        }
    }

    private void onGroupsFailure(AsyncCallback<List<TreeModel>> callback) {
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
    private void onGroupsSuccess(String response, AsyncCallback<List<TreeModel>> callback) {

        // parse the array with group details
        List<GroupModel> groups = GroupParser.parseGroups(response);

        Registry.<List<GroupModel>> get(Constants.REG_GROUPS).addAll(groups);
        Dispatcher.forwardEvent(GroupEvents.ListUpdated);

        callback.onSuccess(new ArrayList<TreeModel>(groups));
    }

    private void onLoadRequest(Object loadConfig, AsyncCallback<List<TreeModel>> callback) {

        if (null == loadConfig) {
            getGroups(callback);

        } else if (loadConfig instanceof GroupModel) {
            GroupModel group = (GroupModel) loadConfig;
            getGroupMembers(group, callback);

        } else {
            callback.onSuccess(new ArrayList<TreeModel>());
        }
    }
}
