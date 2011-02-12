package nl.sense_os.commonsense.client.controllers;

import java.util.List;

import nl.sense_os.commonsense.client.controllers.cors.StatesJsniRequests;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.services.SensorsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.StateCreator;
import nl.sense_os.commonsense.client.views.StateEditor;
import nl.sense_os.commonsense.client.views.StateGrid;
import nl.sense_os.commonsense.client.views.StateSensorConnecter;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateController extends Controller {

    private static final String TAG = "StateController";
    private View grid;
    private View creator;
    private View connecter;
    private View editor;
    private boolean isGettingMyServices;

    public StateController() {
        // events to update the list of groups
        registerEventTypes(StateEvents.ListRequested, StateEvents.Done, StateEvents.Working,
                StateEvents.ShowGrid);
        registerEventTypes(StateEvents.ShowCreator, StateEvents.CreateServiceRequested,
                StateEvents.CreateServiceComplete, StateEvents.CreateServiceFailed,
                StateEvents.CreateServiceCancelled);
        registerEventTypes(StateEvents.RemoveRequested, StateEvents.RemoveComplete,
                StateEvents.RemoveFailed);
        registerEventTypes(StateEvents.ShowSensorConnecter, StateEvents.ConnectComplete,
                StateEvents.ConnectFailed, StateEvents.ConnectRequested,
                StateEvents.AvailableSensorsRequested, StateEvents.AvailableSensorsNotUpdated,
                StateEvents.AvailableSensorsUpdated);
        registerEventTypes(StateEvents.ShowEditor, StateEvents.MethodsRequested,
                StateEvents.MethodsUpdated, StateEvents.MethodsNotUpdated,
                StateEvents.InvokeMethodRequested, StateEvents.InvokeMethodComplete,
                StateEvents.InvokeMethodFailed);
        registerEventTypes(MainEvents.ShowVisualization);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    private void connectService(AppEvent event) {
        TreeModel sensor = event.<TreeModel> getData("sensor");
        TreeModel service = event.<TreeModel> getData("service");
        String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String data = "{\"service\":{";
        data += "\"id\":\"" + service.<String> get("id") + "\"";
        data += ",\"name\":\"" + service.<String> get("service_name") + "\"";
        data += "}}";
        StatesJsniRequests.connectService(url, sessionId, data, this);
    }

    public void connectServiceCallback() {
        Dispatcher.forwardEvent(StateEvents.ConnectComplete);
        // TODO update list
    }

    public void connectServiceErrorCallback() {
        Dispatcher.forwardEvent(StateEvents.ConnectFailed);
    }

    private void createService(AppEvent event) {
        String stateName = event.<String> getData("name");
        TreeModel service = event.<TreeModel> getData("service");
        ModelData sensor = event.<ModelData> getData("sensor");
        List<ModelData> dataFields = event.<List<ModelData>> getData("dataFields");
        String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // create POST data
        String serviceJson = "\"service\":{";
        serviceJson += "\"name\":\"" + service.<String> get("service_name") + "\"";
        serviceJson += ",\"data_fields\":[";
        for (ModelData dataField : dataFields) {
            serviceJson += "\"" + dataField.get("text") + "\",";
        }
        serviceJson = serviceJson.substring(0, serviceJson.length() - 1) + "]";
        serviceJson += "}";
        String sensorJson = "\"sensor\":{";
        sensorJson += "\"name\":\"" + stateName + "\"";
        sensorJson += ",\"device_type\":\"" + stateName + "\"";
        sensorJson += "}";
        String data = "{" + serviceJson + "," + sensorJson + "}";

        Log.d(TAG, data);
        StatesJsniRequests.createService(url, sessionId, data, this);
    }

    public void createServiceCallback() {
        Dispatcher.forwardEvent(StateEvents.CreateServiceComplete);
        // TODO update list
    }

    public void createServiceErrorCallback() {
        Dispatcher.forwardEvent(StateEvents.CreateServiceFailed);
    }

    private void disconnectService(AppEvent event) {
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        TreeModel sensor = event.<TreeModel> getData("sensor");
        TreeModel service = event.<TreeModel> getData("service");
        String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services/"
                + service.<String> get("id");
        StatesJsniRequests.disconnectService(url, sessionId, this);
    }

    public void disconnectServiceCallback() {
        Dispatcher.forwardEvent(StateEvents.RemoveComplete);
    }

    public void disconnectServiceErrorCallback() {
        Dispatcher.forwardEvent(StateEvents.RemoveFailed);
    }

    private void getAvailableSensors(AppEvent event) {
        TreeModel serviceModel = event.<TreeModel> getData("service");
        final AsyncCallback<List<TreeModel>> proxyCallback = event
                .<AsyncCallback<List<TreeModel>>> getData("callback");

        SensorsServiceAsync service = Registry.<SensorsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                proxyCallback.onFailure(caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_SERVICES, result);
                proxyCallback.onSuccess(result);
            }
        };
        service.getAvailableSensors(sessionId, serviceModel, callback);
    }

    @SuppressWarnings("unused")
    private void getAvailableServices(AppEvent event) {
        SensorsServiceAsync service = Registry.<SensorsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(StateEvents.AvailableServicesNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_SERVICES, result);
                Dispatcher.forwardEvent(StateEvents.AvailableServicesUpdated, result);
            }
        };
        service.getAvailableServices(sessionId, callback);
    }

    private void getMyServices(AppEvent event) {
        if (false == isGettingMyServices) {
            this.isGettingMyServices = true;
            Dispatcher.forwardEvent(StateEvents.Working);

            final AsyncCallback<List<TreeModel>> proxyCallback = event.getData();
            SensorsServiceAsync service = Registry
                    .<SensorsServiceAsync> get(Constants.REG_TAGS_SVC);
            String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
            AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Dispatcher.forwardEvent(StateEvents.Done);
                    isGettingMyServices = false;
                    proxyCallback.onFailure(caught);
                }

                @Override
                public void onSuccess(List<TreeModel> result) {
                    Registry.register(Constants.REG_SERVICES, result);
                    Dispatcher.forwardEvent(StateEvents.Done);
                    isGettingMyServices = false;
                    proxyCallback.onSuccess(result);
                }
            };
            service.getMyServices(sessionId, callback);
        } else {
            Log.d(TAG, "Ignored request to get my services: already working on an earlier request");
            final AsyncCallback<List<TreeModel>> proxyCallback = event.getData();
            proxyCallback.onFailure(null);
        }
    }

    private void getServiceMethods(AppEvent event) {
        TreeModel service = event.<TreeModel> getData();
        ModelData child = service.getChild(0);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String url = Constants.URL_SENSORS + "/" + child.<String> get("id") + "/services/"
                + service.<String> get("id") + "/methods";
        StatesJsniRequests.getServiceMethods(url, sessionId, this);
    }

    public void getServiceMethodsCallback(List<ModelData> methods) {
        Dispatcher.forwardEvent(StateEvents.MethodsUpdated, methods);
    }

    public void getServiceMethodsErrorCallback() {
        Dispatcher.forwardEvent(StateEvents.MethodsNotUpdated);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            getMyServices(event);
        } else if (type.equals(StateEvents.AvailableSensorsRequested)) {
            Log.d(TAG, "AvailableSensorsRequested");
            getAvailableSensors(event);
        } else if (type.equals(StateEvents.CreateServiceRequested)) {
            Log.d(TAG, "CreateRequested");
            createService(event);
        } else if (type.equals(StateEvents.RemoveRequested)) {
            Log.d(TAG, "RemoveRequested");
            disconnectService(event);
        } else if (type.equals(StateEvents.ConnectRequested)) {
            Log.d(TAG, "ConnectRequested");
            connectService(event);
        } else if (type.equals(StateEvents.MethodsRequested)) {
            Log.d(TAG, "MethodsRequested");
            getServiceMethods(event);
        } else if (type.equals(StateEvents.InvokeMethodRequested)) {
            Log.d(TAG, "InvokeMethodRequested");
            invokeMethod(event);
        }

        if (type.equals(StateEvents.ShowCreator) || type.equals(StateEvents.CreateServiceComplete)
                || type.equals(StateEvents.CreateServiceFailed)
                || type.equals(StateEvents.CreateServiceCancelled)) {
            forwardToView(this.creator, event);
        }

        if (type.equals(StateEvents.ShowSensorConnecter)
                || type.equals(StateEvents.ConnectComplete)
                || type.equals(StateEvents.ConnectFailed)
                || type.equals(StateEvents.AvailableSensorsUpdated)
                || type.equals(StateEvents.AvailableSensorsNotUpdated)) {
            forwardToView(this.connecter, event);
        }

        if (type.equals(StateEvents.ShowEditor) || type.equals(StateEvents.MethodsUpdated)
                || type.equals(StateEvents.MethodsNotUpdated)
                || type.equals(StateEvents.InvokeMethodComplete)
                || type.equals(StateEvents.InvokeMethodFailed)) {
            forwardToView(this.editor, event);
        }

        if (type.equals(StateEvents.ShowGrid) || type.equals(StateEvents.Done)
                || type.equals(StateEvents.RemoveComplete) || type.equals(StateEvents.RemoveFailed)
                || type.equals(StateEvents.Working) || type.equals(MainEvents.ShowVisualization)
                || type.equals(StateEvents.ConnectComplete)
                || type.equals(StateEvents.CreateServiceComplete)
                || type.equals(LoginEvents.LoggedOut)) {
            forwardToView(this.grid, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.grid = new StateGrid(this);
        this.creator = new StateCreator(this);
        this.connecter = new StateSensorConnecter(this);
        this.editor = new StateEditor(this);
    }

    private void invokeMethod(AppEvent event) {
        TreeModel service = event.<TreeModel> getData("service");
        ModelData sensor = service.getChild(0);
        ModelData method = event.<ModelData> getData("method");
        String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services/"
                + service.<String> get("id") + "/" + method.<String> get("name");

        // create data
        List<String> params = event.<List<String>> getData("parameters");
        String data = null;
        if (params.size() > 0) {
            data = "{\"parameters\":[";
            for (String p : params) {
                data += "\"" + p + "\",";
            }
            data = data.substring(0, data.length() - 1);
            data += "]}";
        }

        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        if (null == data) {
            StatesJsniRequests.invokeGetMethod(url, sessionId, this);
        } else {
            StatesJsniRequests.invokePostMethod(url, sessionId, data, this);
        }
    }

    public void invokeMethodCallback(String result) {
        Dispatcher.forwardEvent(StateEvents.InvokeMethodComplete, result);
    }

    public void invokeMethodErrorCallback() {
        Dispatcher.forwardEvent(StateEvents.InvokeMethodFailed);
    }
}
