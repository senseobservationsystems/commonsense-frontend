package nl.sense_os.commonsense.client.sensors.library;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.json.parsers.GroupParser;
import nl.sense_os.commonsense.client.common.json.parsers.SensorParser;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.client.sensors.share.SensorShareEvents;
import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.client.states.list.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.GroupModel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.UserModel;

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
                SensorLibraryEvents.FullDetailsAjaxFailure, SensorLibraryEvents.ListPhysicalAjaxSuccess,
                SensorLibraryEvents.ListPhysicalAjaxFailure);
        registerEventTypes(SensorLibraryEvents.AjaxGroupsSuccess,
                SensorLibraryEvents.AjaxGroupsFailure);
        registerEventTypes(SensorLibraryEvents.AjaxUnownedSuccess,
                SensorLibraryEvents.AjaxUnownedFailure);
        registerEventTypes(SensorLibraryEvents.AjaxOwnedSuccess,
                SensorLibraryEvents.AjaxOwnedFailure);
        registerEventTypes(SensorLibraryEvents.AjaxDirectSharesSuccess,
                SensorLibraryEvents.AjaxDirectSharesFailure);

        // external events
        registerEventTypes(SensorDeleteEvents.DeleteSuccess, SensorDeleteEvents.DeleteFailure);
        registerEventTypes(SensorShareEvents.ShareComplete, SensorShareEvents.ShareFailed,
                SensorShareEvents.ShareCancelled);
        registerEventTypes(StateCreateEvents.CreateServiceComplete, StateEvents.RemoveComplete,
                StateEvents.CheckDefaultsSuccess);
    }

    private void getDirectShares(List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=1000&owned=0";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(SensorLibraryEvents.AjaxDirectSharesSuccess);
        onSuccess.setData("sensors", sensors);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(SensorLibraryEvents.AjaxDirectSharesFailure);
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
            getUnowned(copy, 0, sensors, callback);
        }
    }

    private void getList(List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        forwardToView(this.grid, new AppEvent(SensorLibraryEvents.Working));

        getFullDetails(sensors, 0, callback);
    }

    private void getOwned(List<GroupModel> groups, int index, List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        if (index < groups.size()) {

            String groupId = groups.get(index).get(GroupModel.ID);

            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_SENSORS + "?per_page=1000&owned=1&alias=" + groupId;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(SensorLibraryEvents.AjaxOwnedSuccess);
            onSuccess.setData("groups", groups);
            onSuccess.setData("index", index);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(SensorLibraryEvents.AjaxOwnedFailure);
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
            getDirectShares(sensors, callback);
        }
    }

    private void getPhysical(List<SensorModel> sensors, List<SensorModel> physical, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        forwardToView(this.grid, new AppEvent(SensorLibraryEvents.Working));

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=" + PER_PAGE + "&page=" + page
                + "&physical=1&owned=1";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(SensorLibraryEvents.ListPhysicalAjaxSuccess);
        onSuccess.setData("sensors", sensors);
        onSuccess.setData("physical", physical);
        onSuccess.setData("page", page);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(SensorLibraryEvents.ListPhysicalAjaxFailure);
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

    private void getUnowned(List<GroupModel> groups, int index, List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        if (index < groups.size()) {

            String groupId = groups.get(index).get(GroupModel.ID);

            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_SENSORS + "?per_page=1000&owned=0&alias=" + groupId;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(SensorLibraryEvents.AjaxUnownedSuccess);
            onSuccess.setData("groups", groups);
            onSuccess.setData("index", index);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(SensorLibraryEvents.AjaxUnownedFailure);
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
            // continue the list by getting the sensors that are shared directly with me
            getOwned(groups, 0, sensors, callback);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorLibraryEvents.ListRequested)) {
            // Log.d(TAG, "ListRequested");
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

        } else if (type.equals(SensorLibraryEvents.ListPhysicalAjaxSuccess)) {
            // Log.d(TAG, "ListPhysicalAjaxSuccess");
            final String response = event.getData("response");
            final List<SensorModel> sensors = event.getData("sensors");
            final List<SensorModel> physical = event.getData("physical");
            final int page = event.getData("page");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData("callback");
            onPhysicalSuccess(response, sensors, physical, page, callback);

        } else if (type.equals(SensorLibraryEvents.ListPhysicalAjaxFailure)) {
            Log.w(TAG, "ListPhysicalAjaxFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData();
            onPhysicalFailure(callback);

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

        } else if (type.equals(SensorLibraryEvents.AjaxUnownedSuccess)) {
            // Log.d(TAG, "AjaxUnownedSuccess");
            final String response = event.<String> getData("response");
            final List<GroupModel> groups = event.<List<GroupModel>> getData("groups");
            final int index = event.getData("index");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onUnownedSuccess(response, groups, index, sensors, callback);

        } else if (type.equals(SensorLibraryEvents.AjaxUnownedFailure)) {
            Log.w(TAG, "AjaxUnownedFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onUnownedFailure(callback);

        } else if (type.equals(SensorLibraryEvents.AjaxOwnedSuccess)) {
            // Log.d(TAG, "AjaxOwnedSuccess");
            final String response = event.<String> getData("response");
            final List<GroupModel> groups = event.<List<GroupModel>> getData("groups");
            final int index = event.getData("index");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onOwnedSuccess(response, groups, index, sensors, callback);

        } else if (type.equals(SensorLibraryEvents.AjaxOwnedFailure)) {
            Log.w(TAG, "AjaxOwnedFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onOwnedFailure(callback);

        } else if (type.equals(SensorLibraryEvents.AjaxDirectSharesSuccess)) {
            // Log.d(TAG, "AjaxDirectSharesSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onDirectSharesSuccess(response, sensors, callback);

        } else if (type.equals(SensorLibraryEvents.AjaxDirectSharesFailure)) {
            Log.w(TAG, "AjaxDirectSharesFailure");
            final AsyncCallback<ListLoadResult<SensorModel>> callback = event
                    .<AsyncCallback<ListLoadResult<SensorModel>>> getData("callback");
            onDirectSharesFailure(callback);

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

    private void onDirectSharesFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        Dispatcher.forwardEvent(SensorLibraryEvents.ListUpdated);
        forwardToView(this.grid, new AppEvent(SensorLibraryEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onDirectSharesSuccess(String response, List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        // parse the sensors
        SensorParser.parseSensors(response, sensors);

        /*
         * Map<String, UserModel> owners = new HashMap<String, UserModel>(); for (SensorModel sensor
         * : sensors) { // get the user that owns the sensors UserModel owner = sensor.getOwner();
         * if (null != owners.get(owner.getId())) { owner = owners.get(owner.getId()); }
         * owner.add(sensor); owners.put(owner.getId(), owner); }
         * 
         * // add the owners to the list of group sensors ArrayList<TreeModel> list = new
         * ArrayList<TreeModel>(groups); list.addAll(owners.values());
         */

        // aaaand we're done
        onListComplete(sensors, callback);
    }

    private void onFullDetailsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        forwardToView(grid, new AppEvent(SensorLibraryEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onFullDetailsSuccess(String response, List<SensorModel> sensors, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        int total = SensorParser.parseSensors(response, sensors);
        if (total > sensors.size()) {
            page++;
            getFullDetails(sensors, page, callback);
        } else {
            onListComplete(sensors, callback);
            // UserModel user = Registry.<UserModel> get(Constants.REG_USER);
            // for (SensorModel sensor : sensors) {
            // sensor.set(SensorModel.OWNER, user);
            // }
            // List<SensorModel> physical = new ArrayList<SensorModel>();
            // getListPhysical(sensors, physical, 0, callback);
        }
    }

    private void onGroupsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        Dispatcher.forwardEvent(SensorLibraryEvents.ListUpdated);
        forwardToView(this.grid, new AppEvent(SensorLibraryEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onGroupsSuccess(String response, List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        List<GroupModel> groups = GroupParser.parseGroups(response);
        getUnowned(groups, 0, sensors, callback);
    }

    private void onListComplete(List<SensorModel> sensors,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {
        if (null != callback) {
            callback.onSuccess(new BaseListLoadResult<SensorModel>(sensors));
        }
        Registry.register(Constants.REG_MY_SENSORS_LIST, sensors);
        forwardToView(grid, new AppEvent(SensorLibraryEvents.Done));
        Dispatcher.forwardEvent(SensorLibraryEvents.ListUpdated);
    }

    private void onOwnedFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        Dispatcher.forwardEvent(SensorLibraryEvents.ListUpdated);
        forwardToView(this.grid, new AppEvent(SensorLibraryEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onOwnedSuccess(String response, List<GroupModel> groups, int index,
            List<SensorModel> sensors, AsyncCallback<ListLoadResult<SensorModel>> callback) {

        SensorParser.parseSensors(response, sensors);

        /*
         * // add the sensors to the group for (SensorModel sensor : sensors) { String alias =
         * groups.get(index).get(GroupModel.ID); sensor.set("alias", alias);
         * groups.get(index).add(sensor); }
         */

        index++;
        getOwned(groups, index, sensors, callback);
    }

    private void onPhysicalFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        forwardToView(grid, new AppEvent(SensorLibraryEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onPhysicalSuccess(String response, List<SensorModel> sensors,
            List<SensorModel> physical, int page,
            AsyncCallback<ListLoadResult<SensorModel>> callback) {

        // parse physical sensors
        int total = SensorParser.parseSensors(response, physical);

        if (total > physical.size()) {
            page++;
            getPhysical(sensors, physical, page, callback);

        } else {

            // set owner of physical sensors
            UserModel user = Registry.<UserModel> get(Constants.REG_USER);
            for (SensorModel sensor : physical) {
                sensor.set(SensorModel.OWNER, user);
            }

            List<SensorModel> complete = new ArrayList<SensorModel>(physical);
            for (SensorModel sensor : sensors) {
                if (!sensor.getType().equals("1")) {
                    complete.add(sensor);
                }
            }

            // continue with group sensors
            getGroups(complete, callback);
        }
    }

    private void onUnownedFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
        Dispatcher.forwardEvent(SensorLibraryEvents.ListUpdated);
        forwardToView(this.grid, new AppEvent(SensorLibraryEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onUnownedSuccess(String response, List<GroupModel> groups, int index,
            List<SensorModel> sensors, AsyncCallback<ListLoadResult<SensorModel>> callback) {

        SensorParser.parseSensors(response, sensors);

        /*
         * // sort the sensors according to the group members that own them Map<String, UserModel>
         * memberMap = new HashMap<String, UserModel>(); for (SensorModel sensor : sensors) { // set
         * the sensor's alias (for fetching data String alias =
         * groups.get(index).get(GroupModel.ID); sensor.set("alias", alias);
         * 
         * // get the sensor's owner UserModel owner = sensor.<UserModel> get(SensorModel.OWNER);
         * 
         * // get owner from list of group members UserModel member =
         * memberMap.get(owner.get(UserModel.ID)); if (null == member) { member = owner; }
         * 
         * // update list of group members member.add(sensor); memberMap.put(member.<String>
         * get(UserModel.ID), member); }
         * 
         * // add the members with their sensors to the group for (UserModel member :
         * memberMap.values()) { groups.get(index).add(member); }
         */

        index++;
        getUnowned(groups, index, sensors, callback);
    }
}
