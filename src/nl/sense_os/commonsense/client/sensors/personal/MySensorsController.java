package nl.sense_os.commonsense.client.sensors.personal;

import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.SensorsEvents;
import nl.sense_os.commonsense.client.services.SensorsProxyAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

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
    private AsyncCallback<List<TreeModel>> getMySensorsCallback;

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
        registerEventTypes(SensorsEvents.DeleteSuccess, SensorsEvents.DeleteFailure);

        // ajax event types
        registerEventTypes(MySensorsEvents.AjaxShareFailure, MySensorsEvents.AjaxShareSuccess);
    }

    private void getMySensors(final AsyncCallback<List<TreeModel>> proxyCallback) {

        if (null == this.getMySensorsCallback) {
            this.getMySensorsCallback = proxyCallback;
        }
        
        if (false == this.isGettingMySensors) {
            this.isGettingMySensors = true;
            forwardToView(treeView, new AppEvent(MySensorsEvents.Working));

            SensorsProxyAsync service = Registry.<SensorsProxyAsync> get(Constants.REG_SENSORS_PROXY);
            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

                @Override
                public void onFailure(Throwable caught) {

                    forwardToView(treeView, new AppEvent(MySensorsEvents.Done));
                    Dispatcher.forwardEvent(MySensorsEvents.ListUpdated);

                    if (null != getMySensorsCallback) {
                        getMySensorsCallback.onFailure(caught);
                    }
                    isGettingMySensors = false;
                }

                @Override
                public void onSuccess(List<TreeModel> result) {
                    Registry.register(Constants.REG_MY_SENSORS, result);

                    forwardToView(treeView, new AppEvent(MySensorsEvents.Done));
                    Dispatcher.forwardEvent(MySensorsEvents.ListUpdated);

                    if (null != getMySensorsCallback) {
                        getMySensorsCallback.onSuccess(result);
                    }
                    isGettingMySensors = false;
                }
            };
            service.getMySensors(sessionId, callback);
        } else {
            Log.d(TAG, "Ignored request to get my sensors: already working on an earlier request");
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MySensorsEvents.ListRequested)) {
            // Log.d(TAG, "ListRequested");
            final AsyncCallback<List<TreeModel>> callback = event.getData();
            getMySensors(callback);

        } else if (type.equals(MySensorsEvents.ShareRequested)) {
            // Log.d(TAG, "ShareRequested");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final String user = event.<String> getData("user");
            shareSensors(sensors, user, 0);

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

        } else if (type.equals(SensorsEvents.DeleteSuccess)
                || type.equals(SensorsEvents.DeleteFailure)) {
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
        this.shareView = new ShareDialog(this);
    }

    /**
     * Does request to share a list of sensors with a user. If there are multiple sensors in the
     * list, this method calls itself for each sensor in the list.
     * 
     * @param event
     *            AppEvent with "sensors" and "user" properties
     */
    private void shareSensors(List<SensorModel> sensors, String username, int retryCount) {

        if (null != sensors && sensors.size() > 0) {
            // get first sensor from the list
            SensorModel sensor = sensors.get(0);

            // prepare request properties
            final String method = "POST";
            final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id")
                    + "/users.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final String body = "{\"user\":{\"username\":\"" + username + "\"}}";
            final AppEvent onSuccess = new AppEvent(MySensorsEvents.AjaxShareSuccess);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("user", username);
            final AppEvent onFailure = new AppEvent(MySensorsEvents.AjaxShareFailure);
            onFailure.setData("sensors", sensors);
            onFailure.setData("user", username);
            onFailure.setData("retry", retryCount);

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
        final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
        final String username = event.<String> getData("user");
        sensors.remove(0);
        shareSensors(sensors, username, 0);
    }

    public void shareSensorErrorCallback(AppEvent event) {
        final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
        final String username = event.<String> getData("user");
        int retryCount = event.<Integer> getData("retry");

        if (retryCount < 3) {
            retryCount++;
            shareSensors(sensors, username, retryCount);
        } else {
            Dispatcher.forwardEvent(MySensorsEvents.ShareFailed);
        }
    }
}
