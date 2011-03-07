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
            // Log.d(TAG, "ListRequest");
            final AsyncCallback<List<TreeModel>> callback = event.getData();
            getGroupSensors(callback);

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

        if (this.isGettingList == false) {

            List<TreeModel> groups = Registry.<List<TreeModel>> get(Constants.REG_GROUPS);
            if (groups == null) {
                forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Working));
                Dispatcher.forwardEvent(GroupEvents.ListRequested);
                return;
            } else {
                forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Working));
                this.isGettingList = true;
            }

            forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Working));
            SensorsProxyAsync service = Registry.<SensorsProxyAsync> get(Constants.REG_TAGS_SVC);
            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

                @Override
                public void onFailure(Throwable caught) {
                    isGettingList = false;
                    Dispatcher.forwardEvent(GroupSensorsEvents.Done);
                    if (null != proxyCallback) {
                        proxyCallback.onFailure(caught);
                    }
                }

                @Override
                public void onSuccess(List<TreeModel> sharedSensors) {
                    isGettingList = false;
                    Dispatcher.forwardEvent(GroupSensorsEvents.Done);
                    Registry.register(Constants.REG_GROUP_SENSORS, sharedSensors);
                    if (null != proxyCallback) {
                        proxyCallback.onSuccess(sharedSensors);
                    }
                }
            };
            service.getSharedSensors(sessionId, groups, callback);

        } else {
            Log.w(TAG, "Ignoring request to get group sensors, already working on earlier requests");
            if (null != proxyCallback) {
                proxyCallback.onFailure(null);
            }
        }
    }
}
