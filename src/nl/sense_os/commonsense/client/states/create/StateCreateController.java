package nl.sense_os.commonsense.client.states.create;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.sensors.library.LibraryEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.ServiceModel;

import com.extjs.gxt.ui.client.Registry;
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

public class StateCreateController extends Controller {

    private static final String TAG = "StateCreateController";
    private View creator;
    private boolean isLoadingSensors;

    public StateCreateController() {
        registerEventTypes(StateCreateEvents.ShowCreator);

        // get available services for a sensor
        registerEventTypes(StateCreateEvents.AvailableServicesRequested,
                StateCreateEvents.AjaxAvailableServiceSuccess,
                StateCreateEvents.AjaxAvailableServiceFailure);

        // load all sensors to create service from
        registerEventTypes(StateCreateEvents.LoadSensors);
        registerEventTypes(LibraryEvents.ListUpdated);

        // create state from sensor
        registerEventTypes(StateCreateEvents.CreateServiceRequested,
                StateCreateEvents.CreateServiceComplete, StateCreateEvents.CreateServiceCancelled,
                StateCreateEvents.AjaxCreateSuccess, StateCreateEvents.AjaxCreateFailure);
    }

    private void createService(String name, TreeModel service, ModelData sensor,
            List<ModelData> dataFields) {

        // prepare request properties
        final String method = "POST";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id")
                + "/services.json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateCreateEvents.AjaxCreateSuccess);
        final AppEvent onFailure = new AppEvent(StateCreateEvents.AjaxCreateFailure);

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

    private void getAvailableServices(SensorModel sensor) {

        String aliasParam = "";
        if (sensor.get("alias") != null && sensor.<String> get("alias").length() > 0) {
            aliasParam = "?alias=" + sensor.<String> get("alias");
        }

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id")
                + "/services/available" + aliasParam;
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateCreateEvents.AjaxAvailableServiceSuccess);
        final AppEvent onFailure = new AppEvent(StateCreateEvents.AjaxAvailableServiceFailure);

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
         * Request available services for a sensor
         */
        if (type.equals(StateCreateEvents.AvailableServicesRequested)) {
            // Log.d(TAG, "AvailableServicesRequested");
            final SensorModel sensor = event.getData("sensor");
            getAvailableServices(sensor);

        } else if (type.equals(StateCreateEvents.AjaxAvailableServiceSuccess)) {
            // Log.d(TAG, "AjaxAvailableServiceSuccess");
            final String response = event.getData("response");
            onAvailableServicesSuccess(response);

        } else if (type.equals(StateCreateEvents.AjaxAvailableServiceFailure)) {
            Log.w(TAG, "AjaxAvailableServiceFailure");
            onAvailableServicesFailure();

        } else

        /*
         * Create service from a sensor
         */
        if (type.equals(StateCreateEvents.CreateServiceRequested)) {
            // Log.d(TAG, "CreateRequested");
            final String name = event.<String> getData("name");
            final TreeModel service = event.<TreeModel> getData("service");
            final ModelData sensor = event.<ModelData> getData("sensor");
            final List<ModelData> dataFields = event.<List<ModelData>> getData("dataFields");
            createService(name, service, sensor, dataFields);

        } else if (type.equals(StateCreateEvents.AjaxCreateFailure)) {
            Log.w(TAG, "CreateAjaxFailure");
            final int code = event.getData("code");
            onCreateServiceFailure(code);

        } else if (type.equals(StateCreateEvents.AjaxCreateSuccess)) {
            // Log.d(TAG, "CreateAjaxSuccess");
            final String response = event.<String> getData("response");
            onCreateServiceSuccess(response);

        } else

        /*
         * Load all sensors to create service from
         */
        if (type.equals(StateCreateEvents.LoadSensors)) {
            // Log.d(TAG, "LoadSensors");
            loadSensors();

        } else if (type.equals(LibraryEvents.ListUpdated)) {
            if (isLoadingSensors) {
                // Log.d(TAG, "Sensor lists updated: LoadSensors");
                loadSensors();
            }

        } else

        /*
         * Pass rest through to creator view
         */
        {
            forwardToView(this.creator, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.creator = new StateCreator(this);
    }

    /**
     * Loads all sensors that can be used to create a new state with.
     */
    // TODO does not signal LoadFailure
    private void loadSensors() {
        this.isLoadingSensors = true;

        List<SensorModel> sensors = Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST);
        if (null == sensors) {
            Dispatcher.forwardEvent(LibraryEvents.LoadRequest);
            return;
        }

        AppEvent success = new AppEvent(StateCreateEvents.LoadSensorsSuccess);
        success.setData("sensors", sensors);
        forwardToView(creator, success);
        this.isLoadingSensors = false;
    }

    private void onAvailableServicesFailure() {
        forwardToView(this.creator, new AppEvent(StateCreateEvents.AvailableServicesNotUpdated));
    }

    private void onAvailableServicesSuccess(String response) {

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

                    AppEvent success = new AppEvent(StateCreateEvents.AvailableServicesUpdated);
                    success.setData("services", result);
                    forwardToView(this.creator, success);

                } else {
                    Log.e(TAG,
                            "Error parsing service methods response: \"available_services\" is not a JSON Array");
                    onAvailableServicesFailure();
                }
            } else {
                Log.e(TAG,
                        "Error parsing service methods response: \"available_services\" is is not found");
                onAvailableServicesFailure();
            }
        } else {
            Log.e(TAG, "Error parsing service methods response: response=null");
            onAvailableServicesFailure();
            onAvailableServicesFailure();
        }
    }

    private void onCreateServiceFailure(int code) {
        forwardToView(this.creator, new AppEvent(StateCreateEvents.CreateServiceFailed));
    }

    private void onCreateServiceSuccess(String response) {
        // update global sensor list
        Dispatcher.forwardEvent(StateCreateEvents.CreateServiceComplete);
    }
}
