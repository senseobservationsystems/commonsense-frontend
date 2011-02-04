package nl.sense_os.commonsense.client.controllers;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.events.GroupEvents;
import nl.sense_os.commonsense.client.events.GroupSensorsEvents;
import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.services.SensorsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.GroupSensorsTree;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupSensorsController extends Controller {

    private static final String TAG = "GroupSensorsController";
    private GroupSensorsTree treeView;
    private List<TreeModel> groupSensors;
    private int pendingRequests;

    public GroupSensorsController() {
        registerEventTypes(GroupSensorsEvents.ShowTree, GroupSensorsEvents.ListRequested,
                GroupSensorsEvents.Done, GroupSensorsEvents.Working);
        registerEventTypes(GroupEvents.Done);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupSensorsEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            getGroupSensors(event);
        } else {
            forwardToView(this.treeView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.treeView = new GroupSensorsTree(this);
        this.pendingRequests = 0;
    }

    private void getGroupSensors(AppEvent event) {
        final AsyncCallback<List<TreeModel>> proxyCallback = event.getData();
        if (this.pendingRequests == 0) {

            List<TreeModel> groups = Registry.<List<TreeModel>> get(Constants.REG_GROUPS);
            if (groups != null && groups.size() > 0) {
                this.pendingRequests = groups.size();
                Dispatcher.forwardEvent(GroupSensorsEvents.Working);
            } else {
                Dispatcher.forwardEvent(GroupSensorsEvents.Done);
                return;
            }

            this.groupSensors = new ArrayList<TreeModel>();
            SensorsServiceAsync service = Registry
                    .<SensorsServiceAsync> get(Constants.REG_TAGS_SVC);
            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            AsyncCallback<TreeModel> callback = new AsyncCallback<TreeModel>() {

                @Override
                public void onFailure(Throwable caught) {
                    pendingRequests--;
                    if (pendingRequests == 0) {
                        Dispatcher.forwardEvent(GroupSensorsEvents.Done);
                        proxyCallback.onFailure(caught);
                    }
                }

                @Override
                public void onSuccess(TreeModel groupModel) {
                    pendingRequests--;
                    groupSensors.add(groupModel);
                    if (pendingRequests == 0) {
                        Dispatcher.forwardEvent(GroupSensorsEvents.Done);
                        Registry.register(Constants.REG_GROUP_SENSORS, groupSensors);
                        proxyCallback.onSuccess(groupSensors);
                    }
                }
            };
            for (TreeModel group : groups) {
                service.getGroupSensors(sessionId, group, callback);
            }
        } else {
            Log.w(TAG, "Ignoring request to get group sensors, already working on earlier requests");
            proxyCallback.onFailure(null);
        }
    }
}
