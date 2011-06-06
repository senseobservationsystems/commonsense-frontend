package nl.sense_os.commonsense.client.sensors.library;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.CommonSense;
import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.DeviceModel;
import nl.sense_os.commonsense.client.common.models.EnvironmentModel;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.ServiceModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.client.env.list.EnvEvents;
import nl.sense_os.commonsense.client.groups.list.AvailServicesResponseJso;
import nl.sense_os.commonsense.client.groups.list.GetGroupUsersResponseJso;
import nl.sense_os.commonsense.client.groups.list.GetGroupsResponseJso;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.client.sensors.share.SensorShareEvents;
import nl.sense_os.commonsense.client.sensors.unshare.UnshareEvents;
import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.client.states.defaults.StateDefaultsEvents;
import nl.sense_os.commonsense.client.states.list.StateListEvents;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LibraryController extends Controller {

    private final static Logger LOGGER = Logger.getLogger(LibraryController.class.getName());
    private static final int PER_PAGE = 1000;
    private View grid;
    private boolean isLoadingList;
    private boolean isLoadingUsers;
    private boolean isLoadingServices;

    public LibraryController() {

        // LOGGER.setLevel(Level.ALL);

        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.Show);

        registerEventTypes(LibraryEvents.ShowLibrary, LibraryEvents.LoadRequest,
                LibraryEvents.ListUpdated, LibraryEvents.FullDetailsAjaxSuccess,
                LibraryEvents.FullDetailsAjaxFailure);
        registerEventTypes(LibraryEvents.GroupsAjaxSuccess, LibraryEvents.GroupsAjaxFailure,
                LibraryEvents.GroupSensorsAjaxSuccess, LibraryEvents.GroupSensorsAjaxFailure);
        registerEventTypes(LibraryEvents.UsersAjaxSuccess, LibraryEvents.UsersAjaxFailure);
        registerEventTypes(LibraryEvents.AvailServicesAjaxSuccess,
                LibraryEvents.AvailServicesAjaxFailure);

        // external events
        registerEventTypes(SensorDeleteEvents.DeleteSuccess, SensorDeleteEvents.DeleteFailure);
        registerEventTypes(SensorShareEvents.ShareComplete);
        registerEventTypes(UnshareEvents.UnshareComplete);
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

    private void getAvailableServices(List<SensorModel> library, int index) {

        if (index < library.size()) {

            isLoadingServices = true;
            notifyState();

            SensorModel sensor = library.get(index);
            String params = "";
            if (sensor.getAlias() != -1) {
                params = "?alias=" + sensor.getAlias();
            }

            // prepare request properties
            final String method = "GET";
            final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/available"
                    + ".json" + params;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(LibraryEvents.AvailServicesAjaxSuccess);
            onSuccess.setData("index", index);
            onSuccess.setData("library", library);
            final AppEvent onFailure = new AppEvent(LibraryEvents.AvailServicesAjaxFailure);
            onFailure.setData("index", index);
            onFailure.setData("library", library);

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
            isLoadingServices = false;
            notifyState();
        }
    }

    private void getFullDetails(List<SensorModel> library, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // prepare request properties
        final String method = "GET";
        final String url = Urls.SENSORS + ".json" + "?per_page=" + PER_PAGE + "&page=" + page
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
        final String url = Urls.GROUPS + ".json" + "?per_page=" + PER_PAGE;
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

            int groupId = groups.get(index).getId();

            // prepare request properties
            final String method = "GET";
            final String url = Urls.SENSORS + ".json" + "?per_page=1000&details=full&alias="
                    + groupId;
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

            // continue loading more details in the background
            if (!CommonSense.HACK_SKIP_LIB_DETAILS) {
                getUsers(library, 0);
                getAvailableServices(library, 0);
            }

            // notify the view that the list is complete
            onLoadComplete(library, callback);
        }
    }

    private void getUsers(List<SensorModel> library, int index) {

        // get the first sensor that the user is owner of
        UserModel user = Registry.<UserModel> get(Constants.REG_USER);
        SensorModel sensor = null;
        while (index < library.size()) {
            if (user.equals(library.get(index).getOwner())) {
                sensor = library.get(index);
                break;
            }
            index++;
        }

        if (sensor != null) {

            isLoadingUsers = true;
            notifyState();

            // prepare request properties
            final String method = "GET";
            final String url = Urls.SENSORS + "/" + sensor.getId() + "/users" + ".json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(LibraryEvents.UsersAjaxSuccess);
            onSuccess.setData("index", index);
            onSuccess.setData("library", library);
            final AppEvent onFailure = new AppEvent(LibraryEvents.UsersAjaxFailure);
            onFailure.setData("index", index);
            onFailure.setData("library", library);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);

            Dispatcher.forwardEvent(ajaxRequest);

        } else {
            // hoorray we are done!
            isLoadingUsers = false;
            notifyState();
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(LibraryEvents.LoadRequest)) {
            LOGGER.finest("LoadRequest");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData("callback");
            final boolean renewCache = event.getData("renewCache");
            onLoadRequest(renewCache, callback);

        } else

        /*
         * Personal sensors
         */
        if (type.equals(LibraryEvents.FullDetailsAjaxSuccess)) {
            LOGGER.finest("FullDetailsAjaxSuccess");
            final String response = event.getData("response");
            final List<SensorModel> library = event.getData("library");
            final int page = event.getData("page");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData("callback");
            onFullDetailsSuccess(response, library, page, callback);

        } else if (type.equals(LibraryEvents.FullDetailsAjaxFailure)) {
            LOGGER.warning("FullDetailsAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData();
            onFullDetailsFailure(callback);

        } else

        /*
         * Group sensors
         */
        if (type.equals(LibraryEvents.GroupsAjaxSuccess)) {
            LOGGER.finest("GroupsAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> library = event.<List<SensorModel>> getData("library");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupsSuccess(response, library, callback);

        } else if (type.equals(LibraryEvents.GroupsAjaxFailure)) {
            LOGGER.warning("GroupsAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupsFailure(callback);

        } else if (type.equals(LibraryEvents.GroupSensorsAjaxSuccess)) {
            LOGGER.finest("GroupSensorsAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<GroupModel> groups = event.<List<GroupModel>> getData("groups");
            final int index = event.getData("index");
            final List<SensorModel> library = event.<List<SensorModel>> getData("library");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupSensorsSuccess(response, groups, index, library, callback);

        } else if (type.equals(LibraryEvents.GroupSensorsAjaxFailure)) {
            LOGGER.warning("GroupSensorsAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onGroupSensorsFailure(callback);

        } else

        /*
         * Available services
         */
        if (type.equals(LibraryEvents.AvailServicesAjaxSuccess)) {
            LOGGER.finest("AvailServicesAjaxSuccess");
            final String response = event.<String> getData("response");
            final int index = event.getData("index");
            final List<SensorModel> library = event.<List<SensorModel>> getData("library");
            onAvailServicesSuccess(response, library, index);

        } else if (type.equals(LibraryEvents.AvailServicesAjaxFailure)) {
            LOGGER.warning("AvailServicesAjaxFailure");
            final int index = event.getData("index");
            final List<SensorModel> library = event.<List<SensorModel>> getData("library");
            onAvailServicesFailure(library, index);

        } else

        /*
         * Sensor users
         */
        if (type.equals(LibraryEvents.UsersAjaxSuccess)) {
            LOGGER.finest("UsersAjaxSuccess");
            final String response = event.<String> getData("response");
            final int index = event.getData("index");
            final List<SensorModel> library = event.<List<SensorModel>> getData("library");
            onUsersSuccess(response, library, index);

        } else if (type.equals(LibraryEvents.UsersAjaxFailure)) {
            LOGGER.warning("UsersAjaxFailure");
            final int index = event.getData("index");
            final List<SensorModel> library = event.<List<SensorModel>> getData("library");
            onUsersFailure(library, index);

        } else

        /*
         * Clear data after logout
         */
        if (type.equals(LoginEvents.LoggedOut)) {
            LOGGER.finest("LoggedOut");
            onLogout();

        } else

        /*
         * Pass through to view
         */
        {
            LOGGER.finest("Pass through to grid");
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

    private void notifyState() {
        if (isLoadingList || isLoadingUsers || isLoadingServices) {
            forwardToView(this.grid, new AppEvent(LibraryEvents.Working));
        } else {
            forwardToView(this.grid, new AppEvent(LibraryEvents.Done));
        }

    }

    private void onAvailServicesFailure(List<SensorModel> library, int index) {
        index++;
        getAvailableServices(library, index);
    }

    private void onAvailServicesSuccess(String response, List<SensorModel> library, int index) {

        // parse list of services from response
        AvailServicesResponseJso jso = JsonUtils.unsafeEval(response);
        List<ServiceModel> services = jso.getServices();

        SensorModel sensor = library.get(index);
        sensor.set(SensorModel.AVAIL_SERVICES, services);

        index++;
        getAvailableServices(library, index);
    }

    private void onFullDetailsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        onLoadFailure(callback);
    }

    private void onFullDetailsSuccess(String response, List<SensorModel> library, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // parse response
        GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
        int total = responseJso.getTotal();
        library.addAll(responseJso.getSensors());

        LOGGER.fine("total: " + total + ", library size: " + library.size());

        if (total > library.size()) {
            // get the next page with sensors
            page++;
            // getFullDetails(library, page, callback);

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
        LOGGER.fine("Received group sensors response...");

        // parse group sensors
        List<SensorModel> groupSensors = new ArrayList<SensorModel>();
        GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
        groupSensors.addAll(responseJso.getSensors());

        LOGGER.finest("Parsed group sensors...");

        GroupModel group = groups.get(index);
        for (SensorModel groupSensor : groupSensors) {
            if (!library.contains(groupSensor)) {
                // set SensorModel.ALIAS property
                groupSensor.setAlias(group.getId());
                library.add(groupSensor);
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

        // parse list of groups from the response
        GetGroupsResponseJso jso = JsonUtils.unsafeEval(response);
        List<GroupModel> groups = jso.getGroups();

        getGroupSensors(groups, 0, library, callback);
    }

    private void onLoadComplete(List<SensorModel> library,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        LOGGER.fine("Load complete...");

        // update list of devices
        Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST).addAll(
                devicesFromLibrary(library));

        isLoadingList = false;
        notifyState();

        if (null != callback) {
            LOGGER.finest("Create load result...");
            ListLoadResult<SensorModel> result = new BaseListLoadResult<SensorModel>(library);

            LOGGER.finest("Call back with load result...");
            callback.onSuccess(result);
        }
        LOGGER.finest("Dispatch ListUpdated...");
        Dispatcher.forwardEvent(LibraryEvents.ListUpdated);
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

            isLoadingList = true;
            notifyState();

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

    private void onUsersFailure(List<SensorModel> library, int index) {
        index++;
        getUsers(library, index);
    }

    private void onUsersSuccess(String response, List<SensorModel> library, int index) {

        // parse list of users from the response
        GetGroupUsersResponseJso jso = JsonUtils.unsafeEval(response);
        List<UserModel> users = jso.getUsers();

        // remove the owner from the list
        users.remove(Registry.get(Constants.REG_USER));

        // update sensor model
        SensorModel sensor = library.get(index);
        sensor.set(SensorModel.USERS, users);

        // get the next sensor's users
        index++;
        getUsers(library, index);
    }
}
