package nl.sense_os.commonsense.client.sensors.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.groups.GroupEvents;
import nl.sense_os.commonsense.client.json.parsers.GroupParser;
import nl.sense_os.commonsense.client.json.parsers.SensorParser;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.GroupModel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupSensorsController extends Controller {

    private static final String TAG = "GroupSensorsController";
    private View unshareDialog;
    private View tree;

    public GroupSensorsController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(GroupSensorsEvents.ShowTree, GroupSensorsEvents.Done,
                GroupSensorsEvents.Working);

        // get list of groups sensors events
        registerEventTypes(GroupSensorsEvents.ListRequest);
        registerEventTypes(GroupEvents.ListUpdated);
        registerEventTypes(GroupSensorsEvents.AjaxGroupsSuccess,
                GroupSensorsEvents.AjaxGroupsFailure);
        registerEventTypes(GroupSensorsEvents.AjaxUnownedSuccess,
                GroupSensorsEvents.AjaxUnownedFailure);
        registerEventTypes(GroupSensorsEvents.AjaxOwnedSuccess, GroupSensorsEvents.AjaxOwnedFailure);
        registerEventTypes(GroupSensorsEvents.AjaxDirectSharesSuccess,
                GroupSensorsEvents.AjaxDirectSharesFailure);

        // remove shared sensor events
        registerEventTypes(GroupSensorsEvents.ShowUnshareDialog, GroupSensorsEvents.UnshareRequest,
                GroupSensorsEvents.AjaxUnshareSuccess, GroupSensorsEvents.AjaxUnshareFailure);
    }

    private void getDirectShares(List<GroupModel> groups, AsyncCallback<List<TreeModel>> callback) {
        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=1000&owned=0";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(GroupSensorsEvents.AjaxDirectSharesSuccess);
        onSuccess.setData("groups", groups);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(GroupSensorsEvents.AjaxDirectSharesFailure);
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

    private void getDirectSharesFailure(AsyncCallback<List<TreeModel>> callback) {
        Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated);
        forwardToView(tree, new AppEvent(GroupSensorsEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void getDirectSharesCallback(String response, List<GroupModel> groups,
            AsyncCallback<List<TreeModel>> callback) {
        // parse the sensors
        List<SensorModel> sensors = new ArrayList<SensorModel>();
        SensorParser.parseSensors(response, sensors);

        Map<String, UserModel> owners = new HashMap<String, UserModel>();
        for (SensorModel sensor : sensors) {
            // get the user that owns the sensors
            UserModel owner = sensor.getOwner();
            if (null != owners.get(owner.getId())) {
                owner = owners.get(owner.getId());
            }
            owner.add(sensor);
            owners.put(owner.getId(), owner);
        }

        // add the owners to the list of group sensors
        ArrayList<TreeModel> list = new ArrayList<TreeModel>(groups);
        list.addAll(owners.values());

        // aaaand we're done
        Registry.register(Constants.REG_GROUP_SENSORS, list);

        forwardToView(tree, new AppEvent(GroupSensorsEvents.Done));
        Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated);

        if (null != callback) {
            callback.onSuccess(list);
        }
    }

    private void getGroups(AsyncCallback<List<TreeModel>> callback) {

        final List<GroupModel> groups = Registry.<List<GroupModel>> get(Constants.REG_GROUPS);
        if (null == groups) {
            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_GROUPS + "?per_page=1000";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(GroupSensorsEvents.AjaxGroupsSuccess);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(GroupSensorsEvents.AjaxGroupsFailure);
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
            // create a copy of the list in the registry to avoid messing with the structure
            List<GroupModel> copy = new ArrayList<GroupModel>();
            for (GroupModel group : groups) {
                copy.add(new GroupModel(group.getProperties()));
            }
            getUnowned(copy, 0, callback);
        }
    }

    private void getGroupsCallback(String response, AsyncCallback<List<TreeModel>> callback) {
        List<GroupModel> groups = GroupParser.parseGroups(response);
        getUnowned(groups, 0, callback);
    }

    private void getGroupsFailure(AsyncCallback<List<TreeModel>> callback) {
        Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated);
        forwardToView(tree, new AppEvent(GroupSensorsEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void getOwned(List<GroupModel> groups, int index,
            AsyncCallback<List<TreeModel>> callback) {
        if (index < groups.size()) {

            String groupId = groups.get(index).get(GroupModel.ID);

            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_SENSORS + "?per_page=1000&owned=1&alias=" + groupId;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(GroupSensorsEvents.AjaxOwnedSuccess);
            onSuccess.setData("groups", groups);
            onSuccess.setData("index", index);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(GroupSensorsEvents.AjaxOwnedFailure);
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
            // should never happen
            Log.w(TAG, "Something is wrong with the index!");
            forwardToView(tree, new AppEvent(GroupSensorsEvents.Done));
            if (null != callback) {
                callback.onFailure(null);
            }
        }
    }

    private void getOwnedCallback(String response, List<GroupModel> groups, int index,
            AsyncCallback<List<TreeModel>> callback) {

        List<SensorModel> sensors = new ArrayList<SensorModel>();
        SensorParser.parseSensors(response, sensors);

        // add the sensors to the group
        for (SensorModel sensor : sensors) {
            String alias = groups.get(index).get(GroupModel.ID);
            sensor.set("alias", alias);
            groups.get(index).add(sensor);
        }

        index++;
        getUnowned(groups, index, callback);
    }

    private void getOwnedFailure(AsyncCallback<List<TreeModel>> callback) {
        Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated);
        forwardToView(tree, new AppEvent(GroupSensorsEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void getSensors(final AsyncCallback<List<TreeModel>> callback) {

        forwardToView(this.tree, new AppEvent(GroupSensorsEvents.Working));

        // get list of groups before getting the group sensors
        getGroups(callback);
    }

    private void getUnowned(List<GroupModel> groups, int index,
            AsyncCallback<List<TreeModel>> callback) {

        if (index < groups.size()) {

            String groupId = groups.get(index).get(GroupModel.ID);

            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_SENSORS + "?per_page=1000&owned=0&alias=" + groupId;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(GroupSensorsEvents.AjaxUnownedSuccess);
            onSuccess.setData("groups", groups);
            onSuccess.setData("index", index);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(GroupSensorsEvents.AjaxUnownedFailure);
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
            getDirectShares(groups, callback);
        }
    }

    private void getUnownedCallback(String response, List<GroupModel> groups, int index,
            AsyncCallback<List<TreeModel>> callback) {

        List<SensorModel> sensors = new ArrayList<SensorModel>();
        SensorParser.parseSensors(response, sensors);

        // sort the sensors according to the group members that own them
        Map<String, UserModel> memberMap = new HashMap<String, UserModel>();
        for (SensorModel sensor : sensors) {
            // set the sensor's alias (for fetching data
            String alias = groups.get(index).get(GroupModel.ID);
            sensor.set("alias", alias);

            // get the sensor's owner
            UserModel owner = sensor.<UserModel> get(SensorModel.OWNER);

            // get owner from list of group members
            UserModel member = memberMap.get(owner.get(UserModel.ID));
            if (null == member) {
                member = owner;
            }

            // update list of group members
            member.add(sensor);
            memberMap.put(member.<String> get(UserModel.ID), member);
        }

        // add the members with their sensors to the group
        for (UserModel member : memberMap.values()) {
            groups.get(index).add(member);
        }

        getOwned(groups, index, callback);
    }

    private void getUnownedFailure(AsyncCallback<List<TreeModel>> callback) {
        Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated);
        forwardToView(tree, new AppEvent(GroupSensorsEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(GroupSensorsEvents.ListRequest)) {
            // Log.d(TAG, "ListRequest");
            final AsyncCallback<List<TreeModel>> callback = event.getData();
            getSensors(callback);

        } else if (type.equals(GroupSensorsEvents.AjaxGroupsSuccess)) {
            // Log.d(TAG, "AjaxGroupsSuccess");
            final String response = event.<String> getData("response");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getGroupsCallback(response, callback);

        } else if (type.equals(GroupSensorsEvents.AjaxGroupsFailure)) {
            Log.w(TAG, "AjaxGroupsFailure");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getGroupsFailure(callback);

        } else if (type.equals(GroupSensorsEvents.AjaxUnownedSuccess)) {
            // Log.d(TAG, "AjaxUnownedSuccess");
            final String response = event.<String> getData("response");
            final List<GroupModel> groups = event.<List<GroupModel>> getData("groups");
            final int index = event.getData("index");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getUnownedCallback(response, groups, index, callback);

        } else if (type.equals(GroupSensorsEvents.AjaxUnownedFailure)) {
            Log.w(TAG, "AjaxUnownedFailure");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getUnownedFailure(callback);

        } else if (type.equals(GroupSensorsEvents.AjaxOwnedSuccess)) {
            // Log.d(TAG, "AjaxOwnedSuccess");
            final String response = event.<String> getData("response");
            final List<GroupModel> groups = event.<List<GroupModel>> getData("groups");
            final int index = event.getData("index");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getOwnedCallback(response, groups, index, callback);

        } else if (type.equals(GroupSensorsEvents.AjaxOwnedFailure)) {
            Log.w(TAG, "AjaxOwnedFailure");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getOwnedFailure(callback);

        } else if (type.equals(GroupSensorsEvents.AjaxDirectSharesSuccess)) {
            // Log.d(TAG, "AjaxDirectSharesSuccess");
            final String response = event.<String> getData("response");
            final List<GroupModel> groups = event.<List<GroupModel>> getData("groups");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getDirectSharesCallback(response, groups, callback);

        } else if (type.equals(GroupSensorsEvents.AjaxDirectSharesFailure)) {
            Log.w(TAG, "AjaxDirectSharesFailure");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getDirectSharesFailure(callback);

        } else if (type.equals(GroupSensorsEvents.UnshareRequest)) {
            // Log.d(TAG, "UnshareRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            unshare(sensors, 0);

        } else if (type.equals(GroupSensorsEvents.AjaxUnshareSuccess)) {
            // Log.d(TAG, "AjaxUnshareSuccess");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            unshareCallback(sensors);

        } else if (type.equals(GroupSensorsEvents.AjaxUnshareFailure)) {
            Log.w(TAG, "AjaxUnshareFailure");
            // final int code = event.getData("code");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int retryCount = event.<Integer> getData("retry");
            unshareFailure(sensors, retryCount);

        } else if (type.equals(GroupSensorsEvents.ShowUnshareDialog)) {
            forwardToView(this.unshareDialog, event);

        } else {
            forwardToView(this.tree, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.tree = new GroupSensorsTree(this);
        this.unshareDialog = new UnshareDialog(this);
    }

    /**
     * Unshares a list of sensors from certain users, using Ajax requests to CommonSense.
     * 
     * @param sensors
     *            The list of sensors that have to be unshared. The sensors must have a "user"
     *            property, containing the ID of the user to unshare.
     * @param retryCount
     *            Counter for failed requests that were retried.
     */
    private void unshare(List<SensorModel> sensors, int retryCount) {
        if (null != sensors && sensors.size() > 0) {
            ModelData sensor = sensors.get(0);

            // get the user that we want to unshare the sensor with
            String userId = sensor.get("user");

            // prepare request properties
            final String method = "DELETE";
            final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/users/"
                    + userId;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(GroupSensorsEvents.AjaxUnshareSuccess);
            onSuccess.setData("sensors", sensors);
            final AppEvent onFailure = new AppEvent(GroupSensorsEvents.AjaxUnshareFailure);
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
            forwardToView(this.unshareDialog, new AppEvent(GroupSensorsEvents.UnshareSuccess));
            forwardToView(this.tree, new AppEvent(GroupSensorsEvents.UnshareSuccess));
        }
    }

    private void unshareCallback(List<SensorModel> sensors) {
        // Goodbye sensor!
        sensors.remove(0);

        // continue with the rest of the list
        unshare(sensors, 0);
    }

    /**
     * Handles a failed unshare request. Retries the request up to three times, after this it gives
     * up and dispatches {@link SensorsEvents#UnshareFailure}.
     * 
     * @param sensors
     *            List of sensors that have to be unshared.
     * @param retryCount
     *            Number of times this request was attempted.
     */
    private void unshareFailure(List<SensorModel> sensors, int retryCount) {

        if (retryCount < 3) {
            retryCount++;
            unshare(sensors, retryCount);
        } else {
            forwardToView(this.unshareDialog, new AppEvent(GroupSensorsEvents.UnshareFailure));
            forwardToView(this.tree, new AppEvent(GroupSensorsEvents.UnshareFailure));
        }
    }
}
