package nl.sense_os.commonsense.client.sensors.personal;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.services.SensorsProxyAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
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
        registerEventTypes(MySensorsEvents.ShowShareDialog, MySensorsEvents.ShowTriggersDialog,
                MySensorsEvents.ShareRequested, MySensorsEvents.ShareComplete,
                MySensorsEvents.ShareCancelled, MySensorsEvents.ShareFailed);

        // ajax event types
        registerEventTypes(MySensorsEvents.AjaxShareFailure, MySensorsEvents.AjaxShareSuccess);
        registerEventTypes(MySensorsEvents.AjaxDeleteFailure, MySensorsEvents.AjaxDeleteSuccess);
    }

    @SuppressWarnings("unused")
    private void cleanupFreeksMess() {
        Log.e(TAG, "Deleting Freek's MyriaNed sensors...");

        List<TreeModel> mySensors = Registry.<List<TreeModel>> get(Constants.REG_MY_SENSORS);

        // get the devices category
        TreeModel deviceCategory = null;
        for (TreeModel category : mySensors) {
            if (category.get("text").equals("Devices")) {
                deviceCategory = category;
                break;
            }
        }
        if (deviceCategory == null) {
            Log.e(TAG, "Could not find device category");
            return;
        }

        // get a list of MyriaNed nodes that are polluting the list of devices
        List<TreeModel> myrianodes = new ArrayList<TreeModel>();
        for (ModelData device : deviceCategory.getChildren()) {
            if (device.<String> get("text").contains("myrianode")) {
                myrianodes.add((TreeModel) device);
            }
        }
        if (myrianodes.size() == 0) {
            Log.e(TAG, "No devices called \'myrianode\'");
            return;
        }

        // KILL! KILL! KILL!
        for (TreeModel myrianode : myrianodes) {
            deleteSensors(myrianode.getChildren(), 0);
        }
    }

    private void deleteSensors(List<ModelData> sensors, int retryCount) {

        if (null != sensors && sensors.size() > 0) {
            ModelData sensor = sensors.get(0);

            // prepare request properties
            final String method = "DELETE";
            final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id");
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(MySensorsEvents.AjaxDeleteSuccess);
            onSuccess.setData("sensors", sensors);
            final AppEvent onFailure = new AppEvent(MySensorsEvents.AjaxDeleteFailure);
            onFailure.setData("sensors", sensors);
            onFailure.setData("retry", retryCount);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);
            Dispatcher.forwardEvent(ajaxRequest);
        } else {
            // Dispatcher.forwardEvent(MySensorsEvents.DeleteComplete);
        }
    }

    private void deleteSensorsCallback(AppEvent event) {
        // Goodbye sensor!
        List<ModelData> sensors = event.<List<ModelData>> getData("sensors");
        sensors.remove(0);

        // continue with the rest of the list
        deleteSensors(sensors, 0);
    }

    private void deletesSensorErrorCallback(AppEvent event) {

        List<ModelData> sensors = event.<List<ModelData>> getData("sensors");
        int retryCount = event.<Integer> getData("retry");
        if (retryCount < 3) {
            retryCount++;
            deleteSensors(sensors, retryCount);
        } else {
            // Dispatcher.forwardEvent(MySensorsEvents.DeleteFailure);
        }
    }

    private void getMySensors(final AsyncCallback<List<TreeModel>> proxyCallback) {

        if (false == this.isGettingMySensors) {
            this.isGettingMySensors = true;
            Dispatcher.forwardEvent(MySensorsEvents.Working);

            SensorsProxyAsync service = Registry.<SensorsProxyAsync> get(Constants.REG_TAGS_SVC);
            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Dispatcher.forwardEvent(MySensorsEvents.Done);
                    isGettingMySensors = false;
                    if (null != proxyCallback) {
                        proxyCallback.onFailure(caught);
                    }
                }

                @Override
                public void onSuccess(List<TreeModel> result) {
                    Registry.register(Constants.REG_MY_SENSORS, result);
                    Dispatcher.forwardEvent(MySensorsEvents.Done);
                    isGettingMySensors = false;
                    if (null != proxyCallback) {
                        proxyCallback.onSuccess(result);
                    }
                }
            };
            service.getMySensors(sessionId, callback);
        } else {
            Log.d(TAG, "Ignored request to get my sensors: already working on an earlier request");
            if (null != proxyCallback) {
                proxyCallback.onFailure(null);
            }
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
            final List<TreeModel> sensors = event.<List<TreeModel>> getData("sensors");
            final String user = event.<String> getData("user");
            shareSensor(sensors, user, 0);

        } else if (type.equals(MySensorsEvents.AjaxDeleteFailure)) {
            Log.w(TAG, "AjaxDeleteFailure");
            // final int code = event.getData("code");
            deletesSensorErrorCallback(event);

        } else if (type.equals(MySensorsEvents.AjaxDeleteSuccess)) {
            // Log.d(TAG, "AjaxDeleteSuccess");
            // final String response = event.<String> getData("response");
            deleteSensorsCallback(event);

        } else if (type.equals(MySensorsEvents.AjaxShareFailure)) {
            // Log.d(TAG, "AjaxShareFailure");
            // final int code = event.getData("code");
            shareSensorErrorCallback(event);

        } else if (type.equals(MySensorsEvents.AjaxShareSuccess)) {
            // Log.d(TAG, "AjaxShareSuccess");
            // final String response = event.<String> getData("response");
            shareSensorCallback(event);

        } else if (type.equals(MySensorsEvents.ShowTriggersDialog)) {
            // Log.d(TAG, "AjaxShareSuccess");
            // final String response = event.<String> getData("response");
            Log.w(TAG, "ShowTriggersDialog is not implemented");

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
    private void shareSensor(List<TreeModel> sensors, String username, int retryCount) {

        if (null != sensors && sensors.size() > 0) {
            // get first sensor from the list
            TreeModel sensor = sensors.get(0);

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
        final List<TreeModel> sensors = event.<List<TreeModel>> getData("sensors");
        final String username = event.<String> getData("user");
        sensors.remove(0);
        shareSensor(sensors, username, 0);
    }

    public void shareSensorErrorCallback(AppEvent event) {
        final List<TreeModel> sensors = event.<List<TreeModel>> getData("sensors");
        final String username = event.<String> getData("user");
        int retryCount = event.<Integer> getData("retry");

        if (retryCount < 3) {
            retryCount++;
            shareSensor(sensors, username, retryCount);
        } else {
            Dispatcher.forwardEvent(MySensorsEvents.ShareFailed);
        }
    }
}
