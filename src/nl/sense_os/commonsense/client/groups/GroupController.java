package nl.sense_os.commonsense.client.groups;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.json.parsers.GroupParser;
import nl.sense_os.commonsense.client.json.parsers.UserParser;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.GroupModel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupController extends Controller {

    private static final String TAG = "GroupController";
    private View groupsTree;
    private View creator;
    private View inviter;

    public GroupController() {
        // events to update the list of groups
        registerEventTypes(GroupEvents.ListRequest, GroupEvents.ListUpdated, GroupEvents.Working,
                GroupEvents.ShowGrid);

        // events to invite a user to a group
        registerEventTypes(GroupEvents.ShowInviter, GroupEvents.InviteCancelled,
                GroupEvents.InviteComplete, GroupEvents.InviteFailed, GroupEvents.InviteRequested);

        // events to leave a group
        registerEventTypes(GroupEvents.LeaveComplete, GroupEvents.LeaveFailed,
                GroupEvents.LeaveRequested);

        // events to create a new group
        registerEventTypes(GroupEvents.ShowCreator, GroupEvents.CreateCancelled,
                GroupEvents.CreateComplete, GroupEvents.CreateFailed, GroupEvents.CreateRequested);

        // Ajax-related events
        registerEventTypes(GroupEvents.AjaxCreateFailure, GroupEvents.AjaxCreateSuccess);
        registerEventTypes(GroupEvents.AjaxInviteFailure, GroupEvents.AjaxInviteSuccess);
        registerEventTypes(GroupEvents.AjaxLeaveFailure, GroupEvents.AjaxLeaveSuccess);
        registerEventTypes(GroupEvents.AjaxGroupsSuccess, GroupEvents.AjaxGroupsSuccess);
        registerEventTypes(GroupEvents.AjaxGroupMembersSuccess, GroupEvents.AjaxGroupMembersFailure);

        registerEventTypes(VizEvents.Show);
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    private void createGroup(String name, String username, String password) {

        // prepare request properties
        final String method = "POST";
        final String url = Constants.URL_GROUPS + ".json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(GroupEvents.AjaxCreateSuccess);
        final AppEvent onFailure = new AppEvent(GroupEvents.AjaxCreateFailure);

        // prepare request body
        String body = "{\"group\":{";
        body += "\"name\":\"" + name + "\"";
        if (null != username) {
            body += ",\"username\":\"" + username + "\"";
            body += ",\"password\":\"" + Md5Hasher.hash(password) + "\"";
        }
        body += "}}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    /**
     * Gets a list of groups that the user is a member of, using an Ajax request to CommonSense. The
     * response is handled by {@link #getGroupsCallback(String, AsyncCallback)} or
     * {@link #getGroupsFailure(AsyncCallback)}. Afterwards, the members of the group are fetched by
     * {@link #getGroupMembers(int, List, AsyncCallback)}.
     * 
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of sensors is
     *            complete.
     */
    private void getGroups(AsyncCallback<List<TreeModel>> callback) {

        Dispatcher.forwardEvent(GroupEvents.Working);

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_GROUPS;
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(GroupEvents.AjaxGroupsSuccess);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(GroupEvents.AjaxGroupsFailure);
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
     * Handles the response from CommonSense to the request for groups. Parses the JSON array with
     * group details, and calls through to {@link #getGroupMembers(int, List, AsyncCallback)}.
     * 
     * @param response
     *            Response from CommonSense (JSON String).
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of groups is
     *            complete.
     */
    private void getGroupsCallback(String response, AsyncCallback<List<TreeModel>> callback) {

        // parse the array with IDs
        List<GroupModel> groups = GroupParser.parseGroups(response);

        // get the group details for each ID
        getGroupMembers(groups, 0, callback);
    }

    private void getGroupsFailure(AsyncCallback<List<TreeModel>> callback) {
        Dispatcher.forwardEvent(GroupEvents.ListUpdated);

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    /**
     * Gets the members of a group (UserModels) from CommonSense, using an Ajax request. The
     * response is handled by {@link #getGroupMembersCallback(String, List, int, AsyncCallback)} or
     * {@link #getGroupMembersFailure(List, int, AsyncCallback)}. If the members for all groups are
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
    private void getGroupMembers(List<GroupModel> groups, int count,
            AsyncCallback<List<TreeModel>> callback) {
        if (count < groups.size()) {
            GroupModel group = groups.get(count);
            String groupId = group.get(GroupModel.ID);

            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_GROUPS + "/" + groupId + "/users";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(GroupEvents.AjaxGroupMembersSuccess);
            onSuccess.setData("callback", callback);
            onSuccess.setData("groups", groups);
            onSuccess.setData("count", count);
            final AppEvent onFailure = new AppEvent(GroupEvents.AjaxGroupMembersFailure);
            onFailure.setData("callback", callback);
            onFailure.setData("groups", groups);
            onSuccess.setData("count", count);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);

            Dispatcher.forwardEvent(ajaxRequest);
        } else {
            // done getting the group members for all groups
            Registry.register(Constants.REG_GROUPS, groups);

            Dispatcher.forwardEvent(GroupEvents.ListUpdated);

            if (null != callback) {
                List<TreeModel> result = new ArrayList<TreeModel>(groups);
                callback.onSuccess(result);
            }
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
    private void getGroupMembersCallback(String response, List<GroupModel> groups, int count,
            AsyncCallback<List<TreeModel>> callback) {
        GroupModel group = groups.get(count);
        List<UserModel> users = UserParser.parseGroupUsers(response);
        for (UserModel user : users) {
            group.add(user);
        }

        count++;
        getGroupMembers(groups, count, callback);
    }

    private void getGroupMembersFailure(List<TreeModel> details, int count,
            AsyncCallback<List<TreeModel>> callback) {
        Dispatcher.forwardEvent(GroupEvents.ListUpdated);

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();
        if (type.equals(GroupEvents.ListRequest)) {
            // Log.d(TAG, "ListRequest");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData();
            getGroups(callback);

        } else if (type.equals(GroupEvents.LeaveRequested)) {
            // Log.d(TAG, "LeaveRequested");
            final String groupId = event.<String> getData();
            leaveGroup(groupId);

        } else if (type.equals(GroupEvents.CreateRequested)) {
            // Log.d(TAG, "CreateRequested");
            final String name = event.getData("name");
            final String username = event.getData("username");
            final String password = event.getData("password");
            createGroup(name, username, password);

        } else if (type.equals(GroupEvents.AjaxGroupsFailure)) {
            Log.w(TAG, "AjaxGroupsFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            getGroupsFailure(callback);

        } else if (type.equals(GroupEvents.AjaxGroupsSuccess)) {
            // Log.d(TAG, "AjaxGroupsSuccess");
            final String response = event.getData("response");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            getGroupsCallback(response, callback);

        } else if (type.equals(GroupEvents.AjaxGroupMembersFailure)) {
            Log.w(TAG, "AjaxGroupMembersFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            final List<TreeModel> details = event.getData("details");
            final int count = event.getData("count");
            getGroupMembersFailure(details, count, callback);

        } else if (type.equals(GroupEvents.AjaxGroupMembersSuccess)) {
            // Log.d(TAG, "AjaxGroupMembersSuccess");
            final String response = event.getData("response");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            final List<GroupModel> groups = event.getData("groups");
            final int count = event.getData("count");
            getGroupMembersCallback(response, groups, count, callback);

        } else if (type.equals(GroupEvents.AjaxInviteFailure)) {
            Log.w(TAG, "AjaxInviteFailure");
            forwardToView(this.inviter, new AppEvent(GroupEvents.InviteFailed));

        } else if (type.equals(GroupEvents.AjaxInviteSuccess)) {
            // Log.d(TAG, "AjaxInviteSuccess");
            forwardToView(this.inviter, new AppEvent(GroupEvents.InviteComplete));
            forwardToView(this.groupsTree, new AppEvent(GroupEvents.InviteComplete));

        } else if (type.equals(GroupEvents.AjaxLeaveFailure)) {
            Log.w(TAG, "AjaxLeaveFailure");
            forwardToView(this.groupsTree, new AppEvent(GroupEvents.LeaveFailed));

        } else if (type.equals(GroupEvents.AjaxLeaveSuccess)) {
            // Log.d(TAG, "AjaxLeaveSuccess");
            forwardToView(this.groupsTree, new AppEvent(GroupEvents.LeaveComplete));

        } else if (type.equals(GroupEvents.AjaxCreateFailure)) {
            Log.w(TAG, "AjaxCreateFailure");
            forwardToView(this.groupsTree, new AppEvent(GroupEvents.CreateFailed));

        } else if (type.equals(GroupEvents.AjaxCreateSuccess)) {
            // Log.d(TAG, "AjaxCreateSuccess");
            forwardToView(this.creator, new AppEvent(GroupEvents.CreateComplete));
            forwardToView(this.groupsTree, new AppEvent(GroupEvents.CreateComplete));

        } else if (type.equals(GroupEvents.InviteRequested)) {
            // Log.d(TAG, "InviteRequested");
            final String groupId = event.<String> getData("groupId");
            final String email = event.<String> getData("username");
            inviteUser(groupId, email);

        } else if (type.equals(GroupEvents.ShowCreator) || type.equals(GroupEvents.CreateCancelled)
                || type.equals(GroupEvents.CreateComplete) || type.equals(GroupEvents.CreateFailed)) {
            forwardToView(this.creator, event);

        } else if (type.equals(GroupEvents.ShowInviter) || type.equals(GroupEvents.InviteCancelled)
                || type.equals(GroupEvents.InviteComplete) || type.equals(GroupEvents.InviteFailed)) {
            forwardToView(this.inviter, event);

        } else {
            forwardToView(this.groupsTree, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.groupsTree = new GroupGrid(this);
        this.creator = new GroupCreator(this);
        this.inviter = new GroupInviter(this);
    }

    private void inviteUser(String groupId, String username) {

        // prepare request properties
        final String method = "POST";
        final String url = Constants.URL_GROUPS + "/" + groupId + "/users.json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(GroupEvents.AjaxInviteSuccess);
        final AppEvent onFailure = new AppEvent(GroupEvents.AjaxInviteFailure);

        // prepare request body
        String body = "{\"user\":{\"username\":\"" + username + "\"}}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void leaveGroup(String groupId) {

        // prepare request property
        final String method = "DELETE";
        final String url = Constants.URL_GROUPS + "/" + groupId;
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
}
