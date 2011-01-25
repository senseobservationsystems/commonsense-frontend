package nl.sense_os.commonsense.client.mvc.controllers;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.GroupEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.views.GroupCreatorView;
import nl.sense_os.commonsense.client.mvc.views.GroupGridView;
import nl.sense_os.commonsense.client.mvc.views.GroupInviterView;
import nl.sense_os.commonsense.client.services.GroupsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.shared.Constants;

public class GroupController extends Controller {

    private static final String TAG = "GroupController";
    private GroupGridView gridView;
    private GroupCreatorView creatorView;
    private GroupInviterView inviterView;

    public GroupController() {
        // events to update the list of groups
        registerEventTypes(GroupEvents.GroupListNotUpdated, GroupEvents.GroupListRequested,
                GroupEvents.GroupListUpdated, GroupEvents.GroupsBusy, GroupEvents.ShowGroups);
        
        // events to invite a user to a group
        registerEventTypes(GroupEvents.ShowInvitation, GroupEvents.InviteUserCancelled,
                GroupEvents.InviteUserComplete, GroupEvents.InviteUserFailed,
                GroupEvents.InviteUserRequested);
        
        // events to leave a group
        registerEventTypes(GroupEvents.LeaveGroupComplete, GroupEvents.LeaveGroupFailed,
                GroupEvents.LeaveGroupRequested);
        
        // events to create a new group
        registerEventTypes(GroupEvents.ShowGroupCreator, GroupEvents.CreateGroupCancelled,
                GroupEvents.CreateGroupComplete, GroupEvents.CreateGroupFailed,
                GroupEvents.CreateGroupRequested);
        
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupEvents.GroupListRequested)) {
            Log.d(TAG, "GroupsRequested");
            onGroupsRequest(event);
        } else if (type.equals(GroupEvents.LeaveGroupRequested)) {
            Log.d(TAG, "LeaveGroupRequested");
            onLeaveGroupRequest(event);
        } else if (type.equals(GroupEvents.CreateGroupRequested)) {
            Log.d(TAG, "CreateGroupRequested");
            onCreateGroupRequest(event);
        } else if (type.equals(GroupEvents.InviteUserRequested)) {
            Log.d(TAG, "InviteUserRequested");
            onInviteRequest(event);
        } else if (type.equals(GroupEvents.ShowGroupCreator)
                || type.equals(GroupEvents.CreateGroupCancelled)
                || type.equals(GroupEvents.CreateGroupComplete)
                || type.equals(GroupEvents.CreateGroupFailed)) {
            forwardToView(this.creatorView, event);
        }  else if (type.equals(GroupEvents.ShowInvitation)
                || type.equals(GroupEvents.InviteUserCancelled)
                || type.equals(GroupEvents.InviteUserComplete)
                || type.equals(GroupEvents.InviteUserFailed)) {
            forwardToView(this.inviterView, event);
        } else {
            forwardToView(this.gridView, event);
        }
    }

    private void onInviteRequest(AppEvent event) {
        GroupsServiceAsync service = Registry.<GroupsServiceAsync> get(Constants.REG_GROUPS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String groupId = event.<String> getData("groupId");
        String email = event.<String> getData("email");
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupEvents.InviteUserFailed);
            }

            @Override
            public void onSuccess(String result) {
                Dispatcher.forwardEvent(GroupEvents.InviteUserComplete, result);
            }
        };
        service.inviteUser(sessionId, groupId, email, callback);
    }

    private void onLeaveGroupRequest(AppEvent event) {
        GroupsServiceAsync service = Registry.<GroupsServiceAsync> get(Constants.REG_GROUPS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String groupId = event.<String> getData();
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupEvents.LeaveGroupFailed);
            }

            @Override
            public void onSuccess(String result) {
                Dispatcher.forwardEvent(GroupEvents.LeaveGroupComplete, result);
            }
        };
        service.leaveGroup(sessionId, groupId, callback);
    }

    private void onCreateGroupRequest(AppEvent event) {
        GroupsServiceAsync service = Registry.<GroupsServiceAsync> get(Constants.REG_GROUPS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String name = event.getData("name");
        String email = event.getData("email");
        String username = event.getData("username");
        String password = event.getData("password");
        String hashPass = null;
        if (null != password) {
            hashPass = Md5Hasher.hash(password);
        }
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupEvents.CreateGroupFailed);
            }

            @Override
            public void onSuccess(String result) {
                Dispatcher.forwardEvent(GroupEvents.CreateGroupComplete, result);
            }
        };
        service.createGroup(sessionId, name, email, username, hashPass, callback);
    }

    private void onGroupsRequest(AppEvent event) {
        GroupsServiceAsync service = Registry.<GroupsServiceAsync> get(Constants.REG_GROUPS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupEvents.GroupListNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_GROUPS, result);
                Dispatcher.forwardEvent(GroupEvents.GroupListUpdated, result);
            }
        };
        service.getGroups(sessionId, callback);
        Dispatcher.forwardEvent(GroupEvents.GroupsBusy);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.gridView = new GroupGridView(this);
        this.creatorView = new GroupCreatorView(this);
        this.inviterView = new GroupInviterView(this);
    }
}
