package nl.sense_os.commonsense.client.mvc.controllers;

import java.util.List;

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

public class GroupSensorsController extends Controller {

    private static final String TAG = "GroupSensorsController";
    private GroupSensorsTree treeView;

    public GroupSensorsController() {
        registerEventTypes(GroupSensorsEvents.Working, GroupSensorsEvents.ListNotUpdated,
                GroupSensorsEvents.ListRequested, GroupSensorsEvents.ListUpdated,
                GroupSensorsEvents.ShowTree);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupSensorsEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            onListRequest(event);
        } else {
            forwardToView(this.treeView, event);
        }
    }

    private void onListRequest(AppEvent event) {
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(GroupSensorsEvents.ListNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_GROUP_SENSORS, result);
                Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated, result);
            }
        };
        service.getGroupSensors(sessionId, callback);
        Dispatcher.forwardEvent(GroupSensorsEvents.Working);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.treeView = new GroupSensorsTree(this);
    }
}
