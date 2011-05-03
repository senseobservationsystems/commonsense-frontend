package nl.sense_os.commonsense.client.sensors.personal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.json.parsers.SensorParser;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.DeviceModel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ListLoadResult;
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
    private static final int PER_PAGE = 1000;
    private View deleteDialog;
    private View tree;
    private View grid;
    private View shareDialog;

    public MySensorsController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.Show);
        registerEventTypes(MySensorsEvents.ShowTree);

        // get list events
        registerEventTypes(MySensorsEvents.ListRequested, MySensorsEvents.ListAjaxSuccess,
                MySensorsEvents.ListAjaxFailure, MySensorsEvents.ListPhysicalAjaxSuccess,
                MySensorsEvents.ListPhysicalAjaxFailure);
        registerEventTypes(MySensorsEvents.TreeRequested, MySensorsEvents.AjaxSensorsFailure,
                MySensorsEvents.AjaxSensorsSuccess, MySensorsEvents.AjaxDevicesFailure,
                MySensorsEvents.AjaxDevicesSuccess);

        // share events
        registerEventTypes(MySensorsEvents.ShowShareDialog, MySensorsEvents.ShareRequest,
                MySensorsEvents.ShareComplete, MySensorsEvents.ShareCancelled,
                MySensorsEvents.ShareFailed, MySensorsEvents.AjaxShareFailure,
                MySensorsEvents.AjaxShareSuccess);

        // delete events
        registerEventTypes(MySensorsEvents.ShowDeleteDialog, MySensorsEvents.AjaxDeleteSuccess,
                MySensorsEvents.AjaxDeleteFailure, MySensorsEvents.DeleteRequest);
    }

    /**
     * Deletes a list of sensors, using Ajax requests to CommonSense.
     * 
     * @param sensors
     *            The list of sensors that have to be deleted.
     * @param retryCount
     *            Counter for failed requests that were retried.
     */
    private void delete(List<SensorModel> sensors, int retryCount) {

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
            forwardToView(this.deleteDialog, new AppEvent(MySensorsEvents.DeleteSuccess));
            forwardToView(this.tree, new AppEvent(MySensorsEvents.DeleteSuccess));
            forwardToView(this.grid, new AppEvent(MySensorsEvents.DeleteSuccess));
        }
    }

    /**
     * Handles a successful delete request. Removes the deleted sensor from the list, and calls back
     * to {@link #delete(List, int)}.
     * 
     * @param sensors
     *            List of sensors that have to be deleted.
     */
    private void deleteCallback(List<SensorModel> sensors) {
        // Goodbye sensor!
        sensors.remove(0);

        // continue with the rest of the list
        delete(sensors, 0);
    }

    /**
     * Handles a failed delete request. Retries the request up to three times, after this it gives
     * up and dispatches {@link SensorsEvents#DeleteFailure}.
     * 
     * @param sensors
     *            List of sensors that have to be deleted.
     * @param retryCount
     *            Number of times this request was attempted.
     */
    private void deleteFailure(List<SensorModel> sensors, int retryCount) {

        if (retryCount < 3) {
            retryCount++;
            delete(sensors, retryCount);
        } else {
            forwardToView(this.deleteDialog, new AppEvent(MySensorsEvents.DeleteFailure));
        }
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
        final String url = Constants.URL_SENSORS + "?per_page=1000&physical=1&owned=1";
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

    private void getList(List<SensorModel> sensors, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        forwardToView(this.grid, new AppEvent(MySensorsEvents.Working));

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=" + PER_PAGE + "&page=" + page
                + "&owned=1";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(MySensorsEvents.ListAjaxSuccess);
        onSuccess.setData("sensors", sensors);
        onSuccess.setData("page", page);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(MySensorsEvents.ListAjaxFailure);
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

    private void getListCallback(String response, List<SensorModel> sensors, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        int total = SensorParser.parseSensors(response, sensors);
        if (total > sensors.size()) {
            page++;
            getList(sensors, page, callback);
        } else {
            List<SensorModel> physical = new ArrayList<SensorModel>();
            getListPhysical(sensors, physical, 0, callback);
        }
    }

    private void getListFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void getListPhysical(List<SensorModel> sensors, List<SensorModel> physical, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        forwardToView(this.grid, new AppEvent(MySensorsEvents.Working));

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=" + PER_PAGE + "&page=" + page
                + "&physical=1&owned=1";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(MySensorsEvents.ListPhysicalAjaxSuccess);
        onSuccess.setData("sensors", sensors);
        onSuccess.setData("physical", physical);
        onSuccess.setData("page", page);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(MySensorsEvents.ListPhysicalAjaxFailure);
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

    private void getListPhysicalCallback(String response, List<SensorModel> sensors,
            List<SensorModel> physical, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // parse physical sensors
        int total = SensorParser.parseSensors(response, physical);

        if (total > physical.size()) {
            page++;
            getListPhysical(sensors, physical, page, callback);

        } else {

            List<SensorModel> complete = new ArrayList<SensorModel>(physical);
            for (SensorModel sensor : sensors) {
                if (!sensor.getType().equals("1")) {
                    complete.add(sensor);
                }
            }

            // callback
            if (null != callback) {
                callback.onSuccess(new BaseListLoadResult<SensorModel>(complete));
            }
            Registry.register(Constants.REG_MY_SENSORS_LIST, complete);
            forwardToView(grid, new AppEvent(MySensorsEvents.Done));
            Dispatcher.forwardEvent(MySensorsEvents.ListUpdated);
        }
    }

    private void getListPhysicalFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
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
        if (type.equals(MySensorsEvents.ListRequested)) {
            // Log.d(TAG, "ListRequested");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData();
            final List<SensorModel> sensors = new ArrayList<SensorModel>();
            getList(sensors, 0, callback);

        } else if (type.equals(MySensorsEvents.ListAjaxSuccess)) {
            // Log.d(TAG, "ListAjaxSuccess");
            final String response = event.getData("response");
            final List<SensorModel> sensors = event.getData("sensors");
            final int page = event.getData("page");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData("callback");
            getListCallback(response, sensors, page, callback);

        } else if (type.equals(MySensorsEvents.ListAjaxFailure)) {
            Log.w(TAG, "ListAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData();
            getListFailure(callback);

        } else if (type.equals(MySensorsEvents.ListPhysicalAjaxSuccess)) {
            // Log.d(TAG, "ListPhysicalAjaxSuccess");
            final String response = event.getData("response");
            final List<SensorModel> sensors = event.getData("sensors");
            final List<SensorModel> physical = event.getData("physical");
            final int page = event.getData("page");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData("callback");
            getListPhysicalCallback(response, sensors, physical, page, callback);

        } else if (type.equals(MySensorsEvents.ListPhysicalAjaxFailure)) {
            Log.w(TAG, "ListPhysicalAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData();
            getListPhysicalFailure(callback);

        } else

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
         * Share request
         */
        if (type.equals(MySensorsEvents.ShowShareDialog)) {
            // Log.d(TAG, "ShowShareDialog");
            forwardToView(this.shareDialog, event);

        } else if (type.equals(MySensorsEvents.ShareRequest)) {
            // Log.d(TAG, "ShareRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final String user = event.<String> getData("user");
            shareSensors(sensors, user, 0);

        } else if (type.equals(MySensorsEvents.AjaxShareSuccess)) {
            // Log.d(TAG, "AjaxShareSuccess");
            // final String response = event.<String> getData("response");
            shareSensorCallback(event);

        } else if (type.equals(MySensorsEvents.AjaxShareFailure)) {
            Log.w(TAG, "AjaxShareFailure");
            // final int code = event.getData("code");
            shareSensorErrorCallback(event);

        } else

        /**
         * Delete request
         */
        if (type.equals(MySensorsEvents.ShowDeleteDialog)) {
            // Log.d(TAG, "ShowDeleteDialog");
            forwardToView(this.deleteDialog, event);

        } else if (type.equals(MySensorsEvents.DeleteRequest)) {
            // Log.d(TAG, "DeleteRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            delete(sensors, 0);

        } else if (type.equals(MySensorsEvents.AjaxDeleteSuccess)) {
            // Log.d(TAG, "AjaxDeleteSuccess");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            deleteCallback(sensors);

        } else if (type.equals(MySensorsEvents.AjaxDeleteFailure)) {
            Log.w(TAG, "AjaxDeleteFailure");
            // final int code = event.getData("code");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int retryCount = event.<Integer> getData("retry");
            deleteFailure(sensors, retryCount);

        } else

        /**
         * Pass the rest on to the main view
         */
        {
            forwardToView(this.tree, event);
            forwardToView(this.grid, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.deleteDialog = new DeleteDialog(this);
        this.tree = new MySensorsTree(this);
        this.grid = new MySensorsGrid(this);
        this.shareDialog = new ShareDialog(this);
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
            forwardToView(shareDialog, new AppEvent(MySensorsEvents.ShareFailed));
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
            forwardToView(this.shareDialog, new AppEvent(MySensorsEvents.ShareComplete));
        }
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
