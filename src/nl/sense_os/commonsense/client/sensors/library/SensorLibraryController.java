package nl.sense_os.commonsense.client.sensors.library;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.json.parsers.GroupParser;
import nl.sense_os.commonsense.client.common.json.parsers.SensorParser;
import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.client.env.list.EnvEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.client.sensors.share.SensorShareEvents;
import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.client.states.defaults.StateDefaultsEvents;
import nl.sense_os.commonsense.client.states.list.StateListEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.DeviceModel;
import nl.sense_os.commonsense.shared.EnvironmentModel;
import nl.sense_os.commonsense.shared.GroupModel;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SensorLibraryController extends Controller {

    private final static String TAG = "SensorGridController";
    private static final int PER_PAGE = 1000;
    private View grid;

    public SensorLibraryController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.Show);

        registerEventTypes(SensorLibraryEvents.ShowLibrary, SensorLibraryEvents.ListRequested,
                SensorLibraryEvents.ListUpdated, SensorLibraryEvents.FullDetailsAjaxSuccess,
                SensorLibraryEvents.FullDetailsAjaxFailure);
        registerEventTypes(SensorLibraryEvents.AjaxGroupsSuccess,
                SensorLibraryEvents.AjaxGroupsFailure);
        registerEventTypes(SensorLibraryEvents.GroupSensorsAjaxSuccess,
                SensorLibraryEvents.GroupSensorsAjaxFailure);

        // external events
        registerEventTypes(SensorDeleteEvents.DeleteSuccess, SensorDeleteEvents.DeleteFailure);
        registerEventTypes(SensorShareEvents.ShareComplete, SensorShareEvents.ShareFailed,
                SensorShareEvents.ShareCancelled);
        registerEventTypes(StateCreateEvents.CreateServiceComplete, StateListEvents.RemoveComplete,
                StateDefaultsEvents.CheckDefaultsSuccess);
        registerEventTypes(EnvCreateEvents.CreateSuccess, EnvEvents.DeleteSuccess);
    }

    private void getFullDetails(List<SensorModel> sensors, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=" + PER_PAGE + "&page=" + page
                + "&details=full";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(SensorLibraryEvents.FullDetailsAjaxSuccess);
        onSuccess.setData("sensors", sensors);
        onSuccess.setData("page", page);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(SensorLibraryEvents.FullDetailsAjaxFailure);
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

    private void getGroups(List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // try to get the list from the Registry
        final List<GroupModel> groups = Registry.<List<GroupModel>> get(Constants.REG_GROUPS);
        if (null == groups) {
            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_GROUPS + "?per_page=" + PER_PAGE;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(SensorLibraryEvents.AjaxGroupsSuccess);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(SensorLibraryEvents.AjaxGroupsFailure);
            onFailure.setData("callback", callback);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);

            Dispatcher.forwardEvent(ajaxRequest);

        } else {
            // create a copy of the list in the Registry to avoid messing with the structure
            List<GroupModel> copy = new ArrayList<GroupModel>();
            for (GroupModel group : groups) {
                copy.add(new GroupModel(group.getProperties()));
            }
            getGroupSensors(copy, 0, sensors, callback);
        }
    }

    private void getGroupSensors(List<GroupModel> groups, int index, List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        if (index < groups.size()) {

            String groupId = groups.get(index).getId();

            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_SENSORS + "?per_page=1000&details=full&alias="
                    + groupId;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(SensorLibraryEvents.GroupSensorsAjaxSuccess);
            onSuccess.setData("groups", groups);
            onSuccess.setData("index", index);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(SensorLibraryEvents.GroupSensorsAjaxFailure);
            onFailure.setData("callback", callback);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);

            Dispatcher.forwardEvent(ajaxRequest);

        } else {
            // hooray we're done!
            onListComplete(sensors, callback);
        }
    }

    private void getList(List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        forwardToView(this.grid, new AppEvent(SensorLibraryEvents.Working));

        getFullDetails(sensors, 0, callback);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorLibraryEvents.ListRequested)) {
            // Log.d(TAG, "LoadRequest");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData();
            final List<SensorModel> sensors = new ArrayList<SensorModel>();
            getList(sensors, callback);

        } else if (type.equals(SensorLibraryEvents.FullDetailsAjaxSuccess)) {
            // Log.d(TAG, "FullDetailsAjaxSuccess");
            final String response = event.getData("response");
            final List<SensorModel> sensors = event.getData("sensors");
            final int page = event.getData("page");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData("callback");
            onFullDetailsSuccess(response, sensors, page, callback);

        } else if (type.equals(SensorLibraryEvents.FullDetailsAjaxFailure)) {
            Log.w(TAG, "FullDetailsAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData();
            onFullDetailsFailure(callback);

        } else

        /*
         * Group sensors
         */
        if (type.equals(SensorLibraryEvents.AjaxGroupsSuccess)) {
            // Log.d(TAG, "AjaxGroupsSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupsSuccess(response, sensors, callback);

        } else if (type.equals(SensorLibraryEvents.AjaxGroupsFailure)) {
            Log.w(TAG, "AjaxGroupsFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupsFailure(callback);

        } else if (type.equals(SensorLibraryEvents.GroupSensorsAjaxSuccess)) {
            // Log.d(TAG, "GroupSensorsAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<GroupModel> groups = event.<List<GroupModel>> getData("groups");
            final int index = event.getData("index");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupSensorsSuccess(response, groups, index, sensors, callback);

        } else if (type.equals(SensorLibraryEvents.GroupSensorsAjaxFailure)) {
            Log.w(TAG, "GroupSensorsAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupSensorsFailure(callback);

        } else

        /*
         * Pass through to view
         */
        {
            forwardToView(this.grid, event);
        }

    }

    @Override
    protected void initialize() {
        super.initialize();
        this.grid = new SensorLibrary(this);
    }

    private void onFullDetailsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        onListFailure(callback);
    }

    private void onFullDetailsSuccess(String response, List<SensorModel> sensors, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        int total = SensorParser.parseSensors(response, sensors);
        if (total > sensors.size()) {
            // get the next page with sensors
            page++;
            getFullDetails(sensors, page, callback);

        } else {
            // get the group IDs to get the group sensors
            getGroups(sensors, callback);
        }
    }

    private void onGroupSensorsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        onListFailure(callback);
    }

    private void onGroupSensorsSuccess(String response, List<GroupModel> groups, int index,
            List<SensorModel> sensors, AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // parse group sensors
        List<SensorModel> groupSensors = new ArrayList<SensorModel>();
        SensorParser.parseSensors(response, groupSensors);

        GroupModel group = groups.get(index);
        for (SensorModel groupSensor : groupSensors) {
            if (!sensors.contains(groupSensor)) {
                // set "alias" property
                groupSensor.set("alias", group.getId());
                sensors.add(groupSensor);
            } else {
                Log.d(TAG, "Skipping duplicate group sensor: " + groupSensor);
            }
        }

        // next group
        index++;
        getGroupSensors(groups, index, sensors, callback);
    }

    private void onGroupsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        onListFailure(callback);
    }

    private void onGroupsSuccess(String response, List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        List<GroupModel> groups = GroupParser.parseGroups(response);
        getGroupSensors(groups, 0, sensors, callback);
    }

    private void onListComplete(List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        Registry.register(Constants.REG_SENSOR_LIST, sensors);
        Registry.register(Constants.REG_DEVICE_LIST, getDevices(sensors));
        Registry.register(Constants.REG_ENVIRONMENT_LIST, getEnvironments(sensors));

        forwardToView(this.grid, new AppEvent(SensorLibraryEvents.Done));
        Dispatcher.forwardEvent(SensorLibraryEvents.ListUpdated);

        if (null != callback) {
            callback.onSuccess(new BaseListLoadResult<SensorModel>(sensors));
        }
    }

    private void onListFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        Registry.unregister(Constants.REG_SENSOR_LIST);
        Registry.unregister(Constants.REG_DEVICE_LIST);
        Registry.unregister(Constants.REG_ENVIRONMENT_LIST);

        Dispatcher.forwardEvent(SensorLibraryEvents.ListUpdated);
        forwardToView(this.grid, new AppEvent(SensorLibraryEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private List<DeviceModel> getDevices(List<SensorModel> sensors) {
        List<DeviceModel> devices = new ArrayList<DeviceModel>();

        // gather the devices of all sensors in the library
        DeviceModel device;
        for (SensorModel sensor : sensors) {
            device = sensor.getDevice();
            if (device != null && !devices.contains(device)) {
                devices.add(device);
            }
        }

        return devices;
    }

    private List<EnvironmentModel> getEnvironments(List<SensorModel> sensors) {
        List<EnvironmentModel> environments = new ArrayList<EnvironmentModel>();

        // gather the devices of all sensors in the library
        EnvironmentModel environment;
        for (SensorModel sensor : sensors) {
            environment = sensor.getEnvironment();
            if (environment != null && !environments.contains(environment)) {
                environments.add(environment);
            }
        }

        return environments;
    }
}
