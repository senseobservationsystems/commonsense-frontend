package nl.sense_os.commonsense.client.groups;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.ajax.parsers.GroupParser;
import nl.sense_os.commonsense.client.ajax.parsers.UserParser;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.GroupModel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

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
        registerEventTypes(GroupEvents.AjaxGroupIdsSuccess, GroupEvents.AjaxGroupIdsSuccess);
        registerEventTypes(GroupEvents.AjaxGroupDetailsSuccess, GroupEvents.AjaxGroupDetailsFailure);
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
     * Gets details (name etc) of a group from CommonSense, using an Ajax request. The response is
     * handled by {@link #getGroupDetailsCallback(String, List, List, AsyncCallback)} or
     * {@link #getGroupDetailsFailure(List, List, AsyncCallback)}. If the details for all groups are
     * complete, the lists of group members will be requested.
     * 
     * @param ids
     *            List of group IDs that need details. The details are requested one at a time, so
     *            this method is called once for each ID in the list.
     * @param details
     *            List of group details that were received earlier.
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of sensors is
     *            complete.
     */
    private void getGroupDetails(List<ModelData> ids, List<TreeModel> details,
            AsyncCallback<List<TreeModel>> callback) {

        if (details.size() < ids.size()) {
            int index = details.size();
            String groupId = ids.get(index).get("group_id");

            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_GROUPS + "/" + groupId;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(GroupEvents.AjaxGroupDetailsSuccess);
            onSuccess.setData("callback", callback);
            onSuccess.setData("details", details);
            onSuccess.setData("ids", ids);
            final AppEvent onFailure = new AppEvent(GroupEvents.AjaxGroupDetailsFailure);
            onFailure.setData("callback", callback);
            onFailure.setData("details", details);
            onFailure.setData("ids", details);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);

            Dispatcher.forwardEvent(ajaxRequest);
        } else {
            // done getting the group details for all IDs
            getGroupMembers(0, details, callback);
        }
    }

    /**
     * Handles the response from CommonSense to the request for group details. Parses the JSON
     * object with information, and calls back to
     * {@link #getGroupDetails(List, List, AsyncCallback)} to get the details for the next group.
     * 
     * @param response
     *            response from CommonSense (JSON String)
     * @param ids
     *            List of group IDs that need details. Used to recursively call
     *            {@link #getGroupDetails(List, List, AsyncCallback)}
     * @param details
     *            List of group details that were received earlier. The new details will be added to
     *            this list.
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of groups is
     *            complete.
     */
    private void getGroupDetailsCallback(String response, List<ModelData> ids,
            List<TreeModel> details, AsyncCallback<List<TreeModel>> callback) {

        GroupModel group = GroupParser.parseGroup(response);

        if (group != null) {
            details.add(group);
            getGroupDetails(ids, details, callback);
        } else {
            // something went wrong
            getGroupDetailsFailure(ids, details, callback);
        }
    }

    private void getGroupDetailsFailure(List<ModelData> todo, List<TreeModel> details,
            AsyncCallback<List<TreeModel>> callback) {
        Dispatcher.forwardEvent(GroupEvents.ListUpdated);

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    /**
     * Gets a list of group IDs that the user is a member of, using an Ajax request to CommonSense.
     * The response is handled by {@link #getGroupIdsCallback(String, AsyncCallback)} or
     * {@link #getGroupIdsFailure(AsyncCallback)}. Afterwards, the group details can be fetched
     * using the list of IDs.
     * 
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of sensors is
     *            complete.
     */
    private void getGroupIds(AsyncCallback<List<TreeModel>> callback) {

        Dispatcher.forwardEvent(GroupEvents.Working);

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_GROUPS;
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(GroupEvents.AjaxGroupIdsSuccess);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(GroupEvents.AjaxGroupIdsFailure);
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
     * Handles the response from CommonSense to the request for group IDs. Parses the JSON array
     * with IDs, and calls through to {@link #getGroupDetails(List, List, AsyncCallback)}.
     * 
     * @param response
     *            Response from CommonSense (JSON String).
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of groups is
     *            complete.
     */
    private void getGroupIdsCallback(String response, AsyncCallback<List<TreeModel>> callback) {

        // parse the array with IDs
        List<ModelData> ids = GroupParser.parseGroupIds(response);

        // get the group details for each ID
        List<TreeModel> details = new ArrayList<TreeModel>();
        getGroupDetails(ids, details, callback);
    }

    private void getGroupIdsFailure(AsyncCallback<List<TreeModel>> callback) {
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
     * @param details
     *            List of group details that were received earlier.
     * @param callback
     *            Optional callback for a DataProxy. Will be called when the list of sensors is
     *            complete.
     */
    private void getGroupMembers(int count, List<TreeModel> details,
            AsyncCallback<List<TreeModel>> callback) {
        if (count < details.size()) {
            GroupModel group = (GroupModel) details.get(count);
            String groupId = group.get(GroupModel.KEY_ID);

            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_GROUPS + "/" + groupId + "/users";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(GroupEvents.AjaxGroupMembersSuccess);
            onSuccess.setData("callback", callback);
            onSuccess.setData("details", details);
            onSuccess.setData("count", count);
            final AppEvent onFailure = new AppEvent(GroupEvents.AjaxGroupMembersFailure);
            onFailure.setData("callback", callback);
            onFailure.setData("details", details);
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
            Registry.register(Constants.REG_GROUPS, details);

            Dispatcher.forwardEvent(GroupEvents.ListUpdated);

            if (null != callback) {
                callback.onSuccess(details);
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
    private void getGroupMembersCallback(String response, List<TreeModel> details, int count,
            AsyncCallback<List<TreeModel>> callback) {
        TreeModel group = details.get(count);
        List<UserModel> users = UserParser.parseGroupUsers(response);
        for (UserModel user : users) {
            group.add(user);
        }

        count++;
        getGroupMembers(count, details, callback);
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
            getGroupIds(callback);

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

        } else if (type.equals(GroupEvents.AjaxGroupIdsFailure)) {
            Log.w(TAG, "AjaxGroupIdsFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            getGroupIdsFailure(callback);

        } else if (type.equals(GroupEvents.AjaxGroupIdsSuccess)) {
            // Log.d(TAG, "AjaxGroupIdsSuccess");
            final String response = event.getData("response");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            getGroupIdsCallback(response, callback);

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
            final List<TreeModel> details = event.getData("details");
            final int count = event.getData("count");
            getGroupMembersCallback(response, details, count, callback);

        } else if (type.equals(GroupEvents.AjaxGroupDetailsFailure)) {
            Log.w(TAG, "AjaxGroupDetailsFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            final List<TreeModel> details = event.getData("details");
            final List<ModelData> ids = event.getData("ids");
            getGroupDetailsFailure(ids, details, callback);

        } else if (type.equals(GroupEvents.AjaxGroupDetailsSuccess)) {
            // Log.d(TAG, "AjaxGroupDetailsSuccess");
            final String response = event.getData("response");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            final List<TreeModel> details = event.getData("details");
            final List<ModelData> ids = event.getData("ids");
            getGroupDetailsCallback(response, ids, details, callback);

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
