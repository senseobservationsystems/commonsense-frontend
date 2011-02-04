package nl.sense_os.commonsense.client.controllers;

import java.util.List;

import nl.sense_os.commonsense.client.controllers.cors.MySensorsJsniRequests;
import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.events.MySensorsEvents;
import nl.sense_os.commonsense.client.services.SensorsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.MySensorsShareDialog;
import nl.sense_os.commonsense.client.views.MySensorsTree;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MySensorsController extends Controller {

    private static final String TAG = "MySensorsController";
    private MySensorsTree treeView;
    private MySensorsShareDialog shareView;
    private boolean isGettingMySensors;

    public MySensorsController() {
        registerEventTypes(MySensorsEvents.ShowTree, MySensorsEvents.ListRequested,
                MySensorsEvents.Done, MySensorsEvents.Working);
        registerEventTypes(MySensorsEvents.ShowShareDialog, MySensorsEvents.ShareRequested,
                MySensorsEvents.ShareComplete, MySensorsEvents.ShareCancelled,
                MySensorsEvents.ShareFailed);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(MainEvents.ShowVisualization);
    }

    private void getMySensors(AppEvent event) {
        final AsyncCallback<List<TreeModel>> proxyCallback = event.getData();
        if (false == this.isGettingMySensors) {
            this.isGettingMySensors = true;
            Dispatcher.forwardEvent(MySensorsEvents.Working);

            SensorsServiceAsync service = Registry
                    .<SensorsServiceAsync> get(Constants.REG_TAGS_SVC);
            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Dispatcher.forwardEvent(MySensorsEvents.Done);
                    isGettingMySensors = false;
                    proxyCallback.onFailure(caught);
                }

                @Override
                public void onSuccess(List<TreeModel> result) {
                    Registry.register(Constants.REG_MY_SENSORS, result);
                    Dispatcher.forwardEvent(MySensorsEvents.Done);
                    isGettingMySensors = false;
                    proxyCallback.onSuccess(result);
                }
            };
            service.getMySensors(sessionId, callback);
        } else {
            Log.d(TAG, "Ignored request to get my sensors: already working on an earlier request");
            proxyCallback.onFailure(null);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MySensorsEvents.ListRequested)) {
            // Log.d(TAG, "ListRequested");
            getMySensors(event);
        } else if (type.equals(MySensorsEvents.ShareRequested)) {
            // Log.d(TAG, "ShareRequested");
            shareSensor(event);
        } else if (type.equals(MySensorsEvents.ShowTree) || type.equals(MySensorsEvents.Done)
                || type.equals(MySensorsEvents.Working) || type.equals(LoginEvents.LoggedOut)
                || type.equals(MainEvents.ShowVisualization)) {
            forwardToView(this.treeView, event);
        } else if (type.equals(MySensorsEvents.ShowShareDialog)
                || type.equals(MySensorsEvents.ShareComplete)
                || type.equals(MySensorsEvents.ShareFailed)
                || type.equals(MySensorsEvents.ShareCancelled)) {
            forwardToView(this.shareView, event);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.treeView = new MySensorsTree(this);
        this.shareView = new MySensorsShareDialog(this);
    }

    private void shareSensor(AppEvent event) {
        List<TreeModel> sensors = event.<List<TreeModel>> getData("sensors");
        TreeModel user = event.<TreeModel> getData("user");
        TreeModel sensor = sensors.get(0);
        sensors.remove(0);
        String data = "{\"user\":{\"id\":\"" + user.<String> get("id") + "\"}}";
        String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/users";
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        MySensorsJsniRequests.shareSensor(url, sessionId, data, sensors, this);
    }

    public void shareSensorCallback(List<TreeModel> remainingSensors, String data) {
        Log.d(TAG, "shareSensorCallback");
        if (remainingSensors.size() > 0) {
            TreeModel sensor = remainingSensors.get(0);
            remainingSensors.remove(0);
            String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/users";
            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            MySensorsJsniRequests.shareSensor(url, sessionId, data, remainingSensors, this);
        } else {
            Dispatcher.forwardEvent(MySensorsEvents.ShareComplete);
        }
    }

    public void shareSensorErrorCallback() {
        Dispatcher.forwardEvent(MySensorsEvents.ShareFailed);
    }
}
