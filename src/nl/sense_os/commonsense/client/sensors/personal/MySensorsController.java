package nl.sense_os.commonsense.client.sensors.personal;

import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.services.SensorsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MySensorsController extends Controller {

    private static final String TAG = "MySensorsController";
    private View treeView;
    private View shareView;
    private boolean isGettingMySensors;

    public MySensorsController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.Show);

        // local event types
        registerEventTypes(MySensorsEvents.ShowTree, MySensorsEvents.ListRequested,
                MySensorsEvents.Done, MySensorsEvents.Working);
        registerEventTypes(MySensorsEvents.ShowShareDialog, MySensorsEvents.ShareRequested,
                MySensorsEvents.ShareComplete, MySensorsEvents.ShareCancelled,
                MySensorsEvents.ShareFailed);

        // ajax event types
        registerEventTypes(MySensorsEvents.AjaxShareFailure, MySensorsEvents.AjaxShareSuccess);
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
            Log.d(TAG, "ListRequested");
            getMySensors(event);

        } else if (type.equals(MySensorsEvents.ShareRequested)) {
            // Log.d(TAG, "ShareRequested");
            shareSensor(event);

        } else if (type.equals(MySensorsEvents.AjaxShareFailure)) {
            // Log.d(TAG, "AjaxShareFailure");
            // final int code = event.getData("code");
            shareSensorErrorCallback(event);

        } else if (type.equals(MySensorsEvents.AjaxShareSuccess)) {
            // Log.d(TAG, "AjaxShareSuccess");
            // final String response = event.<String> getData("response");
            shareSensorCallback(event);

        } else if (type.equals(MySensorsEvents.ShowTree) || type.equals(MySensorsEvents.Done)
                || type.equals(MySensorsEvents.Working) || type.equals(LoginEvents.LoggedOut)
                || type.equals(VizEvents.Show) || type.equals(MainEvents.Init)) {
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

    /**
     * Does request to share a list of sensors with a user. If there are multiple sensors in the
     * list, this method calls itself for each sensor in the list.
     * 
     * @param event
     *            AppEvent with "sensors" and "user" properties
     */
    private void shareSensor(AppEvent event) {
        List<TreeModel> sensors = event.<List<TreeModel>> getData("sensors");

        if (null != sensors && sensors.size() > 0) {
            // get first sensor from the list
            TreeModel user = event.<TreeModel> getData("user");
            TreeModel sensor = sensors.get(0);
            sensors.remove(0);

            // prepare request properties
            final String method = "POST";
            final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id")
                    + "/users.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final String body = "{\"user\":{\"id\":\"" + user.<String> get("id") + "\"}}";
            final AppEvent onSuccess = new AppEvent(MySensorsEvents.AjaxShareSuccess);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("user", user);
            final AppEvent onFailure = new AppEvent(MySensorsEvents.AjaxShareFailure);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("body", body);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);
            Dispatcher.forwardEvent(ajaxRequest);
        } else {
            Dispatcher.forwardEvent(MySensorsEvents.ShareComplete);
        }
    }

    public void shareSensorCallback(AppEvent event) {
        Log.d(TAG, "shareSensorCallback");
        shareSensor(event);
    }

    public void shareSensorErrorCallback(AppEvent event) {
        Dispatcher.forwardEvent(MySensorsEvents.ShareFailed);
    }
}
