package nl.sense_os.commonsense.client.sensors.personal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.json.parsers.SensorParser;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.client.sensors.share.SensorShareEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.tabs.VizEvents;
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

@Deprecated
public class MySensorsController extends Controller {

    private static final String TAG = "MySensorsController";
    private static final int PER_PAGE = 1000;
    private View tree;

    public MySensorsController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.Show);
        registerEventTypes(MySensorsEvents.ShowTree);

        // get list events
        registerEventTypes(MySensorsEvents.TreeRequested, MySensorsEvents.AjaxSensorsFailure,
                MySensorsEvents.AjaxSensorsSuccess, MySensorsEvents.AjaxDevicesFailure,
                MySensorsEvents.AjaxDevicesSuccess);

        registerEventTypes(SensorDeleteEvents.DeleteSuccess, SensorDeleteEvents.DeleteFailure);
        registerEventTypes(SensorShareEvents.ShareComplete, SensorShareEvents.ShareFailed,
                SensorShareEvents.ShareCancelled);
    }

    /**
     * Gets list of physical sensors that are owned by the user, using Ajax request to CommonSense.
     * The Ajax request returns to {@link #getDevicesCallback(String, List, AsyncCallback)} or
     * {@link #getDevicesFailure(AsyncCallback)}. The list will be put in the appropriate category.
     * 
     * @param categories
     *            List with all sensors that the user owns, sorted into categories. The "devices"
     *            category will be replaced.
     * @param callback
     *            optional callback for a DataProxy, will be called when the list of sensors is
     *            complete.
     */
    private void getDevices(List<TreeModel> categories, AsyncCallback<List<TreeModel>> callback) {

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=" + PER_PAGE + "&physical=1&owned=1";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(MySensorsEvents.AjaxDevicesSuccess);
        onSuccess.setData("categories", categories);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(MySensorsEvents.AjaxDevicesFailure);
        onFailure.setData("callback", callback);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    /**
     * Handles response from CommonSense, containing a list of physical sensors. Parses the JSON,
     * sorts the sensors in different devices, and puts these devices in the list of categorized
     * sensors.
     * 
     * @param response
     *            response from CommonSense (JSON String)
     * @param categories
     *            List with all sensors that the user owns, sorted into categories. The "devices"
     *            category will be replaced.
     * @param callback
     *            optional callback for a DataProxy, will be called when the list of sensors is
     *            complete.
     */
    private void getDevicesCallback(String response, List<TreeModel> categories,
            AsyncCallback<List<TreeModel>> callback) {

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

        // done getting my sensors
        Registry.register(Constants.REG_MY_SENSORS_TREE, categories);

        forwardToView(tree, new AppEvent(MySensorsEvents.Done));
        Dispatcher.forwardEvent(MySensorsEvents.TreeUpdated);

        if (null != callback) {
            callback.onSuccess(categories);
        }
    }

    /**
     * Handles a failed Ajax request for a list of physical sensors. Dispatched message that the
     * list was updated, and calls the optional callback.
     * 
     * @param callback
     *            optional callback for a DataProxy, will be called to notify the proxy of failure.
     */
    private void getDevicesFailure(AsyncCallback<List<TreeModel>> callback) {

        forwardToView(tree, new AppEvent(MySensorsEvents.Done));
        Dispatcher.forwardEvent(MySensorsEvents.TreeUpdated);

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    /**
     * Gets list of sensors that are owned by the user, using Ajax request to CommonSense. The Ajax
     * request returns to {@link #getSensorsCallback(String, AsyncCallback)} or
     * {@link #getSensorsFailure(AsyncCallback)}.
     * 
     * @param callback
     *            optional callback for a DataProxy, will be called when the list of sensors is
     *            complete.
     */
    private void getSensors(final AsyncCallback<List<TreeModel>> callback) {

        forwardToView(this.tree, new AppEvent(MySensorsEvents.Working));

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=1000&owned=1";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(MySensorsEvents.AjaxSensorsSuccess);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(MySensorsEvents.AjaxSensorsFailure);
        onFailure.setData("callback", callback);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    /**
     * Handles response from CommonSense, containing a list of sensors. Parses the JSON, sorts the
     * sensors in different categories, and finally calls through to
     * {@link #getDevices(List, AsyncCallback)}.
     * 
     * @param response
     *            response from CommonSense (JSON String)
     * @param callback
     *            optional callback for a DataProxy, will be called when the list of sensors is
     *            complete.
     */
    private void getSensorsCallback(String response, AsyncCallback<List<TreeModel>> callback) {
        // parse response
        ArrayList<SensorModel> unsortedSensors = new ArrayList<SensorModel>();
        int total = SensorParser.parseSensors(response, unsortedSensors);

        if (total > 1000) {
            Log.w(TAG, "Not all sensors were fetched!");
        }

        List<TreeModel> sorted = sortSensors(unsortedSensors);

        // get devices and use them as special category
        getDevices(sorted, callback);
    }

    /**
     * Handles a failed Ajax request for a list of sensors. Dispatched message that the list was
     * updated, and calls the optional callback.
     * 
     * @param callback
     *            optional callback for a DataProxy, will be called to notify the proxy of failure.
     */
    private void getSensorsFailure(AsyncCallback<List<TreeModel>> callback) {

        forwardToView(this.tree, new AppEvent(MySensorsEvents.Done));
        Dispatcher.forwardEvent(MySensorsEvents.TreeUpdated);

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        /**
         * Request for sensors tree
         */
        if (type.equals(MySensorsEvents.TreeRequested)) {
            // Log.d(TAG, "TreeRequested");
            final AsyncCallback<List<TreeModel>> callback = event.getData();
            getSensors(callback);

        } else if (type.equals(MySensorsEvents.AjaxSensorsSuccess)) {
            // Log.d(TAG, "AjaxUnownedSuccess");
            final String response = event.<String> getData("response");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            getSensorsCallback(response, callback);

        } else if (type.equals(MySensorsEvents.AjaxSensorsFailure)) {
            Log.w(TAG, "AjaxUnownedFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            getSensorsFailure(callback);

        } else if (type.equals(MySensorsEvents.AjaxDevicesSuccess)) {
            // Log.d(TAG, "AjaxDevicesSuccess");
            final String response = event.<String> getData("response");
            final List<TreeModel> categories = event.<List<TreeModel>> getData("categories");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            getDevicesCallback(response, categories, callback);

        } else if (type.equals(MySensorsEvents.AjaxDevicesFailure)) {
            Log.w(TAG, "AjaxDevicesFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<TreeModel>> callback = event.getData("callback");
            getDevicesFailure(callback);

        } else

        /**
         * Pass the rest on to the main view
         */
        {
            forwardToView(this.tree, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.tree = new MySensorsTree(this);
    }

    /**
     * Categorizes an unsorted list of sensors into categories (Feeds, Devices, States, ...) for
     * display in a TreePanel.
     * 
     * @param unsorted
     *            list of sensors to be sorted
     * @return a list of TreeModels, containing categories, with the sensors nested in them.
     */
    private List<TreeModel> sortSensors(List<SensorModel> unsorted) {

        List<SensorModel> feeds = new ArrayList<SensorModel>();
        List<SensorModel> devices = new ArrayList<SensorModel>();
        List<SensorModel> states = new ArrayList<SensorModel>();
        List<SensorModel> environments = new ArrayList<SensorModel>();
        List<SensorModel> apps = new ArrayList<SensorModel>();

        // convert the sensor models into TreeModels
        for (SensorModel sensorModel : unsorted) {
            SensorModel sensor = new SensorModel(sensorModel.getProperties());
            int type = Integer.parseInt(sensor.<String> get(SensorModel.TYPE));
            switch (type) {
            case 0:
                feeds.add(sensor);
                break;
            case 1:
                devices.add(sensor);
                break;
            case 2:
                states.add(sensor);
                break;
            case 3:
                environments.add(sensor);
                break;
            case 4:
                apps.add(sensor);
                break;
            default:
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
