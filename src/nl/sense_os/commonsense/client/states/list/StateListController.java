package nl.sense_os.commonsense.client.states.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.client.sensors.library.GetSensorsResponseJso;
import nl.sense_os.commonsense.client.states.connect.StateConnectEvents;
import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.client.states.defaults.StateDefaultsEvents;
import nl.sense_os.commonsense.client.utility.TreeCopier;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateListController extends Controller {

    private static final Logger logger = Logger.getLogger("StateListController");
    private View tree;

    public StateListController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(VizEvents.Show);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(StateListEvents.ShowGrid);

        // external triggers to initiate a list update
        registerEventTypes(StateCreateEvents.CreateServiceComplete,
                StateConnectEvents.ConnectSuccess, SensorDeleteEvents.DeleteSuccess,
                StateDefaultsEvents.CheckDefaultsSuccess);

        // events to update the list of groups
        registerEventTypes(StateListEvents.LoadRequest, StateListEvents.AjaxStateSensorsSuccess,
                StateListEvents.AjaxStateSensorsFailure, StateListEvents.ConnectedAjaxSuccess,
                StateListEvents.ConnectedAjaxFailure, StateListEvents.GetMethodsAjaxSuccess,
                StateListEvents.GetMethodsAjaxFailure);

        registerEventTypes(StateListEvents.RemoveRequested, StateListEvents.AjaxDisconnectFailure,
                StateListEvents.AjaxDisconnectSuccess, StateListEvents.RemoveComplete);
    }

    private void disconnectService(SensorModel sensor, SensorModel stateSensor) {

        // prepare request data
        final String method = "DELETE";
        final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/" + stateSensor.getId();
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateListEvents.AjaxDisconnectSuccess);
        final AppEvent onFailure = new AppEvent(StateListEvents.AjaxDisconnectFailure);

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
        Dispatcher.forwardEvent(StateListEvents.RemoveComplete);
    }

    private void disconnectServiceErrorCallback(int code) {
        forwardToView(this.tree, new AppEvent(StateListEvents.RemoveFailed));
    }

    private void getConnected(SensorModel state, AsyncCallback<List<SensorModel>> callback) {

        // prepare request properties
        final String method = "GET";
        final String url = Urls.SENSORS + "/" + state.getId() + "/sensors";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateListEvents.ConnectedAjaxSuccess);
        onSuccess.setData("state", state);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(StateListEvents.ConnectedAjaxFailure);
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

    private void getMethods(SensorModel state, List<SensorModel> sensors,
            AsyncCallback<List<SensorModel>> callback) {

        if (sensors.size() > 0) {
            // prepare request properties
            final String method = "GET";
            final String url = Urls.SENSORS + "/" + sensors.get(0).getId() + "/services/"
                    + state.getId() + "/methods";
            final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(StateListEvents.GetMethodsAjaxSuccess);
            onSuccess.setData("state", state);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("callback", callback);
            final AppEvent onFailure = new AppEvent(StateListEvents.GetMethodsAjaxFailure);
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
            logger.warning("State \'" + state + "\' has no connected sensors!");
            onLoadComplete(sensors, callback);
        }
    }

    private void getStateSensors(final AsyncCallback<List<SensorModel>> callback) {

        // prepare request properties
        final String method = "GET";
        final String url = Urls.SENSORS + "?per_page=1000&details=full";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateListEvents.AjaxStateSensorsSuccess);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(StateListEvents.AjaxStateSensorsFailure);
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

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        /*
         * Get list of states
         */
        if (type.equals(StateListEvents.LoadRequest)) {
            // logger.fine( "LoadRequest");
            final Object loadConfig = event.getData("loadConfig");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            load(loadConfig, callback);

        } else if (type.equals(StateListEvents.AjaxStateSensorsSuccess)) {
            // logger.fine( "AjaxStateSensorsSuccess");
            final String response = event.<String> getData("response");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            onStateSensorsSuccess(response, callback);

        } else if (type.equals(StateListEvents.AjaxStateSensorsFailure)) {
            logger.warning("AjaxStateSensorsFailure");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            onStateSensorsFailure(callback);

        } else if (type.equals(StateListEvents.ConnectedAjaxSuccess)) {
            // logger.fine( "ConnectedAjaxSuccess");
            final String response = event.<String> getData("response");
            final SensorModel state = event.<SensorModel> getData("state");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            onConnectedSuccess(response, state, callback);

        } else if (type.equals(StateListEvents.ConnectedAjaxFailure)) {
            logger.warning("ConnectedAjaxFailure");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            onConnectedFailure(callback);

        } else if (type.equals(StateListEvents.GetMethodsAjaxSuccess)) {
            // logger.fine( "AjaxGetMethodsSuccess");
            final String response = event.<String> getData("response");
            final SensorModel state = event.<SensorModel> getData("state");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            onMethodsSuccess(response, state, sensors, callback);

        } else if (type.equals(StateListEvents.GetMethodsAjaxFailure)) {
            logger.warning("AjaxGetMethodsFailure");
            // final int code = event.getData("code");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            onMethodsFailure(callback);

        } else

        /*
         * Disconnect a sensor from a state
         */
        if (type.equals(StateListEvents.RemoveRequested)) {
            // logger.fine( "RemoveRequested");
            SensorModel sensor = event.<SensorModel> getData("sensor");
            SensorModel stateSensor = event.<SensorModel> getData("stateSensor");
            disconnectService(sensor, stateSensor);

        } else if (type.equals(StateListEvents.AjaxDisconnectFailure)) {
            logger.warning("AjaxDisconnectFailure");
            final int code = event.getData("code");
            disconnectServiceErrorCallback(code);

        } else if (type.equals(StateListEvents.AjaxDisconnectSuccess)) {
            // logger.fine( "AjaxDisconnectSuccess");
            final String response = event.<String> getData("response");
            disconnectServiceCallback(response);

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
        this.tree = new StateGrid(this);
    }

    private void onConnectedFailure(AsyncCallback<List<SensorModel>> callback) {
        forwardToView(this.tree, new AppEvent(StateListEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onConnectedSuccess(String response, SensorModel state,
            AsyncCallback<List<SensorModel>> callback) {

        // parse list of sensors from response
        List<SensorModel> sensors = new ArrayList<SensorModel>();
        GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
        sensors.addAll(responseJso.getSensors());

        // get details from library
        List<SensorModel> result = new ArrayList<SensorModel>();
        List<SensorModel> library = Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST);
        for (SensorModel sensor : sensors) {
            int index = library.indexOf(sensor);
            if (index != -1) {
                SensorModel detailed = (SensorModel) TreeCopier.copySensor(library.get(index));
                state.add(detailed);
                result.add(detailed);
            } else {
                sensor.setParent(state);
                result.add(sensor);
            }
        }

        getMethods(state, result, callback);
    }

    private void onMethodsFailure(AsyncCallback<List<SensorModel>> callback) {
        forwardToView(this.tree, new AppEvent(StateListEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onMethodsSuccess(String response, SensorModel state, List<SensorModel> sensors,
            AsyncCallback<List<SensorModel>> callback) {
        List<ModelData> methods = parseServiceMethods(response);
        if (null != methods) {
            state.set("methods", methods);
            onLoadComplete(sensors, callback);
        } else {
            onMethodsFailure(callback);
        }
    }

    private void onStateSensorsFailure(AsyncCallback<List<SensorModel>> callback) {
        onLoadFailure(callback);
    }

    private void onStateSensorsSuccess(String response, AsyncCallback<List<SensorModel>> callback) {

        // parse list of sensors from response
        List<SensorModel> sensors = new ArrayList<SensorModel>();
        GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
        sensors.addAll(responseJso.getSensors());

        UserModel user = Registry.<UserModel> get(Constants.REG_USER);
        List<SensorModel> states = new ArrayList<SensorModel>();
        for (SensorModel sensor : sensors) {
            if (sensor.getType() == 2 && user.equals(sensor.getOwner())) {
                states.add(sensor);
            }
        }

        onLoadComplete(states, callback);
    }

    private void load(Object loadConfig, AsyncCallback<List<SensorModel>> callback) {
        forwardToView(this.tree, new AppEvent(StateListEvents.Working));
        if (null == loadConfig) {
            getStateSensors(callback);
        } else if (loadConfig instanceof SensorModel && ((SensorModel) loadConfig).getType() == 2) {
            getConnected((SensorModel) loadConfig, callback);
        } else {
            onLoadComplete(new ArrayList<SensorModel>(), callback);
        }
    }

    private void onLoadComplete(List<SensorModel> result, AsyncCallback<List<SensorModel>> callback) {
        forwardToView(this.tree, new AppEvent(StateListEvents.Done));
        forwardToView(this.tree, new AppEvent(StateListEvents.LoadComplete));
        if (null != callback) {
            callback.onSuccess(result);
        }
    }

    private void onLoadFailure(AsyncCallback<List<SensorModel>> callback) {
        forwardToView(this.tree, new AppEvent(StateListEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
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
                    logger.severe("Error parsing service methods response: \"methods\" is not a JSON Array");
                }
            } else {
                logger.severe("Error parsing service methods response: \"methods\" is is not found");
            }
        } else {
            logger.severe("Error parsing service methods response: response=null");
        }
        return null;
    }
}
