package nl.sense_os.commonsense.client.states.list;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.json.parsers.SensorParser;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.states.connect.StateConnectEvents;
import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateController extends Controller {

    private static final String TAG = "StateController";
    private View tree;

    public StateController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(VizEvents.Show);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(StateEvents.ShowGrid);

        // external triggers to initiate a list update
        registerEventTypes(StateCreateEvents.CreateServiceComplete,
                StateConnectEvents.ConnectSuccess);

        // events to update the list of groups
        registerEventTypes(StateEvents.ListRequested, StateEvents.AjaxSensorsSuccess,
                StateEvents.AjaxSensorsFailure, StateEvents.AjaxConnectedSuccess,
                StateEvents.AjaxConnectedFailure, StateEvents.GetMethodsAjaxSuccess,
                StateEvents.GetMethodsAjaxFailure);

        registerEventTypes(StateEvents.RemoveRequested, StateEvents.AjaxDisconnectFailure,
                StateEvents.AjaxDisconnectSuccess);

        // check default states events
        registerEventTypes(StateEvents.CheckDefaults, StateEvents.AjaxDefaultsSuccess,
                StateEvents.AjaxDefaultsFailure);
    }

    private void checkDefaults() {
        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_STATES + "/default/check";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEvents.AjaxDefaultsSuccess);
        final AppEvent onFailure = new AppEvent(StateEvents.AjaxDefaultsFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void checkDefaultsCallback(String response) {
        forwardToView(this.tree, new AppEvent(StateEvents.CheckDefaultsSuccess));
    }

    private void checkDefaultsFailure() {
        forwardToView(this.tree, new AppEvent(StateEvents.CheckDefaultsFailure));
    }

    private void disconnectService(TreeModel sensor, TreeModel service) {

        // prepare request data
        final String method = "DELETE";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services/"
                + service.<String> get("id");
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEvents.AjaxDisconnectSuccess);
        final AppEvent onFailure = new AppEvent(StateEvents.AjaxDisconnectFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void disconnectServiceCallback(String response) {
        forwardToView(this.tree, new AppEvent(StateEvents.RemoveComplete));
    }

    private void disconnectServiceErrorCallback(int code) {
        forwardToView(this.tree, new AppEvent(StateEvents.RemoveFailed));
    }

    private void getConnected(List<SensorModel> states, int index,
            AsyncCallback<List<TreeModel>> callback) {

        if (index < states.size()) {
            String stateId = states.get(index).getId();

            // prepare request properties
            final String method = "GET";
            final String url = Constants.URL_SENSORS + "/" + stateId + "/sensors";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(StateEvents.AjaxConnectedSuccess);
            onSuccess.setData("states", states);
            onSuccess.setData("index", index);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(StateEvents.AjaxConnectedFailure);
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
            // continue getting service methods for each state
            getServiceMethods(states, 0, callback);
        }
    }

    private void getConnectedCallback(String response, List<SensorModel> states, int index,
            AsyncCallback<List<TreeModel>> callback) {
        List<SensorModel> sensors = new ArrayList<SensorModel>();
        SensorParser.parseSensors(response, sensors);

        for (SensorModel sensor : sensors) {
            states.get(index).add(sensor);
        }
        index++;
        getConnected(states, index, callback);
    }

    private void getConnectedFailure(AsyncCallback<List<TreeModel>> callback) {
        forwardToView(this.tree, new AppEvent(StateEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void getMyServices(final AsyncCallback<List<TreeModel>> callback) {

        forwardToView(this.tree, new AppEvent(StateEvents.Working));

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "?per_page=1000&owned=1";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEvents.AjaxSensorsSuccess);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(StateEvents.AjaxSensorsFailure);
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

    private void getMyServicesCallback(String response, AsyncCallback<List<TreeModel>> callback) {
        List<SensorModel> sensors = new ArrayList<SensorModel>();
        SensorParser.parseSensors(response, sensors);

        List<SensorModel> states = new ArrayList<SensorModel>();
        for (SensorModel sensor : sensors) {
            if (sensor.get(SensorModel.TYPE).equals("2")) {
                states.add(sensor);
            }
        }

        getConnected(states, 0, callback);
    }

    private void getMyServicesFailure(AsyncCallback<List<TreeModel>> callback) {
        forwardToView(this.tree, new AppEvent(StateEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void getServiceMethods(List<SensorModel> states, int index,
            AsyncCallback<List<TreeModel>> callback) {

        if (index < states.size()) {
            SensorModel service = states.get(index);
            final ModelData child = service.getChild(0);

            if (child instanceof SensorModel) {
                // prepare request properties
                final String method = "GET";
                final String url = Constants.URL_SENSORS + "/" + child.<String> get("id")
                        + "/services/" + service.<String> get("id") + "/methods";
                final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
                final AppEvent onSuccess = new AppEvent(StateEvents.GetMethodsAjaxSuccess);
                onSuccess.setData("states", states);
                onSuccess.setData("index", index);
                onSuccess.setData("callback", callback);
                final AppEvent onFailure = new AppEvent(StateEvents.GetMethodsAjaxFailure);
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
                Log.w(TAG, "State child is not a SensorModel: " + child);
                getServiceMethodsFailure(callback);
            }

        } else {
            // completed the list of state sensors
            Registry.register(Constants.REG_SERVICES, states);

            forwardToView(this.tree, new AppEvent(StateEvents.Done));
            if (null != callback) {
                callback.onSuccess(new ArrayList<TreeModel>(states));
            }
        }
    }

    private void getServiceMethodsCallback(String response, List<SensorModel> states, int index,
            AsyncCallback<List<TreeModel>> callback) {
        List<ModelData> methods = parseServiceMethods(response);
        if (null != methods) {
            SensorModel state = states.get(index);
            state.set("methods", methods);
            index++;
            getServiceMethods(states, index, callback);
        } else {
            getServiceMethodsFailure(callback);
        }
    }

    private void getServiceMethodsFailure(AsyncCallback<List<TreeModel>> callback) {
        forwardToView(this.tree, new AppEvent(StateEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        /*
         * Get list of state and connected sensors
         */
        if (type.equals(StateEvents.ListRequested)) {
            // Log.d(TAG, "TreeRequested");
            AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData();
            getMyServices(callback);

        } else if (type.equals(StateEvents.AjaxSensorsSuccess)) {
            // Log.d(TAG, "AjaxSensorsSuccess");
            final String response = event.<String> getData("response");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getMyServicesCallback(response, callback);

        } else if (type.equals(StateEvents.AjaxSensorsFailure)) {
            Log.w(TAG, "AjaxSensorsFailure");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getMyServicesFailure(callback);

        } else if (type.equals(StateEvents.AjaxConnectedSuccess)) {
            // Log.d(TAG, "AjaxConnectedSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> states = event.<List<SensorModel>> getData("states");
            final int index = event.getData("index");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getConnectedCallback(response, states, index, callback);

        } else if (type.equals(StateEvents.AjaxConnectedFailure)) {
            Log.w(TAG, "AjaxConnectedFailure");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getConnectedFailure(callback);

        } else

        /**
         * Get list of methods for this state service
         */
        if (type.equals(StateEvents.GetMethodsAjaxSuccess)) {
            // Log.d(TAG, "AjaxGetMethodsSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> states = event.<List<SensorModel>> getData("states");
            final int index = event.getData("index");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getServiceMethodsCallback(response, states, index, callback);

        } else if (type.equals(StateEvents.GetMethodsAjaxFailure)) {
            Log.w(TAG, "AjaxGetMethodsFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<TreeModel>> callback = event
                    .<AsyncCallback<List<TreeModel>>> getData("callback");
            getServiceMethodsFailure(callback);

        } else

        /*
         * Disconnect a sensor from a state
         */
        if (type.equals(StateEvents.RemoveRequested)) {
            // Log.d(TAG, "RemoveRequested");
            TreeModel sensor = event.<TreeModel> getData("sensor");
            TreeModel service = event.<TreeModel> getData("service");
            disconnectService(sensor, service);

        } else if (type.equals(StateEvents.AjaxDisconnectFailure)) {
            Log.w(TAG, "AjaxDisconnectFailure");
            final int code = event.getData("code");
            disconnectServiceErrorCallback(code);

        } else if (type.equals(StateEvents.AjaxDisconnectSuccess)) {
            // Log.d(TAG, "AjaxDisconnectSuccess");
            final String response = event.<String> getData("response");
            disconnectServiceCallback(response);

        } else

        /*
         * Create default states, if available
         */
        if (type.equals(StateEvents.CheckDefaults)) {
            Log.d(TAG, "CheckDefaults");
            checkDefaults();

        } else if (type.equals(StateEvents.AjaxDefaultsSuccess)) {
            Log.d(TAG, "AjaxDefaultsSuccess");
            final String response = event.<String> getData("response");
            checkDefaultsCallback(response);

        } else if (type.equals(StateEvents.AjaxDefaultsFailure)) {
            Log.w(TAG, "AjaxDefaultsFailure");
            // final int code = event.getData("code");
            checkDefaultsFailure();
        } else

        /*
         * Pass on to state tree view
         */
        {
            forwardToView(this.tree, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.tree = new StateTree(this);
    }

    private List<ModelData> parseServiceMethods(String response) {
        if (response != null) {
            // try to get "methods" array
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonVal = json.get("methods");
            if (null != jsonVal) {
                JSONArray jsonArray = jsonVal.isArray();
                if (null != jsonArray) {
                    List<ModelData> methods = new ArrayList<ModelData>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject methodObj = jsonArray.get(i).isObject();

                        ModelData m = new BaseModelData();
                        m.set("name", methodObj.get("name").isString().stringValue());
                        m.set("return", methodObj.get("return value").isString().stringValue());
                        JSONArray params = methodObj.get("parameters").isArray();
                        List<String> paramsList = new ArrayList<String>();
                        for (int j = 0; j < params.size(); j++) {
                            paramsList.add(params.get(j).isString().stringValue());
                        }
                        m.set("parameters", paramsList);
                        methods.add(m);
                    }
                    return methods;
                } else {
                    Log.e(TAG,
                            "Error parsing service methods response: \"methods\" is not a JSON Array");
                }
            } else {
                Log.e(TAG, "Error parsing service methods response: \"methods\" is is not found");
            }
        } else {
            Log.e(TAG, "Error parsing service methods response: response=null");
        }
        return null;
    }
}
