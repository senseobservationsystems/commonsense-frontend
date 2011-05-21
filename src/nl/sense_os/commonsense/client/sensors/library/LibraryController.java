package nl.sense_os.commonsense.client.sensors.library;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.constants.Constants;
import nl.sense_os.commonsense.shared.constants.Urls;
import nl.sense_os.commonsense.shared.models.DeviceModel;
import nl.sense_os.commonsense.shared.models.EnvironmentModel;
import nl.sense_os.commonsense.shared.models.GroupModel;
import nl.sense_os.commonsense.shared.models.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LibraryController extends Controller {

    private final static Logger logger = Logger.getLogger("LibraryController");
    private static final int PER_PAGE = 1000;
    private View grid;

    public LibraryController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.Show);

        registerEventTypes(LibraryEvents.ShowLibrary, LibraryEvents.LoadRequest,
                LibraryEvents.ListUpdated, LibraryEvents.FullDetailsAjaxSuccess,
                LibraryEvents.FullDetailsAjaxFailure);
        registerEventTypes(LibraryEvents.GroupsAjaxSuccess, LibraryEvents.GroupsAjaxFailure,
                LibraryEvents.GroupSensorsAjaxSuccess, LibraryEvents.GroupSensorsAjaxFailure);

        // external events
        registerEventTypes(SensorDeleteEvents.DeleteSuccess, SensorDeleteEvents.DeleteFailure);
        registerEventTypes(SensorShareEvents.ShareComplete, SensorShareEvents.ShareFailed,
                SensorShareEvents.ShareCancelled);
        registerEventTypes(StateCreateEvents.CreateServiceComplete, StateListEvents.RemoveComplete,
                StateDefaultsEvents.CheckDefaultsSuccess);
        registerEventTypes(EnvCreateEvents.CreateSuccess, EnvEvents.DeleteSuccess);
    }

    private List<DeviceModel> devicesFromLibrary(List<SensorModel> library) {
        List<DeviceModel> devices = new ArrayList<DeviceModel>();

        // gather the devices of all sensors in the library
        DeviceModel device;
        for (SensorModel sensor : library) {
            device = sensor.getDevice();
            if (device != null && !devices.contains(device)) {
                devices.add(device);
            }
        }

        return devices;
    }

    private void getFullDetails(List<SensorModel> library, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // prepare request properties
        final String method = "GET";
        final String url = Urls.SENSORS + "?per_page=" + PER_PAGE + "&page=" + page
                + "&details=full";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(LibraryEvents.FullDetailsAjaxSuccess);
        onSuccess.setData("library", library);
        onSuccess.setData("page", page);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(LibraryEvents.FullDetailsAjaxFailure);
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

    private void getGroups(List<SensorModel> library,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // prepare request properties
        final String method = "GET";
        final String url = Urls.GROUPS + "?per_page=" + PER_PAGE;
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(LibraryEvents.GroupsAjaxSuccess);
        onSuccess.setData("library", library);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(LibraryEvents.GroupsAjaxFailure);
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

    private void getGroupSensors(List<GroupModel> groups, int index, List<SensorModel> library,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        if (index < groups.size()) {

            String groupId = groups.get(index).getId();

            // prepare request properties
            final String method = "GET";
            final String url = Urls.SENSORS + "?per_page=1000&details=full&alias=" + groupId;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(LibraryEvents.GroupSensorsAjaxSuccess);
            onSuccess.setData("groups", groups);
            onSuccess.setData("index", index);
            onSuccess.setData("library", library);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(LibraryEvents.GroupSensorsAjaxFailure);
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
            onLoadComplete(library, callback);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(LibraryEvents.LoadRequest)) {
            // logger.fine( "LoadRequest");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData("callback");
            final boolean renewCache = event.getData("renewCache");
            onLoadRequest(renewCache, callback);

        } else

        /*
         * Personal sensors
         */
        if (type.equals(LibraryEvents.FullDetailsAjaxSuccess)) {
            // logger.fine( "FullDetailsAjaxSuccess");
            final String response = event.getData("response");
            final List<SensorModel> library = event.getData("library");
            final int page = event.getData("page");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData("callback");
            onFullDetailsSuccess(response, library, page, callback);

        } else if (type.equals(LibraryEvents.FullDetailsAjaxFailure)) {
            logger.warning("FullDetailsAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData();
            onFullDetailsFailure(callback);

        } else

        /*
         * Group sensors
         */
        if (type.equals(LibraryEvents.GroupsAjaxSuccess)) {
            // logger.fine( "GroupsAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> library = event.<List<SensorModel>> getData("library");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupsSuccess(response, library, callback);

        } else if (type.equals(LibraryEvents.GroupsAjaxFailure)) {
            logger.warning("GroupsAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupsFailure(callback);

        } else if (type.equals(LibraryEvents.GroupSensorsAjaxSuccess)) {
            // logger.fine( "GroupSensorsAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<GroupModel> groups = event.<List<GroupModel>> getData("groups");
            final int index = event.getData("index");
            final List<SensorModel> library = event.<List<SensorModel>> getData("library");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupSensorsSuccess(response, groups, index, library, callback);

        } else if (type.equals(LibraryEvents.GroupSensorsAjaxFailure)) {
            logger.warning("GroupSensorsAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupSensorsFailure(callback);

        } else

        /*
         * Clear data after logout
         */
        if (type.equals(LoginEvents.LoggedOut)) {
            // logger.fine( "LoggedOut");
            onLogout();

        } else

        /*
         * Pass through to view
         */
        {
            // logger.fine( "Pass through to grid");
            forwardToView(this.grid, event);
        }

    }

    @Override
    protected void initialize() {
        super.initialize();
        this.grid = new LibraryGrid(this);

        // initialize library and lists of devices and environments
        Registry.register(Constants.REG_SENSOR_LIST, new ArrayList<SensorModel>());
        Registry.register(Constants.REG_DEVICE_LIST, new ArrayList<DeviceModel>());
    }

    private void onFullDetailsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        onLoadFailure(callback);
    }

    private void onFullDetailsSuccess(String response, List<SensorModel> library, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        int total = SensorParser.parseSensors(response, library);
        if (total > library.size()) {
            // get the next page with sensors
            page++;
            getFullDetails(library, page, callback);

        } else {
            // get the group IDs to get the group sensors
            getGroups(library, callback);
        }
    }

    private void onGroupSensorsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        onLoadFailure(callback);
    }

    private void onGroupSensorsSuccess(String response, List<GroupModel> groups, int index,
            List<SensorModel> library, AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // parse group sensors
        List<SensorModel> groupSensors = new ArrayList<SensorModel>();
        SensorParser.parseSensors(response, groupSensors);

        GroupModel group = groups.get(index);
        for (SensorModel groupSensor : groupSensors) {
            if (!library.contains(groupSensor)) {
                // set "alias" property
                groupSensor.set("alias", group.getId());
                library.add(groupSensor);
            } else {
                logger.fine("Skipping duplicate group sensor: " + groupSensor);
            }
        }

        // next group
        index++;
        getGroupSensors(groups, index, library, callback);
    }

    private void onGroupsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        onLoadFailure(callback);
    }

    private void onGroupsSuccess(String response, List<SensorModel> library,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        List<GroupModel> groups = GroupParser.parseGroups(response);
        getGroupSensors(groups, 0, library, callback);
    }

    private void onLoadComplete(List<SensorModel> library,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // update list of devices
        Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST).addAll(
                devicesFromLibrary(library));

        forwardToView(this.grid, new AppEvent(LibraryEvents.Done));
        Dispatcher.forwardEvent(LibraryEvents.ListUpdated);

        if (null != callback) {
            callback.onSuccess(new BaseListLoadResult<SensorModel>(library));
        }
    }

    private void onLoadFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST).clear();
        Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST).clear();
        Registry.<List<EnvironmentModel>> get(Constants.REG_ENVIRONMENT_LIST).clear();

        Dispatcher.forwardEvent(LibraryEvents.ListUpdated);
        forwardToView(this.grid, new AppEvent(LibraryEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onLoadRequest(boolean renewCache,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        List<SensorModel> library = Registry.get(Constants.REG_SENSOR_LIST);
        if (renewCache) {
            library.clear();
            Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST).clear();

            forwardToView(this.grid, new AppEvent(LibraryEvents.Working));
            getFullDetails(library, 0, callback);
        } else {
            onLoadComplete(library, callback);
        }
    }

    /**
     * Clears the library and lists of devices.
     */
    private void onLogout() {
        List<SensorModel> library = Registry.get(Constants.REG_SENSOR_LIST);
        library.clear();

        List<DeviceModel> devices = Registry.get(Constants.REG_DEVICE_LIST);
        devices.clear();
    }
}
