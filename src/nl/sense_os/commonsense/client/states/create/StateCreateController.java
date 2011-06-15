package nl.sense_os.commonsense.client.states.create;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.ServiceModel;
import nl.sense_os.commonsense.client.sensors.library.LibraryEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;

public class StateCreateController extends Controller {

    private static final Logger LOG = Logger.getLogger(StateCreateController.class.getName());
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

    private void createService(String name, ServiceModel service, SensorModel sensor,
            List<ModelData> dataFields) {

        // prepare request properties
        final String method = "POST";
        final String url = Urls.SENSORS + "/" + sensor.getId() + "/services.json";
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
        if (sensor.getAlias() != -1) {
            aliasParam = "?alias=" + sensor.getAlias();
        }

        // prepare request properties
        final String method = "GET";
        final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/available" + ".json"
                + aliasParam;
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
            // LOG.fine( "AvailableServicesRequested");
            final SensorModel sensor = event.getData("sensor");
            getAvailableServices(sensor);

        } else if (type.equals(StateCreateEvents.AjaxAvailableServiceSuccess)) {
            // LOG.fine( "AjaxAvailableServiceSuccess");
            final String response = event.getData("response");
            onAvailableServicesSuccess(response);

        } else if (type.equals(StateCreateEvents.AjaxAvailableServiceFailure)) {
            LOG.warning("AjaxAvailableServiceFailure");
            onAvailableServicesFailure();

        } else

        /*
         * Create service from a sensor
         */
        if (type.equals(StateCreateEvents.CreateServiceRequested)) {
            // LOG.fine( "CreateRequested");
            final String name = event.<String> getData("name");
            final ServiceModel service = event.<ServiceModel> getData("service");
            final SensorModel sensor = event.<SensorModel> getData("sensor");
            final List<ModelData> dataFields = event.<List<ModelData>> getData("dataFields");
            createService(name, service, sensor, dataFields);

        } else if (type.equals(StateCreateEvents.AjaxCreateFailure)) {
            LOG.warning("CreateAjaxFailure");
            final int code = event.getData("code");
            onCreateServiceFailure(code);

        } else if (type.equals(StateCreateEvents.AjaxCreateSuccess)) {
            // LOG.fine( "CreateAjaxSuccess");
            final String response = event.<String> getData("response");
            onCreateServiceSuccess(response);

        } else

        /*
         * Load all sensors to create service from
         */
        if (type.equals(StateCreateEvents.LoadSensors)) {
            // LOG.fine( "LoadSensors");
            loadSensors();

        } else if (type.equals(LibraryEvents.ListUpdated)) {
            if (isLoadingSensors) {
                // LOG.fine( "Sensor lists updated: LoadSensors");
                loadSensors();
            }

        } else

        /*
         * Pass rest through to creator view
         */
        {
            forwardToView(creator, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        creator = new StateCreator(this);
    }

    /**
     * Loads all sensors that can be used to create a new state with.
     */
    // TODO does not signal LoadFailure
    private void loadSensors() {
        isLoadingSensors = true;

        List<SensorModel> sensors = Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST);
        if (null == sensors) {
            Dispatcher.forwardEvent(LibraryEvents.LoadRequest);
            return;
        }

        AppEvent success = new AppEvent(StateCreateEvents.LoadSensorsSuccess);
        success.setData("sensors", sensors);
        forwardToView(creator, success);
        isLoadingSensors = false;
    }

    private void onAvailableServicesFailure() {
        forwardToView(creator, new AppEvent(StateCreateEvents.AvailableServicesNotUpdated));
    }

    private void onAvailableServicesSuccess(String response) {

        // parse list of services from response
        List<ServiceModel> services = new ArrayList<ServiceModel>();
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            AvailServicesResponseJso jso = JsonUtils.unsafeEval(response);
            services = jso.getServices();
        }

        AppEvent success = new AppEvent(StateCreateEvents.AvailableServicesUpdated);
        success.setData("services", services);
        forwardToView(creator, success);
    }

    private void onCreateServiceFailure(int code) {
        forwardToView(creator, new AppEvent(StateCreateEvents.CreateServiceFailed));
    }

    private void onCreateServiceSuccess(String response) {
        // update global sensor list
        Dispatcher.forwardEvent(StateCreateEvents.CreateServiceComplete);
    }
}
