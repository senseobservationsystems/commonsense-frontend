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
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.views.GroupCreator;
import nl.sense_os.commonsense.client.mvc.views.GroupGrid;
import nl.sense_os.commonsense.client.mvc.views.GroupInviter;
import nl.sense_os.commonsense.client.services.GroupsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.shared.Constants;

public class GroupController extends Controller {

    private static final String TAG = "GroupController";
    private GroupGrid gridView;
    private GroupCreator creatorView;
    private GroupInviter inviterView;

    public GroupController() {
        // events to update the list of groups
        registerEventTypes(GroupEvents.ListNotUpdated, GroupEvents.ListRequested,
                GroupEvents.ListUpdated, GroupEvents.Working, GroupEvents.ShowGrid);

        // events to invite a user to a group
        registerEventTypes(GroupEvents.ShowInviter, GroupEvents.InviteCancelled,
                GroupEvents.InviteComplete, GroupEvents.InviteFailed, GroupEvents.InviteRequested);

        // events to leave a group
        registerEventTypes(GroupEvents.LeaveComplete, GroupEvents.LeaveFailed,
                GroupEvents.LeaveRequested);

        // events to create a new group
        registerEventTypes(GroupEvents.ShowCreator, GroupEvents.CreateCancelled,
                GroupEvents.CreateComplete, GroupEvents.CreateFailed, GroupEvents.CreateRequested);

        registerEventTypes(MainEvents.ShowVisualization);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            onListRequest(event);
        } else if (type.equals(GroupEvents.LeaveRequested)) {
            Log.d(TAG, "LeaveRequested");
            onLeaveRequest(event);
        } else if (type.equals(GroupEvents.CreateRequested)) {
            Log.d(TAG, "CreateRequested");
            onCreateRequest(event);
        } else if (type.equals(GroupEvents.InviteRequested)) {
            Log.d(TAG, "InviteRequested");
            onInviteRequest(event);
        } else if (type.equals(GroupEvents.ShowCreator) || type.equals(GroupEvents.CreateCancelled)
                || type.equals(GroupEvents.CreateComplete) || type.equals(GroupEvents.CreateFailed)) {
            forwardToView(this.creatorView, event);
        } else if (type.equals(GroupEvents.ShowInviter) || type.equals(GroupEvents.InviteCancelled)
                || type.equals(GroupEvents.InviteComplete) || type.equals(GroupEvents.InviteFailed)) {
            forwardToView(this.inviterView, event);
        } else {
            forwardToView(this.gridView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.gridView = new GroupGrid(this);
        this.creatorView = new GroupCreator(this);
        this.inviterView = new GroupInviter(this);
    }

    private void onCreateRequest(AppEvent event) {
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
                Dispatcher.forwardEvent(GroupEvents.CreateFailed);
            }

            @Override
            public void onSuccess(String result) {
                Dispatcher.forwardEvent(GroupEvents.CreateComplete, result);
            }
        };
        service.createGroup(sessionId, name, email, username, hashPass, callback);
    }

    private void onInviteRequest(AppEvent event) {
        GroupsServiceAsync service = Registry.<GroupsServiceAsync> get(Constants.REG_GROUPS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String groupId = event.<String> getData("groupId");
        String email = event.<String> getData("email");
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupEvents.InviteFailed);
            }

            @Override
            public void onSuccess(String result) {
                Dispatcher.forwardEvent(GroupEvents.InviteComplete, result);
            }
        };
        service.inviteUser(sessionId, groupId, email, callback);
    }

    private void onLeaveRequest(AppEvent event) {
        GroupsServiceAsync service = Registry.<GroupsServiceAsync> get(Constants.REG_GROUPS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String groupId = event.<String> getData();
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupEvents.LeaveFailed);
            }

            @Override
            public void onSuccess(String result) {
                Dispatcher.forwardEvent(GroupEvents.LeaveComplete, result);
            }
        };
        service.leaveGroup(sessionId, groupId, callback);
    }

    private void onListRequest(AppEvent event) {
        GroupsServiceAsync service = Registry.<GroupsServiceAsync> get(Constants.REG_GROUPS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupEvents.ListNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_GROUPS, result);
                Dispatcher.forwardEvent(GroupEvents.ListUpdated, result);
            }
        };
        service.getGroups(sessionId, callback);
        Dispatcher.forwardEvent(GroupEvents.Working);
    }
}
