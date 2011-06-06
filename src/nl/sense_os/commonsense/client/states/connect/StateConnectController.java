package nl.sense_os.commonsense.client.states.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.ServiceModel;
import nl.sense_os.commonsense.client.groups.list.GetServicesResponseJso;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateConnectController extends Controller {

    private static final Logger LOG = Logger.getLogger(StateConnectController.class.getName());
    private View connecter;

    public StateConnectController() {

        LOG.setLevel(Level.WARNING);

        registerEventTypes(StateConnectEvents.ShowSensorConnecter);

        registerEventTypes(StateConnectEvents.ServiceNameRequest,
                StateConnectEvents.ServiceNameAjaxSuccess,
                StateConnectEvents.ServiceNameAjaxFailure);

        registerEventTypes(StateConnectEvents.ConnectRequested, StateConnectEvents.ConnectSuccess,
                StateConnectEvents.ConnectAjaxSuccess, StateConnectEvents.ConnectAjaxFailure);

        registerEventTypes(StateConnectEvents.AvailableSensorsRequested,
                StateConnectEvents.AvailableSensorsUpdated,
                StateConnectEvents.AvailableSensorsNotUpdated);
    }

    private void connectService(SensorModel sensor, SensorModel stateSensor, String serviceName) {

        // prepare request properties
        final String method = "POST";
        final String url = Urls.SENSORS + "/" + sensor.getId() + "/services.json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateConnectEvents.ConnectAjaxSuccess);
        final AppEvent onFailure = new AppEvent(StateConnectEvents.ConnectAjaxFailure);

        // prepare request body
        String body = "{\"service\":{";
        body += "\"id\":\"" + stateSensor.getId() + "\"";
        body += ",\"name\":\"" + serviceName + "\"";
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

    private void connectServiceCallback(String response) {
        Dispatcher.forwardEvent(StateConnectEvents.ConnectSuccess);
    }

    private void connectServiceErrorCallback(int code) {
        forwardToView(this.connecter, new AppEvent(StateConnectEvents.ConnectFailure));
    }

    private void getAvailableSensors(String serviceName,
            final AsyncCallback<List<SensorModel>> proxyCallback) {

        List<SensorModel> result = new ArrayList<SensorModel>();

        List<SensorModel> library = Registry.get(Constants.REG_SENSOR_LIST);
        for (SensorModel sensor : library) {
            List<ServiceModel> availableServices = sensor.getAvailServices();
            if (null != availableServices) {
                for (ServiceModel availableService : availableServices) {
                    if (availableService.getName().equalsIgnoreCase(serviceName)) {
                        result.add(sensor);
                        break;
                    }
                }
            }
        }

        proxyCallback.onSuccess(result);
    }

    private void getServiceName(SensorModel stateSensor) {

        if (stateSensor.getChildCount() == 0) {
            // if a service has no child sensors, we cannot get the name
            getServiceNameError();
            return;
        }
        SensorModel sensor = (SensorModel) stateSensor.getChild(0);

        final String method = "GET";
        final String url = Urls.SENSORS + "/" + sensor.getId() + "/services" + ".json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateConnectEvents.ServiceNameAjaxSuccess);
        onSuccess.setData("stateSensor", stateSensor);
        final AppEvent onFailure = new AppEvent(StateConnectEvents.ServiceNameAjaxFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);

    }

    private void getServiceNameCallback(SensorModel stateSensor, String response) {
        if (response != null) {

            // parse list of running services from the response
            GetServicesResponseJso jso = JsonUtils.unsafeEval(response);
            List<ServiceModel> services = jso.getServices();

            // find the right service among all the running services
            for (ServiceModel service : services) {

                int id = service.getId();
                if (id == stateSensor.getId()) {
                    String name = service.getName();

                    // forward event to Connecter
                    AppEvent event = new AppEvent(StateConnectEvents.ServiceNameSuccess);
                    event.setData("name", name);
                    forwardToView(this.connecter, event);
                    return;
                }
            }

            // if we made it here, the service was not found!
            getServiceNameError();

        } else {
            LOG.severe("Error parsing running services response: response=null");
            getServiceNameError();
        }

    }

    private void getServiceNameError() {
        forwardToView(this.connecter, new AppEvent(StateConnectEvents.ServiceNameFailure));
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        /*
         * Get available sensors for this service
         */
        if (type.equals(StateConnectEvents.AvailableSensorsRequested)) {
            LOG.fine("AvailableSensorsRequested");
            final String serviceName = event.<String> getData("name");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            getAvailableSensors(serviceName, callback);

        } else

        /*
         * Connect sensor to the service
         */
        if (type.equals(StateConnectEvents.ConnectRequested)) {
            LOG.fine("ConnectRequested");
            final SensorModel sensor = event.<SensorModel> getData("sensor");
            final SensorModel stateSensor = event.<SensorModel> getData("stateSensor");
            final String serviceName = event.<String> getData("serviceName");
            connectService(sensor, stateSensor, serviceName);

        } else if (type.equals(StateConnectEvents.ConnectAjaxFailure)) {
            LOG.warning("ConnectAjaxFailure");
            final int code = event.getData("code");
            connectServiceErrorCallback(code);

        } else if (type.equals(StateConnectEvents.ConnectAjaxSuccess)) {
            LOG.fine("ConnectAjaxSuccess");
            final String response = event.<String> getData("response");
            connectServiceCallback(response);

        } else

        /*
         * Get service name (before getting available sensors)
         */
        if (type.equals(StateConnectEvents.ServiceNameRequest)) {
            LOG.fine("ServiceNameRequest");
            final SensorModel service = event.<SensorModel> getData("stateSensor");
            getServiceName(service);

        } else if (type.equals(StateConnectEvents.ServiceNameAjaxSuccess)) {
            LOG.fine("ServiceNameAjaxSuccess");
            final SensorModel stateSensor = event.<SensorModel> getData("stateSensor");
            final String response = event.<String> getData("response");
            getServiceNameCallback(stateSensor, response);

        } else if (type.equals(StateConnectEvents.ServiceNameAjaxFailure)) {
            LOG.warning("ServiceNameAjaxFailure");
            getServiceNameError();

        } else

        /*
         * Forward to the connector view
         */
        {
            forwardToView(this.connecter, event);
        }

    }

    @Override
    protected void initialize() {
        super.initialize();
        this.connecter = new StateConnecter(this);
    }

}
