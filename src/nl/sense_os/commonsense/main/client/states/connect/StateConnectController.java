package nl.sense_os.commonsense.main.client.states.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.constant.Constants;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.common.client.httpresponse.GetServicesResponseJso;
import nl.sense_os.commonsense.common.client.model.SensorModel;
import nl.sense_os.commonsense.common.client.model.ServiceModel;
import nl.sense_os.commonsense.main.client.auth.SessionManager;

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
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateConnectController extends Controller {

    private static final Logger LOG = Logger.getLogger(StateConnectController.class.getName());
    private View connecter;

    public StateConnectController() {

	// LOG.setLevel(Level.ALL);

	registerEventTypes(StateConnectEvents.ShowSensorConnecter);
	registerEventTypes(StateConnectEvents.ServiceNameRequest);
	registerEventTypes(StateConnectEvents.ConnectRequested, StateConnectEvents.ConnectSuccess);
	registerEventTypes(StateConnectEvents.AvailableSensorsRequested,
		StateConnectEvents.AvailableSensorsUpdated,
		StateConnectEvents.AvailableSensorsNotUpdated);
    }

    private void connectService(SensorModel sensor, SensorModel stateSensor, String serviceName) {

	// prepare request properties
	final Method method = RequestBuilder.POST;
	final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
	urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services.json");
	final String url = urlBuilder.buildString();
	final String sessionId = SessionManager.getSessionId();

	// prepare request body
	String body = "{\"service\":{";
	body += "\"id\":\"" + stateSensor.getId() + "\"";
	body += ",\"name\":\"" + serviceName + "\"";
	body += "}}";

	// prepare request callback
	RequestCallback reqCallback = new RequestCallback() {

	    @Override
	    public void onError(Request request, Throwable exception) {
		LOG.warning("POST sensor services onError callback: " + exception.getMessage());
		onConnectServiceFailure(0);
	    }

	    @Override
	    public void onResponseReceived(Request request, Response response) {
		LOG.finest("POST sensor services response received: " + response.getStatusText());
		int statusCode = response.getStatusCode();
		if (Response.SC_CREATED == statusCode) {
		    onConnectServiceSuccess(response.getText());
		} else {
		    LOG.warning("POST sensor services returned incorrect status: " + statusCode);
		    onConnectServiceFailure(statusCode);
		}
	    }
	};

	// send request
	try {
	    RequestBuilder builder = new RequestBuilder(method, url);
	    builder.setHeader("X-SESSION_ID", sessionId);
	    builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
	    builder.sendRequest(body, reqCallback);
	} catch (Exception e) {
	    LOG.warning("POST sensor services request threw exception: " + e.getMessage());
	    reqCallback.onError(null, e);
	}
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

    private void getServiceName(final SensorModel stateSensor) {

	if (stateSensor.getChildCount() == 0) {
	    // if a service has no child sensors, we cannot get the name
	    onServiceNameFailure();
	    return;
	}
	SensorModel sensor = (SensorModel) stateSensor.getChild(0);

	final Method method = RequestBuilder.GET;
	final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
	urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services.json");
	final String url = urlBuilder.buildString();
	final String sessionId = SessionManager.getSessionId();

	// prepare request callback
	RequestCallback reqCallback = new RequestCallback() {

	    @Override
	    public void onError(Request request, Throwable exception) {
		LOG.warning("GET sensor services onError callback: " + exception.getMessage());
		onServiceNameFailure();
	    }

	    @Override
	    public void onResponseReceived(Request request, Response response) {
		LOG.finest("GET sensor services response received: " + response.getStatusText());
		int statusCode = response.getStatusCode();
		if (Response.SC_OK == statusCode) {
		    onServiceNameSuccess(stateSensor, response.getText());
		} else {
		    LOG.warning("GET sensor services returned incorrect status: " + statusCode);
		    onServiceNameFailure();
		}
	    }
	};

	// send request
	try {
	    RequestBuilder builder = new RequestBuilder(method, url);
	    builder.setHeader("X-SESSION_ID", sessionId);
	    builder.sendRequest(null, reqCallback);
	} catch (Exception e) {
	    LOG.warning("GET sensor services request threw exception: " + e.getMessage());
	    reqCallback.onError(null, e);
	}
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

	} else

	/*
	 * Get service name (before getting available sensors)
	 */
	if (type.equals(StateConnectEvents.ServiceNameRequest)) {
	    LOG.fine("ServiceNameRequest");
	    final SensorModel service = event.<SensorModel> getData("stateSensor");
	    getServiceName(service);

	} else

	/*
	 * Forward to the connector view
	 */
	{
	    forwardToView(connecter, event);
	}

    }

    @Override
    protected void initialize() {
	super.initialize();
	connecter = new StateConnecter(this);
    }

    private void onConnectServiceFailure(int code) {
	forwardToView(connecter, new AppEvent(StateConnectEvents.ConnectFailure));
    }

    private void onConnectServiceSuccess(String response) {
	Dispatcher.forwardEvent(StateConnectEvents.ConnectSuccess);
    }

    private void onServiceNameFailure() {
	forwardToView(connecter, new AppEvent(StateConnectEvents.ServiceNameFailure));
    }

    private void onServiceNameSuccess(SensorModel stateSensor, String response) {

	// parse list of running services from the response
	List<ServiceModel> services = new ArrayList<ServiceModel>();
	if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
	    GetServicesResponseJso jso = JsonUtils.unsafeEval(response);
	    services = jso.getServices();
	}

	// find the right service among all the running services
	for (ServiceModel service : services) {

	    int id = service.getId();
	    if (id == stateSensor.getId()) {
		String name = service.getName();

		// forward event to Connecter
		AppEvent event = new AppEvent(StateConnectEvents.ServiceNameSuccess);
		event.setData("name", name);
		forwardToView(connecter, event);
		return;
	    }
	}

	// if we made it here, the service was not found!
	onServiceNameFailure();
    }
}
