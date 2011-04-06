package nl.sense_os.commonsense.client.sensors.personal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.ajax.parsers.SensorParser;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.SensorsEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.DeviceModel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
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
        registerEventTypes(MySensorsEvents.AjaxSensorsFailure, MySensorsEvents.AjaxSensorsSuccess);
        registerEventTypes(MySensorsEvents.AjaxDevicesFailure, MySensorsEvents.AjaxDevicesSuccess);
    }

    private void getDevices(List<TreeModel> categories) {

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=1000&physical=1&owned=1";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(MySensorsEvents.AjaxDevicesSuccess);
        onSuccess.setData("categories", categories);
        final AppEvent onFailure = new AppEvent(MySensorsEvents.AjaxDevicesFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void getDevicesCallback(String response, List<TreeModel> categories) {

        ArrayList<SensorModel> sensors = new ArrayList<SensorModel>();
        int total = SensorParser.parseSensors(response, sensors);

        if (total > 1000) {
            Log.w(TAG, "Not all sensors were fetched!");
        }

        // sort sensors per device
        Map<String, DeviceModel> devices = new HashMap<String, DeviceModel>();
        for (SensorModel sensor : sensors) {

            // get the device TreeModel, or create a new one
            String deviceKey = sensor.<String> get(SensorModel.DEVICE_ID)
                    + sensor.<String> get(SensorModel.DEVICE_DEVTYPE);

            DeviceModel device = devices.get(deviceKey);
            if (device == null) {
                device = new DeviceModel();
                device.set(DeviceModel.KEY_ID, deviceKey);
                device.set(DeviceModel.KEY_UUID, sensor.<String> get(SensorModel.DEVICE_ID));
                device.set(DeviceModel.KEY_TYPE, sensor.<String> get(SensorModel.DEVICE_DEVTYPE));

                // front end-only properties
                device.set("tagType", TagModel.TYPE_DEVICE);
                if (device.get(DeviceModel.KEY_TYPE).equals("myrianode")) {
                    device.set(
                            "text",
                            device.get(DeviceModel.KEY_TYPE) + " "
                                    + device.get(DeviceModel.KEY_UUID));
                } else {
                    device.set("text", device.get(DeviceModel.KEY_TYPE));
                }
            }

            // add the sensor to the device
            device.add(new SensorModel(sensor.getProperties()));
            devices.put(deviceKey, device);
        }

        // remove the content of the category if it is already present
        for (TreeModel cat : categories) {
            if (cat.<String> get("text").equalsIgnoreCase("devices")) {
                cat.removeAll();
                for (DeviceModel device : devices.values()) {
                    cat.add(device);
                }
                break;
            }
        }

        getMySensorsCallback.onSuccess(categories);
    }

    private void getDevicesFailure() {
        if (null != getMySensorsCallback) {
            getMySensorsCallback.onFailure(null);
        }
    }

    private void getMySensors(final AsyncCallback<List<TreeModel>> proxyCallback) {

        if (null == this.getMySensorsCallback) {
            this.getMySensorsCallback = proxyCallback;
        }

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "/?per_page=1000&owned=1";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(MySensorsEvents.AjaxSensorsSuccess);
        final AppEvent onFailure = new AppEvent(MySensorsEvents.AjaxSensorsFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);

        //
        // if (false == this.isGettingMySensors) {
        // this.isGettingMySensors = true;
        // forwardToView(treeView, new AppEvent(MySensorsEvents.Working));
        //
        // SensorsProxyAsync service = Registry
        // .<SensorsProxyAsync> get(Constants.REG_SENSORS_PROXY);
        // String sessionId = Registry.get(Constants.REG_SESSION_ID);
        // AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {
        //
        // @Override
        // public void onFailure(Throwable caught) {
        //
        // forwardToView(treeView, new AppEvent(MySensorsEvents.Done));
        // Dispatcher.forwardEvent(MySensorsEvents.ListUpdated);
        //
        // if (null != getMySensorsCallback) {
        // getMySensorsCallback.onFailure(caught);
        // }
        // isGettingMySensors = false;
        // }
        //
        // @Override
        // public void onSuccess(List<TreeModel> result) {
        // Registry.register(Constants.REG_MY_SENSORS, result);
        //
        // forwardToView(treeView, new AppEvent(MySensorsEvents.Done));
        // Dispatcher.forwardEvent(MySensorsEvents.ListUpdated);
        //
        // if (null != getMySensorsCallback) {
        // getMySensorsCallback.onSuccess(result);
        // }
        // isGettingMySensors = false;
        // }
        // };
        // service.getMySensors(sessionId, callback);
        // } else {
        // Log.d(TAG, "Ignored request to get my sensors: already working on an earlier request");
        // }
    }

    private void getMySensorsCallback(String response) {
        // parse response
        ArrayList<SensorModel> unsortedSensors = new ArrayList<SensorModel>();
        int total = SensorParser.parseSensors(response, unsortedSensors);

        if (total > 1000) {
            Log.w(TAG, "Not all sensors were fetched!");
        }

        List<SensorModel> feedsSensors = new ArrayList<SensorModel>();
        List<SensorModel> deviceSensors = new ArrayList<SensorModel>();
        List<SensorModel> stateSensors = new ArrayList<SensorModel>();
        List<SensorModel> environmentSensors = new ArrayList<SensorModel>();
        List<SensorModel> apps = new ArrayList<SensorModel>();
        List<TreeModel> sorted = sortSensors(unsortedSensors, deviceSensors, environmentSensors,
                apps, feedsSensors, stateSensors);

        // get devices and use them as special category
        getDevices(sorted);
    }

    private void getMySensorsFailure() {
        if (null != getMySensorsCallback) {
            getMySensorsCallback.onFailure(null);
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
            Log.w(TAG, "AjaxShareFailure");
            // final int code = event.getData("code");
            shareSensorErrorCallback(event);

        } else if (type.equals(MySensorsEvents.AjaxShareSuccess)) {
            // Log.d(TAG, "AjaxShareSuccess");
            // final String response = event.<String> getData("response");
            shareSensorCallback(event);

        } else if (type.equals(MySensorsEvents.AjaxSensorsFailure)) {
            Log.w(TAG, "AjaxSensorsFailure");
            // final int code = event.getData("code");
            getMySensorsFailure();

        } else if (type.equals(MySensorsEvents.AjaxSensorsSuccess)) {
            // Log.d(TAG, "AjaxSensorsSuccess");
            final String response = event.<String> getData("response");
            getMySensorsCallback(response);

        } else if (type.equals(MySensorsEvents.AjaxDevicesFailure)) {
            Log.w(TAG, "AjaxDevicesFailure");
            // final int code = event.getData("code");
            getDevicesFailure();

        } else if (type.equals(MySensorsEvents.AjaxDevicesSuccess)) {
            Log.d(TAG, "AjaxDevicesSuccess");
            final String response = event.<String> getData("response");
            final List<TreeModel> categories = event.<List<TreeModel>> getData("categories");
            getDevicesCallback(response, categories);

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

    private void shareSensorCallback(AppEvent event) {
        final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
        final String username = event.<String> getData("user");
        sensors.remove(0);
        shareSensors(sensors, username, 0);
    }

    private void shareSensorErrorCallback(AppEvent event) {
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

    private List<TreeModel> sortSensors(List<SensorModel> unsorted, List<SensorModel> devices,
            List<SensorModel> environments, List<SensorModel> apps, List<SensorModel> feeds,
            List<SensorModel> states) {

        // convert the sensor models into TreeModels
        for (SensorModel sensorModel : unsorted) {
            SensorModel sensor = new SensorModel(sensorModel.getProperties());
            int type = Integer.parseInt(sensor.<String> get(SensorModel.TYPE));
            switch (type) {
                case 0 :
                    feeds.add(sensor);
                    break;
                case 1 :
                    devices.add(sensor);
                    break;
                case 2 :
                    states.add(sensor);
                    break;
                case 3 :
                    environments.add(sensor);
                    break;
                case 4 :
                    apps.add(sensor);
                    break;
                default :
                    Log.w(TAG, "Unexpected sensor type: " + type);
            }
        }

        // create main groups
        TreeModel feedCat = new BaseTreeModel();
        feedCat.set("text", "Public Feeds");
        feedCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : feeds) {
            feedCat.add(child);
        }
        TreeModel deviceCat = new BaseTreeModel();
        deviceCat.set("text", "Devices");
        deviceCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : devices) {
            deviceCat.add(child);
        }
        TreeModel stateCat = new BaseTreeModel();
        stateCat.set("text", "State Sensors");
        stateCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : states) {
            stateCat.add(child);
        }
        TreeModel environmentCat = new BaseTreeModel();
        environmentCat.set("text", "Environments");
        environmentCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : environments) {
            environmentCat.add(child);
        }
        TreeModel appCat = new BaseTreeModel();
        appCat.set("text", "Online Activity");
        appCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : apps) {
            appCat.add(child);
        }

        List<TreeModel> sorted = new ArrayList<TreeModel>();
        if (feedCat.getChildCount() > 0) {
            sorted.add(feedCat);
        }
        if (deviceCat.getChildCount() > 0) {
            sorted.add(deviceCat);
        }
        if (stateCat.getChildCount() > 0) {
            sorted.add(stateCat);
        }
        if (environmentCat.getChildCount() > 0) {
            sorted.add(environmentCat);
        }
        if (appCat.getChildCount() > 0) {
            sorted.add(appCat);
        }

        return sorted;
    }
}
