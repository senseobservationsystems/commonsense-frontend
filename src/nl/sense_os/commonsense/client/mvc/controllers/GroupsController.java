package nl.sense_os.commonsense.client.mvc.controllers;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.GroupsEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.views.GroupsView;
import nl.sense_os.commonsense.client.services.GroupsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

public class GroupsController extends Controller {

    private static final String TAG = "GroupsController";
    private GroupsView groupView;

    public GroupsController() {
        registerEventTypes(GroupsEvents.GroupsNotUpdated, GroupsEvents.GroupsRequested,
                GroupsEvents.ShowGroups, GroupsEvents.GroupsUpdated);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupsEvents.GroupsRequested)) {
            Log.d(TAG, "GroupsRequested");
            onGroupsRequested(event);
        }
        forwardToView(this.groupView, event);
    }

    private void onGroupsRequested(AppEvent event) {
        GroupsServiceAsync service = Registry.<GroupsServiceAsync> get(Constants.REG_GROUPS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupsEvents.GroupsNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Dispatcher.forwardEvent(GroupsEvents.GroupsUpdated, result);
            }
        };
        service.getGroups(sessionId, callback);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.groupView = new GroupsView(this);
    }
}
