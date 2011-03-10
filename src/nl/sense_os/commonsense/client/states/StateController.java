package nl.sense_os.commonsense.client.states;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.group.GroupSensorsEvents;
import nl.sense_os.commonsense.client.sensors.personal.MySensorsEvents;
import nl.sense_os.commonsense.client.services.SensorsProxyAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.ServiceModel;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
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
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateController extends Controller {

    private static final String TAG = "StateController";
    private View grid;
    private View creator;
    private View connecter;
    private View editor;
    private boolean isGettingMyServices;
    private AsyncCallback<List<TreeModel>> getMyServicesCallback;
    private boolean isLoadingSensors;

    public StateController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(VizEvents.Show);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(StateEvents.ShowGrid);
        registerEventTypes(MySensorsEvents.ListUpdated);
        registerEventTypes(GroupSensorsEvents.ListUpdated);

        // events to update the list of groups
        registerEventTypes(StateEvents.ListRequested, StateEvents.Done, StateEvents.Working);
        registerEventTypes(StateEvents.ShowCreator, StateEvents.CreateServiceRequested,
                StateEvents.CreateServiceComplete, StateEvents.CreateServiceFailed,
                StateEvents.CreateServiceCancelled);
        registerEventTypes(StateEvents.RemoveRequested, StateEvents.RemoveComplete,
                StateEvents.RemoveFailed);
        registerEventTypes(StateEvents.LoadSensors);
        registerEventTypes(StateEvents.AvailableServicesRequested,
                StateEvents.AjaxAvailableServiceSuccess, StateEvents.AjaxAvailableServiceFailure);
        registerEventTypes(StateEvents.ShowSensorConnecter, StateEvents.ConnectComplete,
                StateEvents.ConnectFailed, StateEvents.ConnectRequested,
                StateEvents.AvailableSensorsRequested, StateEvents.AvailableSensorsNotUpdated,
                StateEvents.AvailableSensorsUpdated);
        registerEventTypes(StateEvents.ShowEditor, StateEvents.MethodsRequested,
                StateEvents.MethodsUpdated, StateEvents.MethodsNotUpdated,
                StateEvents.InvokeMethodRequested, StateEvents.InvokeMethodComplete,
                StateEvents.InvokeMethodFailed);

        // Ajax-related event types
        registerEventTypes(StateEvents.AjaxConnectFailure, StateEvents.AjaxConnectSuccess,
                StateEvents.AjaxCreateFailure, StateEvents.AjaxCreateSuccess,
                StateEvents.AjaxDisconnectFailure, StateEvents.AjaxDisconnectSuccess,
                StateEvents.AjaxGetMethodsFailure, StateEvents.AjaxGetMethodsSuccess,
                StateEvents.AjaxMethodFailure, StateEvents.AjaxMethodSuccess);
    }

    private void connectService(TreeModel sensor, TreeModel service) {

        // prepare request properties
        final String method = "POST";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id")
                + "/services.json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEvents.AjaxConnectSuccess);
        final AppEvent onFailure = new AppEvent(StateEvents.AjaxConnectFailure);

        // prepare request body
        String body = "{\"service\":{";
        body += "\"id\":\"" + service.<String> get("id") + "\"";
        body += ",\"name\":\"" + service.<String> get("service_name") + "\"";
        body += "}}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    public void connectServiceCallback(String response) {
        forwardToView(this.connecter, new AppEvent(StateEvents.ConnectComplete));
        forwardToView(this.grid, new AppEvent(StateEvents.ConnectComplete));
    }

    public void connectServiceErrorCallback(int code) {
        forwardToView(this.connecter, new AppEvent(StateEvents.ConnectFailed));
    }

    private void createService(String name, TreeModel service, ModelData sensor,
            List<ModelData> dataFields) {

        // prepare request properties
        final String method = "POST";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id")
                + "/services.json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEvents.AjaxCreateSuccess);
        final AppEvent onFailure = new AppEvent(StateEvents.AjaxCreateFailure);

        // create request body
        String serviceJson = "\"service\":{";
        serviceJson += "\"name\":\"" + service.<String> get(ServiceModel.NAME) + "\"";
        serviceJson += ",\"data_fields\":[";
        for (ModelData dataField : dataFields) {
            serviceJson += "\"" + dataField.get("text") + "\",";
        }
        serviceJson = serviceJson.substring(0, serviceJson.length() - 1) + "]";
        serviceJson += "}";
        String sensorJson = "\"sensor\":{";
        sensorJson += "\"name\":\"" + name + "\"";
        sensorJson += ",\"device_type\":\"" + name + "\"";
        sensorJson += "}";
        final String body = "{" + serviceJson + "," + sensorJson + "}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    public void createServiceCallback(String response) {
        forwardToView(this.creator, new AppEvent(StateEvents.CreateServiceComplete));
        forwardToView(this.grid, new AppEvent(StateEvents.CreateServiceComplete));
    }

    public void createServiceErrorCallback(int code) {
        forwardToView(this.creator, new AppEvent(StateEvents.CreateServiceFailed));
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

    public void disconnectServiceCallback(String response) {
        forwardToView(this.grid, new AppEvent(StateEvents.RemoveComplete));
    }

    public void disconnectServiceErrorCallback(int code) {
        forwardToView(this.grid, new AppEvent(StateEvents.RemoveFailed));
    }

    private void getAvailableSensors(AppEvent event) {
        TreeModel serviceModel = event.<TreeModel> getData("service");
        final AsyncCallback<List<TreeModel>> proxyCallback = event
                .<AsyncCallback<List<TreeModel>>> getData("callback");

        SensorsProxyAsync service = Registry.<SensorsProxyAsync> get(Constants.REG_TAGS_SVC);
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

    private void getAvailableServices(SensorModel sensor) {

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id")
                + "/services/available";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEvents.AjaxAvailableServiceSuccess);
        final AppEvent onFailure = new AppEvent(StateEvents.AjaxAvailableServiceFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void getAvailableServicesCallback(String response) {

        if (response != null) {
            // try to get "methods" array
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonVal = json.get("available_services");
            if (null != jsonVal) {
                JSONArray services = jsonVal.isArray();
                if (null != services) {

                    List<ServiceModel> result = new ArrayList<ServiceModel>();
                    for (int i = 0; i < services.size(); i++) {
                        JSONObject serviceJson = services.get(i).isObject();
                        if (serviceJson != null) {
                            String name = serviceJson.get(ServiceModel.NAME).isString()
                                    .stringValue();
                            JSONArray dataFieldsJson = serviceJson.get(ServiceModel.DATA_FIELDS)
                                    .isArray();
                            List<String> dataFields = new ArrayList<String>();
                            for (int j = 0; j < dataFieldsJson.size(); j++) {
                                String field = dataFieldsJson.get(j).isString().stringValue();
                                dataFields.add(field);
                            }

                            ServiceModel service = new ServiceModel();
                            service.set(ServiceModel.NAME, name);
                            service.set(ServiceModel.DATA_FIELDS, dataFields);
                            result.add(service);
                        }
                    }

                    AppEvent success = new AppEvent(StateEvents.AvailableServicesUpdated);
                    success.setData("services", result);
                    forwardToView(this.creator, success);

                } else {
                    Log.e(TAG,
                            "Error parsing service methods response: \"available_services\" is not a JSON Array");
                    getAvailableServicesError();
                }
            } else {
                Log.e(TAG,
                        "Error parsing service methods response: \"available_services\" is is not found");
                getAvailableServicesError();
            }
        } else {
            Log.e(TAG, "Error parsing service methods response: response=null");
            getAvailableServicesError();
            getAvailableServicesError();
        }
    }

    private void getAvailableServicesError() {
        forwardToView(this.creator, new AppEvent(StateEvents.AvailableServicesNotUpdated));
    }

    private void getMyServices(final AsyncCallback<List<TreeModel>> proxyCallback) {
        
        if (null == this.getMyServicesCallback) {
            this.getMyServicesCallback = proxyCallback;
        }
        
        if (false == isGettingMyServices) {
            this.isGettingMyServices = true;
            Dispatcher.forwardEvent(StateEvents.Working);
           
            SensorsProxyAsync service = Registry.<SensorsProxyAsync> get(Constants.REG_TAGS_SVC);
            String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
            AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Dispatcher.forwardEvent(StateEvents.Done);
                    isGettingMyServices = false;
                    if (null != getMyServicesCallback) {
                        getMyServicesCallback.onFailure(caught);
                    }
                }

                @Override
                public void onSuccess(List<TreeModel> result) {
                    Registry.register(Constants.REG_SERVICES, result);
                    Dispatcher.forwardEvent(StateEvents.Done);
                    isGettingMyServices = false;
                    if (null != getMyServicesCallback) {
                        getMyServicesCallback.onSuccess(result);
                    }
                }
            };
            service.getMyServices(sessionId, callback);
        } else {
            Log.d(TAG, "Ignored request to get my services: already working on an earlier request");
        }
    }

    private void getServiceMethods(TreeModel service) {

        final ModelData child = service.getChild(0);

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "/" + child.<String> get("id") + "/services/"
                + service.<String> get("id") + "/methods";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEvents.AjaxGetMethodsSuccess);
        final AppEvent onFailure = new AppEvent(StateEvents.AjaxGetMethodsFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Log.d(TAG, "req methods: " + url);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    public void getServiceMethodsCallback(List<ModelData> methods) {
        forwardToView(this.editor, new AppEvent(StateEvents.MethodsUpdated, methods));
    }

    public void getServiceMethodsErrorCallback(int code) {
        forwardToView(this.editor, new AppEvent(StateEvents.MethodsNotUpdated));
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(StateEvents.ListRequested)) {
            // Log.d(TAG, "ListRequested");
            AsyncCallback<List<TreeModel>> callback = event.<AsyncCallback<List<TreeModel>>> getData();
            getMyServices(callback);

        } else if (type.equals(StateEvents.LoadSensors)) {
            // Log.d(TAG, "LoadSensors");
            loadSensors();

        } else if (isLoadingSensors
                && (type.equals(MySensorsEvents.ListUpdated) || type
                        .equals(GroupSensorsEvents.ListUpdated))) {
            // Log.d(TAG, "LoadSensors");
            loadSensors();

        } else if (type.equals(StateEvents.AvailableServicesRequested)) {
            // Log.d(TAG, "AvailableServicesRequested");
            final SensorModel sensor = event.getData("sensor");
            getAvailableServices(sensor);

        } else if (type.equals(StateEvents.AjaxAvailableServiceSuccess)) {
            // Log.d(TAG, "AjaxAvailableServiceSuccess");
            final String response = event.getData("response");
            getAvailableServicesCallback(response);

        } else if (type.equals(StateEvents.AjaxAvailableServiceFailure)) {
            Log.w(TAG, "AjaxAvailableServiceFailure");
            getAvailableServicesError();

        } else if (type.equals(StateEvents.AvailableSensorsRequested)) {
            // Log.d(TAG, "AvailableSensorsRequested");
            getAvailableSensors(event);

        } else if (type.equals(StateEvents.CreateServiceRequested)) {
            // Log.d(TAG, "CreateRequested");
            final String name = event.<String> getData("name");
            final TreeModel service = event.<TreeModel> getData("service");
            final ModelData sensor = event.<ModelData> getData("sensor");
            final List<ModelData> dataFields = event.<List<ModelData>> getData("dataFields");
            createService(name, service, sensor, dataFields);

        } else if (type.equals(StateEvents.RemoveRequested)) {
            // Log.d(TAG, "RemoveRequested");
            TreeModel sensor = event.<TreeModel> getData("sensor");
            TreeModel service = event.<TreeModel> getData("service");
            disconnectService(sensor, service);

        } else if (type.equals(StateEvents.ConnectRequested)) {
            // Log.d(TAG, "ConnectRequested");
            final TreeModel sensor = event.<TreeModel> getData("sensor");
            final TreeModel service = event.<TreeModel> getData("service");
            connectService(sensor, service);

        } else if (type.equals(StateEvents.MethodsRequested)) {
            // Log.d(TAG, "MethodsRequested");
            final TreeModel service = event.<TreeModel> getData();
            getServiceMethods(service);

        } else if (type.equals(StateEvents.InvokeMethodRequested)) {
            // Log.d(TAG, "InvokeMethodRequested");
            invokeMethod(event);

        } else if (type.equals(StateEvents.AjaxMethodFailure)) {
            Log.w(TAG, "AjaxMethodFailure");
            final int code = event.getData("code");
            invokeMethodErrorCallback(code);

        } else if (type.equals(StateEvents.AjaxMethodSuccess)) {
            // Log.d(TAG, "AjaxMethodSuccess");
            final String response = event.<String> getData("response");
            invokeMethodCallback(response);

        } else if (type.equals(StateEvents.AjaxConnectFailure)) {
            Log.w(TAG, "AjaxConnectFailure");
            final int code = event.getData("code");
            connectServiceErrorCallback(code);

        } else if (type.equals(StateEvents.AjaxConnectSuccess)) {
            // Log.d(TAG, "AjaxConnectSuccess");
            final String response = event.<String> getData("response");
            connectServiceCallback(response);

        } else if (type.equals(StateEvents.AjaxCreateFailure)) {
            Log.w(TAG, "AjaxCreateFailure");
            final int code = event.getData("code");
            createServiceErrorCallback(code);

        } else if (type.equals(StateEvents.AjaxCreateSuccess)) {
            // Log.d(TAG, "AjaxCreateSuccess");
            final String response = event.<String> getData("response");
            createServiceCallback(response);

        } else if (type.equals(StateEvents.AjaxDisconnectFailure)) {
            Log.w(TAG, "AjaxDisconnectFailure");
            final int code = event.getData("code");
            disconnectServiceErrorCallback(code);

        } else if (type.equals(StateEvents.AjaxDisconnectSuccess)) {
            // Log.d(TAG, "AjaxDisconnectSuccess");
            final String response = event.<String> getData("response");
            disconnectServiceCallback(response);

        } else if (type.equals(StateEvents.AjaxGetMethodsFailure)) {
            Log.w(TAG, "AjaxGetMethodsFailure");
            final int code = event.getData("code");
            getServiceMethodsErrorCallback(code);

        } else if (type.equals(StateEvents.AjaxGetMethodsSuccess)) {
            // Log.d(TAG, "AjaxGetMethodsSuccess");
            final String response = event.<String> getData("response");
            parseServiceMethods(response);
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
                || type.equals(MainEvents.Init) || type.equals(StateEvents.RemoveComplete)
                || type.equals(StateEvents.RemoveFailed) || type.equals(StateEvents.Working)
                || type.equals(VizEvents.Show) || type.equals(StateEvents.ConnectComplete)
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

        // get event info
        TreeModel service = event.<TreeModel> getData("service");
        ModelData sensor = service.getChild(0);
        ModelData serviceMethod = event.<ModelData> getData("method");
        List<String> params = event.<List<String>> getData("parameters");

        // prepare request properties
        final String method = params.size() > 0 ? "POST" : "GET";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services/"
                + service.<String> get("id") + "/" + serviceMethod.<String> get("name") + ".json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEvents.AjaxMethodSuccess);
        final AppEvent onFailure = new AppEvent(StateEvents.AjaxMethodFailure);

        // create request body
        String body = null;
        if (params.size() > 0) {
            body = "{\"parameters\":[";
            for (String p : params) {
                body += "\"" + p + "\",";
            }
            body = body.substring(0, body.length() - 1);
            body += "]}";
        }

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    public void invokeMethodCallback(String response) {

        if (response != null) {
            // try to get "methods" array
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonVal = json.get("result");
            if (null != jsonVal) {
                JSONString jsonResult = jsonVal.isString();
                if (null != jsonResult) {
                    String result = jsonResult.stringValue();
                    forwardToView(this.editor, new AppEvent(StateEvents.InvokeMethodComplete,
                            result));
                } else {
                    Log.e(TAG,
                            "Error parsing service methods response: \"result\" is not a JSON String");
                    invokeMethodErrorCallback(0);
                }
            } else {
                Log.e(TAG, "Error parsing service methods response: \"result\" is is not found");
                invokeMethodErrorCallback(0);
            }
        } else {
            Log.e(TAG, "Error parsing service methods response: response=null");
            invokeMethodErrorCallback(0);
        }
    }

    public void invokeMethodErrorCallback(int code) {
        forwardToView(this.editor, new AppEvent(StateEvents.InvokeMethodFailed));
    }

    private void loadSensors() {
        this.isLoadingSensors = true;

        List<TreeModel> mySensors = Registry.<List<TreeModel>> get(Constants.REG_MY_SENSORS);
        TreeModel mySensorsParent = new BaseTreeModel();
        mySensorsParent.set("tagType", TagModel.TYPE_CATEGORY);
        mySensorsParent.set("text", "My personal sensors");
        if (null != mySensors) {
            for (TreeModel sensor : mySensors) {
                mySensorsParent.add(new BaseTreeModel(sensor.getProperties()));
                List<ModelData> children = sensor.getChildren();
                
                for (ModelData child : children) {
                    
                }
            }
        } else {
            Log.w(TAG, "Getting list of personal sensors for state sensor creation");
            Dispatcher.forwardEvent(MySensorsEvents.ListRequested);
            return;
        }

        List<TreeModel> groupSensors = Registry.<List<TreeModel>> get(Constants.REG_GROUP_SENSORS);
        TreeModel groupSensorsParent = new BaseTreeModel();
        groupSensorsParent.set("tagType", TagModel.TYPE_CATEGORY);
        groupSensorsParent.set("text", "My group sensors");
        if (null != groupSensors) {
            for (TreeModel sensor : groupSensors) {
                groupSensorsParent.add(sensor);
            }
        } else {
            Log.w(TAG, "Getting list of group sensors for state sensor creation");
            Dispatcher.forwardEvent(GroupSensorsEvents.ListRequest);
            return;
        }

        List<TreeModel> sensors = new ArrayList<TreeModel>();
        sensors.add(0, mySensorsParent);
        sensors.add(1, groupSensorsParent);

        AppEvent success = new AppEvent(StateEvents.LoadSensorsSuccess);
        success.setData("sensors", sensors);
        forwardToView(creator, success);
        this.isLoadingSensors = false;
    }

    private void parseServiceMethods(String response) {
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
                    getServiceMethodsCallback(methods);
                } else {
                    Log.e(TAG,
                            "Error parsing service methods response: \"methods\" is not a JSON Array");
                    getServiceMethodsErrorCallback(0);
                }
            } else {
                Log.e(TAG, "Error parsing service methods response: \"methods\" is is not found");
                getServiceMethodsErrorCallback(0);
            }
        } else {
            Log.e(TAG, "Error parsing service methods response: response=null");
            getServiceMethodsErrorCallback(0);
        }
    }
}
