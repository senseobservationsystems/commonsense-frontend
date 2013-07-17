package nl.sense_os.commonsense.main.client.states.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetMethodsResponse;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetSensorsResponse;
import nl.sense_os.commonsense.common.client.model.Sensor;
import nl.sense_os.commonsense.common.client.model.ServiceMethod;
import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient.Urls;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtServiceMethod;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;
import nl.sense_os.commonsense.main.client.ext.util.TreeCopier;
import nl.sense_os.commonsense.main.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.main.client.states.connect.StateConnectEvents;
import nl.sense_os.commonsense.main.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.main.client.states.defaults.StateDefaultsEvents;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateListController extends Controller {

	private static final Logger LOG = Logger.getLogger(StateListController.class.getName());
	private View tree;

	public StateListController() {
		registerEventTypes(VizEvents.Show);
		registerEventTypes(StateListEvents.ShowGrid);

		// external triggers to initiate a list update
		registerEventTypes(StateCreateEvents.CreateServiceComplete,
				StateConnectEvents.ConnectSuccess, SensorDeleteEvents.DeleteSuccess,
				StateDefaultsEvents.CheckDefaultsSuccess);

		// events to update the list of groups
		registerEventTypes(StateListEvents.LoadRequest);

		registerEventTypes(StateListEvents.RemoveRequested, StateListEvents.RemoveComplete);
	}

	private void disconnectService(ExtSensor sensor, ExtSensor stateSensor) {

		// prepare request data
		final Method method = RequestBuilder.DELETE;
		final UrlBuilder urlBuilder = new UrlBuilder().setProtocol(CommonSenseClient.Urls.PROTOCOL)
				.setHost(CommonSenseClient.Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services/"
				+ stateSensor.getId() + ".json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

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
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("DELETE service request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void getConnected(final ExtSensor state, final AsyncCallback<List<ExtSensor>> callback) {

		// prepare request properties
		final Method method = RequestBuilder.GET;
		final UrlBuilder urlBuilder = new UrlBuilder().setProtocol(CommonSenseClient.Urls.PROTOCOL)
				.setHost(CommonSenseClient.Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + state.getId() + "/sensors.json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

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
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("GET service sensors request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void getMethods(final ExtSensor state, final List<ExtSensor> sensors) {

		if (sensors.size() > 0) {
			// prepare request properties
			final Method method = RequestBuilder.GET;
			final UrlBuilder urlBuilder = new UrlBuilder().setProtocol(
					CommonSenseClient.Urls.PROTOCOL).setHost(CommonSenseClient.Urls.HOST);
			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensors.get(0).getId() + "/services/"
					+ state.getId() + "/methods.json");
			final String url = urlBuilder.buildString();
			final String sessionId = SessionManager.getSessionId();

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("GET service methods onError callback: " + exception.getMessage());
					onMethodsFailure(0);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("GET service methods response received: " + response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_OK == statusCode) {
						onMethodsSuccess(response.getText(), state, sensors);
					} else {
						LOG.warning("GET service methods returned incorrect status: " + statusCode);
						onMethodsFailure(statusCode);
					}
				}
			};

			// send request
			try {
				RequestBuilder builder = new RequestBuilder(method, url);
				builder.setHeader("X-SESSION_ID", sessionId);
				builder.sendRequest(null, reqCallback);
			} catch (Exception e) {
				LOG.warning("GET service methods request threw exception: " + e.getMessage());
				reqCallback.onError(null, e);
			}

		} else {
			LOG.warning("State \'" + state + "\' has no connected sensors!");
		}
	}

	private void getStateSensors(final AsyncCallback<List<ExtSensor>> callback) {

		// prepare request properties
		final Method method = RequestBuilder.GET;
		final UrlBuilder urlBuilder = new UrlBuilder().setProtocol(CommonSenseClient.Urls.PROTOCOL)
				.setHost(CommonSenseClient.Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + ".json");
		urlBuilder.setParameter("per_page", "1000");
		urlBuilder.setParameter("details", "full");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

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
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("GET sensors request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
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
			final AsyncCallback<List<ExtSensor>> callback = event
					.<AsyncCallback<List<ExtSensor>>> getData("callback");
			load(loadConfig, callback);

		} else

		/*
		 * Disconnect a sensor from a state
		 */
		if (type.equals(StateListEvents.RemoveRequested)) {
			// LOG.fine( "RemoveRequested");
			ExtSensor sensor = event.<ExtSensor> getData("sensor");
			ExtSensor stateSensor = event.<ExtSensor> getData("stateSensor");
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

	private void load(Object loadConfig, AsyncCallback<List<ExtSensor>> callback) {
		forwardToView(tree, new AppEvent(StateListEvents.Working));
		if (null == loadConfig) {
			getStateSensors(callback);
		} else if (loadConfig instanceof ExtSensor && ((ExtSensor) loadConfig).getType() == 2) {
			getConnected((ExtSensor) loadConfig, callback);
		} else {
			onLoadComplete(new ArrayList<ExtSensor>(), callback);
		}
	}

	private void onConnectedFailure(AsyncCallback<List<ExtSensor>> callback) {
		forwardToView(tree, new AppEvent(StateListEvents.Done));
		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onConnectedSuccess(String response, ExtSensor state,
			AsyncCallback<List<ExtSensor>> callback) {

		// parse list of sensors from response
		List<ExtSensor> sensors = new ArrayList<ExtSensor>();
		GetSensorsResponse responseJso = GetSensorsResponse.create(response).cast();
		if (null != responseJso) {
			JsArray<Sensor> rawSensors = responseJso.getRawSensors();
			for (int i = 0; i < rawSensors.length(); i++) {
				ExtSensor sensor = new ExtSensor(rawSensors.get(i));
				sensors.add(sensor);
			}
		}

		// get details from library
		List<ExtSensor> result = new ArrayList<ExtSensor>();
		List<ExtSensor> library = Registry
				.<List<ExtSensor>> get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
		for (ExtSensor sensor : sensors) {
			int index = -1;
			for (ExtSensor libSensor : library) {
				if (libSensor.getId() == sensor.getId()) {
					index = library.indexOf(libSensor);
					break;
				}
			}
			if (index != -1) {
				ExtSensor detailed = (ExtSensor) TreeCopier.copySensor(library.get(index));
				state.add(detailed);
				result.add(detailed);
			} else {
				sensor.setParent(state);
				result.add(sensor);
			}
		}

		// return to view
		onLoadComplete(result, callback);

		// continue getting methods
		getMethods(state, result);
	}

	private void onDisconnectFailure(int code) {
		forwardToView(tree, new AppEvent(StateListEvents.RemoveFailed));
	}

	private void onDisconnectSuccess(String response) {
		Dispatcher.forwardEvent(StateListEvents.RemoveComplete);
	}

	private void onLoadComplete(List<ExtSensor> result, AsyncCallback<List<ExtSensor>> callback) {
		forwardToView(tree, new AppEvent(StateListEvents.Done));
		forwardToView(tree, new AppEvent(StateListEvents.LoadComplete));
		if (null != callback) {
			callback.onSuccess(result);
		}
	}

	private void onLoadFailure(AsyncCallback<List<ExtSensor>> callback) {
		forwardToView(tree, new AppEvent(StateListEvents.Done));
		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onMethodsFailure(int statusCode) {
		// TODO
	}

	private void onMethodsSuccess(String response, ExtSensor state, List<ExtSensor> sensors) {

		// parse list of methods from the response
		JsArray<ServiceMethod> methods = null;
		GetMethodsResponse jso = GetMethodsResponse.create(response).cast();
		if (jso != null) {
			methods = jso.getRawMethods();
		}

		if (null != methods) {
			List<ExtServiceMethod> extMethods = new ArrayList<ExtServiceMethod>(methods.length());
			for (int i = 0; i < methods.length(); i++) {
				extMethods.add(new ExtServiceMethod(methods.get(i)));
			}
			state.set("methods", extMethods);
		}
	}

	private void onStateSensorsFailure(AsyncCallback<List<ExtSensor>> callback) {
		onLoadFailure(callback);
	}

	private void onStateSensorsSuccess(String response, AsyncCallback<List<ExtSensor>> callback) {

		// parse list of sensors from response
		JsArray<Sensor> sensors = null;
		GetSensorsResponse responseJso = GetSensorsResponse.create(response).cast();
		if (null != responseJso) {
			sensors = responseJso.getRawSensors();
		}

		ExtUser user = Registry
				.<ExtUser> get(nl.sense_os.commonsense.common.client.util.Constants.REG_USER);
		List<ExtSensor> states = new ArrayList<ExtSensor>();
		for (int i = 0; i < sensors.length(); i++) {
			ExtSensor sensor = new ExtSensor(sensors.get(i));
			if (sensor.getType() == 2 && user.equals(sensor.getOwner())) {
				states.add(sensor);
			}
		}

		onLoadComplete(states, callback);
	}
}
