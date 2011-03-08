package nl.sense_os.commonsense.client.groups;

import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.services.GroupsProxyAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;

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
    private boolean isGettingGroups;

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
        registerEventTypes(GroupEvents.AjaxCreateFailure, GroupEvents.AjaxCreateSuccess,
                GroupEvents.AjaxInviteFailure, GroupEvents.AjaxInviteSuccess,
                GroupEvents.AjaxLeaveFailure, GroupEvents.AjaxLeaveSuccess);

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

    private void getGroups(AppEvent event) {
        final AsyncCallback<List<TreeModel>> proxyCallback = event.getData();
        if (false == this.isGettingGroups) {
            this.isGettingGroups = true;
            Dispatcher.forwardEvent(GroupEvents.Working);

            GroupsProxyAsync service = Registry.<GroupsProxyAsync> get(Constants.REG_GROUPS_SVC);
            String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
            AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Dispatcher.forwardEvent(GroupEvents.ListUpdated);
                    isGettingGroups = false;
                    if (null != proxyCallback) {
                        proxyCallback.onFailure(caught);
                    }
                }

                @Override
                public void onSuccess(List<TreeModel> result) {
                    Registry.register(Constants.REG_GROUPS, result);
                    Dispatcher.forwardEvent(GroupEvents.ListUpdated, result);
                    isGettingGroups = false;
                    if (null != proxyCallback) {
                        proxyCallback.onSuccess(result);
                    }
                }
            };
            service.getGroups(sessionId, callback);
        } else {
            Log.d(TAG, "Ignored request to get groups: already working on an earlier request");
            if (null != proxyCallback) {
                proxyCallback.onFailure(null);
            }
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();
        if (type.equals(GroupEvents.ListRequest)) {
            Log.d(TAG, "ListRequest");
            getGroups(event);

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
