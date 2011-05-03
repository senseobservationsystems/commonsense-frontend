package nl.sense_os.commonsense.client.states.create;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.sensors.group.GroupSensorsEvents;
import nl.sense_os.commonsense.client.sensors.personal.MySensorsEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.Copier;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.ServiceModel;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.Registry;
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
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.List;

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
        registerEventTypes(GroupSensorsEvents.ListUpdated, MySensorsEvents.TreeUpdated);

        // create state from sensor
        registerEventTypes(StateCreateEvents.CreateServiceRequested,
                StateCreateEvents.CreateServiceComplete, StateCreateEvents.CreateServiceCancelled,
                StateCreateEvents.AjaxCreateSuccess, StateCreateEvents.AjaxCreateFailure);
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
            getAvailableServicesCallback(response);

        } else if (type.equals(StateCreateEvents.AjaxAvailableServiceFailure)) {
            Log.w(TAG, "AjaxAvailableServiceFailure");
            getAvailableServicesError();

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
            Log.w(TAG, "AjaxCreateFailure");
            final int code = event.getData("code");
            createServiceErrorCallback(code);

        } else if (type.equals(StateCreateEvents.AjaxCreateSuccess)) {
            // Log.d(TAG, "AjaxCreateSuccess");
            final String response = event.<String> getData("response");
            createServiceCallback(response);

        } else

        /*
         * Load all sensors to create service from
         */
        if (type.equals(StateCreateEvents.LoadSensors)) {
            // Log.d(TAG, "LoadSensors");
            loadSensors();

        } else if (type.equals(MySensorsEvents.TreeUpdated)
                || type.equals(GroupSensorsEvents.ListUpdated)) {
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

    private void createServiceCallback(String response) {
        Dispatcher.forwardEvent(StateCreateEvents.CreateServiceComplete);
    }

    private void createServiceErrorCallback(int code) {
        forwardToView(this.creator, new AppEvent(StateCreateEvents.CreateServiceFailed));
    }

    private void getAvailableServices(SensorModel sensor) {

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id")
                + "/services/available";
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

                    AppEvent success = new AppEvent(StateCreateEvents.AvailableServicesUpdated);
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
        forwardToView(this.creator, new AppEvent(StateCreateEvents.AvailableServicesNotUpdated));
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

        List<TreeModel> mySensors = Registry.<List<TreeModel>> get(Constants.REG_MY_SENSORS_TREE);
        TreeModel mySensorsParent = new BaseTreeModel();
        mySensorsParent.set("tagType", TagModel.TYPE_CATEGORY);
        mySensorsParent.set("text", "My personal sensors");
        if (null != mySensors) {
            for (TreeModel sensor : mySensors) {
                TreeModel copy = Copier.copySensor(sensor);
                mySensorsParent.add(copy);
            }
        } else {
            Dispatcher.forwardEvent(MySensorsEvents.TreeRequested);
            return;
        }

        List<TreeModel> groupSensors = Registry.<List<TreeModel>> get(Constants.REG_GROUP_SENSORS);
        TreeModel groupSensorsParent = new BaseTreeModel();
        groupSensorsParent.set("tagType", TagModel.TYPE_CATEGORY);
        groupSensorsParent.set("text", "My group sensors");
        if (null != groupSensors) {
            for (TreeModel sensor : groupSensors) {
                TreeModel copy = Copier.copySensor(sensor);
                groupSensorsParent.add(copy);
            }
        } else {
            Dispatcher.forwardEvent(GroupSensorsEvents.ListRequest);
            return;
        }

        List<TreeModel> sensors = new ArrayList<TreeModel>();
        sensors.add(groupSensorsParent);
        sensors.add(mySensorsParent);

        AppEvent success = new AppEvent(StateCreateEvents.LoadSensorsSuccess);
        success.setData("sensors", sensors);
        forwardToView(creator, success);
        this.isLoadingSensors = false;
    }
}
