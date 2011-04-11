package nl.sense_os.commonsense.client.sensors.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.ajax.parsers.GroupParser;
import nl.sense_os.commonsense.client.ajax.parsers.SensorParser;
import nl.sense_os.commonsense.client.groups.GroupEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.SensorsEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.GroupModel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupSensorsController extends Controller {

    private static final String TAG = "GroupSensorsController";
    private View sensorsTree;

    public GroupSensorsController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(GroupSensorsEvents.ShowTree);
        registerEventTypes(GroupEvents.ListUpdated);
        registerEventTypes(SensorsEvents.UnshareSuccess, SensorsEvents.UnshareFailure);

        // local event types
        registerEventTypes(GroupSensorsEvents.ListRequest, GroupSensorsEvents.Done,
                GroupSensorsEvents.Working);
        registerEventTypes(GroupSensorsEvents.AjaxGroupsSuccess,
                GroupSensorsEvents.AjaxGroupsFailure);
        registerEventTypes(GroupSensorsEvents.AjaxUnownedSuccess,
                GroupSensorsEvents.AjaxUnownedFailure);
        registerEventTypes(GroupSensorsEvents.AjaxOwnedSuccess, GroupSensorsEvents.AjaxOwnedFailure);
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
        forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Done));

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
            forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Done));
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
            groups.get(index).add(sensor);
        }

        index++;
        getUnowned(groups, index, callback);
    }

    private void getOwnedFailure(AsyncCallback<List<TreeModel>> callback) {
        Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated);
        forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void getSensors(final AsyncCallback<List<TreeModel>> callback) {

        forwardToView(this.sensorsTree, new AppEvent(GroupSensorsEvents.Working));

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
            // aaaand we're done
            Registry.register(Constants.REG_GROUP_SENSORS, groups);

            forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Done));
            Dispatcher.forwardEvent(GroupSensorsEvents.ListUpdated);

            if (null != callback) {
                callback.onSuccess(new ArrayList<TreeModel>(groups));
            }
        }
    }

    private void getUnownedCallback(String response, List<GroupModel> groups, int index,
            AsyncCallback<List<TreeModel>> callback) {

        List<SensorModel> sensors = new ArrayList<SensorModel>();
        SensorParser.parseSensors(response, sensors);

        // sort the sensors according to the group members that own them
        Map<String, UserModel> memberMap = new HashMap<String, UserModel>();
        for (SensorModel sensor : sensors) {
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
        forwardToView(sensorsTree, new AppEvent(GroupSensorsEvents.Done));

        if (null != callback) {
            callback.onFailure(null);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(GroupSensorsEvents.ListRequest)) {
            Log.d(TAG, "ListRequest");
            final AsyncCallback<List<TreeModel>> callback = event.getData();
            getSensors(callback);

        } else if (type.equals(GroupSensorsEvents.AjaxGroupsSuccess)) {
            Log.d(TAG, "AjaxGroupsSuccess");
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
            Log.d(TAG, "AjaxUnownedSuccess");
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
            Log.d(TAG, "AjaxOwnedSuccess");
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

        } else {
            forwardToView(this.sensorsTree, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.sensorsTree = new GroupSensorsTree(this);
    }
}
