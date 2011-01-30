package nl.sense_os.commonsense.client.controllers;

import java.util.List;

import nl.sense_os.commonsense.client.controllers.cors.StatesJsniRequests;
import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.services.TagsServiceAsync;
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

    public StateController() {
        // events to update the list of groups
        registerEventTypes(StateEvents.ListNotUpdated, StateEvents.ListRequested,
                StateEvents.ListUpdated, StateEvents.Working, StateEvents.ShowGrid);
        registerEventTypes(StateEvents.ShowCreator, StateEvents.CreateRequested,
                StateEvents.CreateComplete, StateEvents.CreateFailed, StateEvents.CreateCancelled);
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
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut);
    }

    private void connectService(AppEvent event) {
        TreeModel sensor = event.<TreeModel> getData("sensor");
        TreeModel service = event.<TreeModel> getData("service");
        String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String data = "{\"service\":{";
        data += "\"id\":\"" + service.<String> get("id") + "\"";
        data += "\"name\":\"" + service.<String> get("service_name") + "\"";
        data += "}}";
        StatesJsniRequests.connectService(url, sessionId, data, this);
    }

    public void connectServiceCallback() {
        Dispatcher.forwardEvent(StateEvents.ConnectComplete);
    }

    public void connectServiceErrorCallback() {
        Dispatcher.forwardEvent(StateEvents.ConnectFailed);
    }

    private void connectVirtualSensor(AppEvent event) {
        TreeModel sensor = event.<TreeModel> getData("sensor");
        TreeModel service = event.<TreeModel> getData("service");
        String url = Constants.URL_SENSORS + "/" + service.<String> get("id") + "/sensors";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String data = "{\"sensor\":{\"id\":\"" + sensor.<String> get("id") + "\"}}";

        StatesJsniRequests.connectVirtualSensor(url, sessionId, data, this);
    }

    public void connectVirtualSensorCallback() {
        Dispatcher.forwardEvent(StateEvents.ConnectComplete);
    }

    public void connectVirtualSensorErrorCallback() {
        Dispatcher.forwardEvent(StateEvents.ConnectFailed);
    }

    private void createSensor(AppEvent event) {
        TreeModel sensor = event.<TreeModel> getData("sensor");
        String name = sensor.<String> get("name");
        String url = Constants.URL_SENSORS;
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String data = "{\"sensor\":{";
        data += "\"name\":\"" + name + "\"";
        data += ",\"device_type\":\"" + name + "\"";
        data += ",\"pager_type\":\"" + "" + "\"";
        data += ",\"data_type\":\"" + "string" + "\"";
        data += "}}";

        StatesJsniRequests.createSensor(url, sessionId, data, this);
    }

    public void createSensorCallback() {
        Log.w(TAG, "createSensorCallback not implemented");
    }

    public void createSensorErrorCallback() {
        Log.w(TAG, "createSensorErrorCallback not implemented");
    }

    private void disconnectService(AppEvent event) {
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String sensorId = event.<String> getData("sensorId");
        String serviceId = event.<String> getData("serviceId");
        String url = Constants.URL_SENSORS + "/" + sensorId + "/services/" + serviceId;
        StatesJsniRequests.disconnectService(url, sessionId, this);
    }

    public void disconnectServiceCallback() {
        Dispatcher.forwardEvent(StateEvents.RemoveComplete);
    }

    public void disconnectServiceErrorCallback() {
        Dispatcher.forwardEvent(StateEvents.RemoveFailed);
    }

    private void getAvailableSensors(AppEvent event) {
        TreeModel serviceModel = event.<TreeModel> getData();
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(StateEvents.AvailableSensorsNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_SERVICES, result);
                Dispatcher.forwardEvent(StateEvents.AvailableSensorsUpdated, result);
            }
        };
        service.getAvailableSensors(sessionId, serviceModel, callback);
    }

    private void getAvailableServices(AppEvent event) {
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
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

    private void getMySensors(AppEvent event) {
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(StateEvents.ListNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_SERVICES, result);
                Dispatcher.forwardEvent(StateEvents.ListUpdated, result);
            }
        };
        service.getMyServices(sessionId, callback);
        Dispatcher.forwardEvent(StateEvents.Working);
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
            getMySensors(event);
        } else if (type.equals(StateEvents.AvailableSensorsRequested)) {
            Log.d(TAG, "AvailableSensorsRequested");
            getAvailableSensors(event);
        } else if (type.equals(StateEvents.CreateRequested)) {
            Log.d(TAG, "CreateRequested");
            createSensor(event);
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
        } else if (type.equals(StateEvents.ShowCreator) || type.equals(StateEvents.CreateComplete)
                || type.equals(StateEvents.CreateFailed)
                || type.equals(StateEvents.CreateCancelled)) {
            forwardToView(this.creator, event);
        } else if (type.equals(StateEvents.ShowSensorConnecter)
                || type.equals(StateEvents.ConnectComplete)
                || type.equals(StateEvents.ConnectFailed)
                || type.equals(StateEvents.AvailableSensorsUpdated)
                || type.equals(StateEvents.AvailableSensorsNotUpdated)) {
            forwardToView(this.connecter, event);
        } else if (type.equals(StateEvents.ShowEditor) || type.equals(StateEvents.MethodsUpdated)
                || type.equals(StateEvents.MethodsNotUpdated)
                || type.equals(StateEvents.InvokeMethodComplete)
                || type.equals(StateEvents.InvokeMethodFailed)) {
            forwardToView(this.editor, event);
        } else {
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
        Log.w(TAG, "invokeMethod is not implemented");
    }
}
