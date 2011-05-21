package nl.sense_os.commonsense.client.states.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.shared.constants.Constants;
import nl.sense_os.commonsense.shared.constants.Urls;
import nl.sense_os.commonsense.shared.models.SensorModel;
import nl.sense_os.commonsense.shared.models.ServiceModel;

import com.extjs.gxt.ui.client.Registry;
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

public class StateConnectController extends Controller {
    private static final Logger logger = Logger.getLogger("StateConnectController");
    private View connecter;

    public StateConnectController() {
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

    private void connectService(TreeModel sensor, TreeModel service, String serviceName) {

        // prepare request properties
        final String method = "POST";
        final String url = Urls.SENSORS + "/" + sensor.<String> get(SensorModel.ID)
                + "/services.json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateConnectEvents.ConnectAjaxSuccess);
        final AppEvent onFailure = new AppEvent(StateConnectEvents.ConnectAjaxFailure);

        // prepare request body
        String body = "{\"service\":{";
        body += "\"id\":\"" + service.<String> get(SensorModel.ID) + "\"";
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

    private void getServiceName(TreeModel service) {

        if (service.getChildCount() == 0) {
            // if a service has no child sensors, we cannot get the name
            getServiceNameError();
            return;
        }
        SensorModel sensor = (SensorModel) service.getChild(0);

        final String method = "GET";
        final String url = Urls.SENSORS + "/" + sensor.<String> get(SensorModel.ID) + "/services";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateConnectEvents.ServiceNameAjaxSuccess);
        onSuccess.setData("service", service);
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

    private void getServiceNameCallback(TreeModel service, String response) {
        if (response != null) {
            // try to get "methods" array
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonVal = json.get("services");
            if (null != jsonVal) {
                JSONArray services = jsonVal.isArray();
                if (null != services) {

                    for (int i = 0; i < services.size(); i++) {
                        JSONObject serviceJson = services.get(i).isObject();
                        if (serviceJson != null) {
                            String id = serviceJson.get(ServiceModel.ID).isString().stringValue();
                            if (id.equals(service.<String> get(SensorModel.ID))) {
                                String name = serviceJson.get(ServiceModel.NAME).isString()
                                        .stringValue();

                                // forward event to Connecter
                                AppEvent event = new AppEvent(StateConnectEvents.ServiceNameSuccess);
                                event.setData("name", name);
                                forwardToView(this.connecter, event);
                                return;
                            }
                        }
                    }

                    // if we made it here, the service was not found!
                    getServiceNameError();

                } else {
                    logger.severe("Error parsing running services response: \"services\" is not a JSON Array");
                    getServiceNameError();
                }
            } else {
                logger.severe("Error parsing running services response: \"services\" is is not found");
                getServiceNameError();
            }
        } else {
            logger.severe("Error parsing running services response: response=null");
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
            // logger.fine( "AvailableSensorsRequested");
            final String serviceName = event.<String> getData("name");
            final AsyncCallback<List<SensorModel>> callback = event
                    .<AsyncCallback<List<SensorModel>>> getData("callback");
            getAvailableSensors(serviceName, callback);

        } else

        /*
         * Connect sensor to the service
         */
        if (type.equals(StateConnectEvents.ConnectRequested)) {
            // logger.fine( "ConnectRequested");
            final TreeModel sensor = event.<TreeModel> getData("sensor");
            final TreeModel service = event.<TreeModel> getData("service");
            final String serviceName = event.<String> getData("serviceName");
            connectService(sensor, service, serviceName);

        } else if (type.equals(StateConnectEvents.ConnectAjaxFailure)) {
            logger.warning("ConnectAjaxFailure");
            final int code = event.getData("code");
            connectServiceErrorCallback(code);

        } else if (type.equals(StateConnectEvents.ConnectAjaxSuccess)) {
            // logger.fine( "ConnectAjaxSuccess");
            final String response = event.<String> getData("response");
            connectServiceCallback(response);

        } else

        /*
         * Get service name (before getting available sensors)
         */
        if (type.equals(StateConnectEvents.ServiceNameRequest)) {
            // logger.fine( "ServiceNameRequest");
            final TreeModel service = event.<TreeModel> getData("service");
            getServiceName(service);

        } else if (type.equals(StateConnectEvents.ServiceNameAjaxSuccess)) {
            // logger.fine( "ServiceNameAjaxSuccess");
            final TreeModel service = event.<TreeModel> getData("service");
            final String response = event.<String> getData("response");
            getServiceNameCallback(service, response);

        } else if (type.equals(StateConnectEvents.ServiceNameAjaxFailure)) {
            logger.warning("ServiceNameAjaxFailure");
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
