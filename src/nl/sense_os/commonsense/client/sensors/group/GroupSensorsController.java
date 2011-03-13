package nl.sense_os.commonsense.client.sensors.group;

import java.util.List;

import nl.sense_os.commonsense.client.groups.GroupEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.SensorsEvents;
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
    private View sensorsTree;
    private boolean isGettingList;
    private AsyncCallback<List<TreeModel>> getGroupSensorsCallback;

    public GroupSensorsController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(GroupSensorsEvents.ShowTree);
        registerEventTypes(GroupEvents.ListUpdated);
        registerEventTypes(SensorsEvents.DeleteSuccess, SensorsEvents.DeleteFailure);

        // local event types
        registerEventTypes(GroupSensorsEvents.ListRequest, GroupSensorsEvents.Done,
                GroupSensorsEvents.Working);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(GroupSensorsEvents.ListRequest)) {
            Log.d(TAG, "ListRequest");
            final AsyncCallback<List<TreeModel>> callback = event.getData();
            getGroupSensors(callback);

        } else if (isGettingList && type.equals(GroupEvents.ListUpdated)) {
            Log.d(TAG, "Group ListUpdated");
            isGettingList = false;
            getGroupSensors(null);

        } else {
            forwardToView(this.sensorsTree, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.sensorsTree = new GroupSensorsTree(this);
    }

    private void getGroupSensors(final AsyncCallback<List<TreeModel>> proxyCallback) {
        
        // keep track of the callback to the view
        if (null == getGroupSensorsCallback) {
            getGroupSensorsCallback = proxyCallback;
        }
        
        if (false == this.isGettingList) {
            this.isGettingList = true;

            forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Working));

            final List<TreeModel> groups = Registry.<List<TreeModel>> get(Constants.REG_GROUPS);
            if (groups == null) {
                Dispatcher.forwardEvent(GroupEvents.ListRequest);
                return;
            }

            forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Working));
            SensorsProxyAsync service = Registry.<SensorsProxyAsync> get(Constants.REG_SENSORS_PROXY);
            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

                @Override
                public void onFailure(Throwable caught) {
                    if (null != getGroupSensorsCallback) {
                        getGroupSensorsCallback.onFailure(caught);
                    }
                    forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Done));
                    isGettingList = false;
                }

                @Override
                public void onSuccess(List<TreeModel> sharedSensors) {
                    Registry.register(Constants.REG_GROUP_SENSORS, sharedSensors);
                    if (null != getGroupSensorsCallback) {
                         getGroupSensorsCallback.onSuccess(sharedSensors);
                    }

                    forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Done));
                    Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated);
                    isGettingList = false;
                }
            };
            service.getSharedSensors(sessionId, groups, callback);

        } else {
            Log.w(TAG, "Ignoring request to get group sensors, already working on earlier requests");
        }
    }
}
