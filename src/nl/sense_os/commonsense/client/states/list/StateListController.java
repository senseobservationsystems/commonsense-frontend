package nl.sense_os.commonsense.client.states.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.ServiceMethodModel;
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
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateListController extends Controller {

    private static final Logger LOG = Logger.getLogger(StateListController.class.getName());
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
        registerEventTypes(StateListEvents.LoadRequest);

        registerEventTypes(StateListEvents.RemoveRequested, StateListEvents.RemoveComplete);
    }

    private void disconnectService(SensorModel sensor, SensorModel stateSensor) {

        // prepare request data
        final Method method = RequestBuilder.DELETE;
        final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/" + stateSensor.getId()
                + ".json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("DELETE service onError callback: " + exception.getMessage());
                onDisconnectFailure(0);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("DELETE service response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onDisconnectSuccess(response.getText());
                } else {
                    LOG.warning("DELETE service returned incorrect status: " + statusCode);
                    onDisconnectFailure(statusCode);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(method, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(null, reqCallback);
        } catch (RequestException e) {
            LOG.warning("DELETE service request threw exception: " + e.getMessage());
            onDisconnectFailure(0);
        }
    }

    private void getConnected(final SensorModel state,
            final AsyncCallback<List<SensorModel>> callback) {

        // prepare request properties
        final Method method = RequestBuilder.GET;
        final String url = Urls.SENSORS + "/" + state.getId() + "/sensors.json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("GET service sensors onError callback: " + exception.getMessage());
                onConnectedFailure(callback);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("GET service sensors response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onConnectedSuccess(response.getText(), state, callback);
                } else {
                    LOG.warning("GET service sensors returned incorrect status: " + statusCode);
                    onConnectedFailure(callback);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(method, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(null, reqCallback);
        } catch (RequestException e) {
            LOG.warning("GET service sensors request threw exception: " + e.getMessage());
            onConnectedFailure(callback);
        }
    }

    private void getMethods(final SensorModel state, final List<SensorModel> sensors,
            final AsyncCallback<List<SensorModel>> callback) {

        if (sensors.size() > 0) {
            // prepare request properties
            final Method method = RequestBuilder.GET;
            final String url = Urls.SENSORS + "/" + sensors.get(0).getId() + "/services/"
                    + state.getId() + "/methods.json";
            final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("GET service methods onError callback: " + exception.getMessage());
                    onMethodsFailure(callback);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("GET service methods response received: " + response.getStatusText());
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onMethodsSuccess(response.getText(), state, sensors, callback);
                    } else {
                        LOG.warning("GET service methods returned incorrect status: " + statusCode);
                        onMethodsFailure(callback);
                    }
                }
            };

            // send request
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("X-SESSION_ID", sessionId);
            try {
                builder.sendRequest(null, reqCallback);
            } catch (RequestException e) {
                LOG.warning("GET service methods request threw exception: " + e.getMessage());
                onMethodsFailure(callback);
            }

        } else {
            LOG.warning("State \'" + state + "\' has no connected sensors!");
            onLoadComplete(sensors, callback);
        }
    }

    private void getStateSensors(final AsyncCallback<List<SensorModel>> callback) {

        // prepare request properties
        final Method method = RequestBuilder.GET;
        final String url = Urls.SENSORS + ".json" + "?per_page=1000&details=full";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("GET sensors onError callback: " + exception.getMessage());
                onStateSensorsFailure(callback);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("GET sensors response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onStateSensorsSuccess(response.getText(), callback);
                } else {
                    LOG.warning("GET sensors returned incorrect status: " + statusCode);
                    onStateSensorsFailure(callback);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(method, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(null, reqCallback);
        } catch (RequestException e) {
            LOG.warning("GET sensors request threw exception: " + e.getMessage());
            onStateSensorsFailure(callback);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        /*
         * Get list of states
         */
        if (type.equals(StateListEvents.LoadRequest)) {
            // LOG.fine( "LoadRequest");
            final Object loadConfig = event.getData("loadConfig");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            load(loadConfig, callback);

        } else

        /*
         * Disconnect a sensor from a state
         */
        if (type.equals(StateListEvents.RemoveRequested)) {
            // LOG.fine( "RemoveRequested");
            SensorModel sensor = event.<SensorModel> getData("sensor");
            SensorModel stateSensor = event.<SensorModel> getData("stateSensor");
            disconnectService(sensor, stateSensor);

        } else

        /*
         * Pass on to state tree view
         */
        {
            forwardToView(tree, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        tree = new StateGrid(this);
    }

    private void load(Object loadConfig, AsyncCallback<List<SensorModel>> callback) {
        forwardToView(tree, new AppEvent(StateListEvents.Working));
        if (null == loadConfig) {
            getStateSensors(callback);
        } else if (loadConfig instanceof SensorModel && ((SensorModel) loadConfig).getType() == 2) {
            getConnected((SensorModel) loadConfig, callback);
        } else {
            onLoadComplete(new ArrayList<SensorModel>(), callback);
        }
    }

    private void onConnectedFailure(AsyncCallback<List<SensorModel>> callback) {
        forwardToView(tree, new AppEvent(StateListEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onConnectedSuccess(String response, SensorModel state,
            AsyncCallback<List<SensorModel>> callback) {

        // parse list of sensors from response
        List<SensorModel> sensors = new ArrayList<SensorModel>();
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
            sensors.addAll(responseJso.getSensors());
        }

        // get details from library
        List<SensorModel> result = new ArrayList<SensorModel>();
        List<SensorModel> library = Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST);
        for (SensorModel sensor : sensors) {
            int index = -1;
            for (SensorModel libSensor : library) {
                if (libSensor.getId() == sensor.getId()) {
                    index = library.indexOf(libSensor);
                    break;
                }
            }
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

    private void onDisconnectFailure(int code) {
        forwardToView(tree, new AppEvent(StateListEvents.RemoveFailed));
    }

    private void onDisconnectSuccess(String response) {
        Dispatcher.forwardEvent(StateListEvents.RemoveComplete);
    }

    private void onLoadComplete(List<SensorModel> result, AsyncCallback<List<SensorModel>> callback) {
        forwardToView(tree, new AppEvent(StateListEvents.Done));
        forwardToView(tree, new AppEvent(StateListEvents.LoadComplete));
        if (null != callback) {
            callback.onSuccess(result);
        }
    }

    private void onLoadFailure(AsyncCallback<List<SensorModel>> callback) {
        forwardToView(tree, new AppEvent(StateListEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onMethodsFailure(AsyncCallback<List<SensorModel>> callback) {
        forwardToView(tree, new AppEvent(StateListEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onMethodsSuccess(String response, SensorModel state, List<SensorModel> sensors,
            AsyncCallback<List<SensorModel>> callback) {

        // parse list of methods from the response
        List<ServiceMethodModel> methods = new ArrayList<ServiceMethodModel>();
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetMethodsResponseJso jso = JsonUtils.unsafeEval(response);
            methods = jso.getMethods();
        }

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
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
            sensors.addAll(responseJso.getSensors());
        }

        UserModel user = Registry.<UserModel> get(Constants.REG_USER);
        List<SensorModel> states = new ArrayList<SensorModel>();
        for (SensorModel sensor : sensors) {
            if (sensor.getType() == 2 && user.equals(sensor.getOwner())) {
                states.add(sensor);
            }
        }

        onLoadComplete(states, callback);
    }
}
