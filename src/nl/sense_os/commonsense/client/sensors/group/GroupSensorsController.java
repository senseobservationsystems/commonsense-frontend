package nl.sense_os.commonsense.client.sensors.group;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.groups.GroupEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.services.SensorsProxyAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupSensorsController extends Controller {

    private static final String TAG = "GroupSensorsController";
    private View treeView;
    private List<TreeModel> groupSensors;
    private int pendingRequests;

    public GroupSensorsController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(GroupSensorsEvents.ShowTree, GroupSensorsEvents.ListRequested,
                GroupSensorsEvents.Done, GroupSensorsEvents.Working);
        registerEventTypes(GroupEvents.ListUpdated);
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

            forwardToView(treeView, new AppEvent(GroupSensorsEvents.Working));

            List<TreeModel> groups = Registry.<List<TreeModel>> get(Constants.REG_GROUPS);
            if (groups == null) {
                forwardToView(treeView, new AppEvent(GroupSensorsEvents.Working));
                Dispatcher.forwardEvent(GroupEvents.ListRequested);
                return;
            } else if (groups.size() > 0) {
                forwardToView(treeView, new AppEvent(GroupSensorsEvents.Working));
                this.pendingRequests = groups.size();
            } else {
                forwardToView(treeView, new AppEvent(GroupSensorsEvents.Done));
                return;
            }

            this.groupSensors = new ArrayList<TreeModel>();
            SensorsProxyAsync service = Registry.<SensorsProxyAsync> get(Constants.REG_TAGS_SVC);
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
