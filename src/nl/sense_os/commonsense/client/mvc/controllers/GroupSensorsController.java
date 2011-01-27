package nl.sense_os.commonsense.client.mvc.controllers;

import nl.sense_os.commonsense.client.mvc.events.GroupEvents;
import nl.sense_os.commonsense.client.mvc.events.GroupSensorsEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.views.GroupSensorsTree;
import nl.sense_os.commonsense.client.services.TagsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

public class GroupSensorsController extends Controller {

    private static final String TAG = "GroupSensorsController";
    private GroupSensorsTree treeView;
    private List<TreeModel> groupSensors;

    public GroupSensorsController() {
        registerEventTypes(GroupSensorsEvents.Working, GroupSensorsEvents.ListNotUpdated,
                GroupSensorsEvents.ListRequested, GroupSensorsEvents.ListUpdated,
                GroupSensorsEvents.ShowTree);
        registerEventTypes(GroupEvents.ListUpdated);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupSensorsEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            onListRequest(event);
        } else if (type.equals(GroupEvents.ListUpdated)) {
            Log.d(TAG, "GroupListUpdated");
            onListRequest(event);
        } else {
            forwardToView(this.treeView, event);
        }
    }

    private void onListRequest(AppEvent event) {
        List<TreeModel> groups = null;
        this.groupSensors = new ArrayList<TreeModel>();
        if (event.getType().equals(GroupEvents.ListUpdated)) {
            groups = event.getData();
        } else {
            groups = Registry.<List<TreeModel>> get(Constants.REG_GROUPS);
        }
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        AsyncCallback<TreeModel> callback = new AsyncCallback<TreeModel>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupSensorsEvents.ListNotUpdated, caught);
            }

            @Override
            public void onSuccess(TreeModel groupModel) {                
                groupSensors.add(groupModel);
                Registry.register(Constants.REG_GROUP_SENSORS, groupSensors);
                Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated, groupSensors);
            }
        };
        if (groups != null && groups.size() > 0) {
            Dispatcher.forwardEvent(GroupSensorsEvents.Working);
            for (TreeModel group : groups) {
                service.getGroupSensors(sessionId, group, callback);
            }
        } else {
            Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.treeView = new GroupSensorsTree(this);
    }
}
