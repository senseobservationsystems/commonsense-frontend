package nl.sense_os.commonsense.main.client.states.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.constant.Constants;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.common.client.httpresponse.GetMethodsResponseJso;
import nl.sense_os.commonsense.common.client.httpresponse.GetSensorsResponseJso;
import nl.sense_os.commonsense.common.client.model.SensorModel;
import nl.sense_os.commonsense.common.client.model.ServiceMethodModel;
import nl.sense_os.commonsense.common.client.model.UserModel;
import nl.sense_os.commonsense.common.client.util.SessionManager;
import nl.sense_os.commonsense.common.client.util.TreeCopier;
import nl.sense_os.commonsense.main.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.main.client.main.MainEvents;
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
import com.google.gwt.core.client.JsonUtils;
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
		registerEventTypes(MainEvents.Init);
		registerEventTypes(VizEvents.Show);
		registerEventTypes(LoginEvents.LoggedOut);
		registerEventTypes(StateListEvents.ShowGrid);

		// external triggers to initiate a list update
		registerEventTypes(StateCreateEvents.CreateServiceComplete,
				StateConnectEvents.ConnectSuccess, SensorDeleteEvents.DeleteSuccess,
				StateDefaultsEvents.CheckDefaultsSuccess);

		// events to update the list of groups
		registerEventTypes(StateListEvents.LoadRequest);

		registerEventTypes(StateListEvents.RemoveRequested, StateListEvents.RemoveComplete);
	}

	private void disconnectService(SensorModel sensor, SensorModel stateSensor) {

		// prepare request data
		final Method method = RequestBuilder.DELETE;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
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

	private void getConnected(final SensorModel state,
			final AsyncCallback<List<SensorModel>> callback) {

		// prepare request properties
		final Method method = RequestBuilder.GET;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
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

	private void getMethods(final SensorModel state, final List<SensorModel> sensors) {

		if (sensors.size() > 0) {
			// prepare request properties
			final Method method = RequestBuilder.GET;
			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
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

	private void getStateSensors(final AsyncCallback<List<SensorModel>> callback) {

		// prepare request properties
		final Method method = RequestBuilder.GET;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
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
			final AsyncCallback<List<SensorModel>> callback = event
					.<AsyncCallback<List<SensorModel>>> getData("callback");
			load(loadConfig, callback);

		} else

		/*
		 * Disconnect a sensor from a state
		 */
		if (type.equals(StateListEvents.RemoveRequested)) {
			// LOG.fine( "RemoveRequested");
			SensorModel sensor = event.<SensorModel> getData("sensor");
			SensorModel stateSensor = event.<SensorModel> getData("stateSensor");
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

	private void load(Object loadConfig, AsyncCallback<List<SensorModel>> callback) {
		forwardToView(tree, new AppEvent(StateListEvents.Working));
		if (null == loadConfig) {
			getStateSensors(callback);
		} else if (loadConfig instanceof SensorModel && ((SensorModel) loadConfig).getType() == 2) {
			getConnected((SensorModel) loadConfig, callback);
		} else {
			onLoadComplete(new ArrayList<SensorModel>(), callback);
		}
	}

	private void onConnectedFailure(AsyncCallback<List<SensorModel>> callback) {
		forwardToView(tree, new AppEvent(StateListEvents.Done));
		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onConnectedSuccess(String response, SensorModel state,
			AsyncCallback<List<SensorModel>> callback) {

		// parse list of sensors from response
		List<SensorModel> sensors = new ArrayList<SensorModel>();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
			sensors.addAll(responseJso.getSensors());
		}

		// get details from library
		List<SensorModel> result = new ArrayList<SensorModel>();
		List<SensorModel> library = Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST);
		for (SensorModel sensor : sensors) {
			int index = -1;
			for (SensorModel libSensor : library) {
				if (libSensor.getId() == sensor.getId()) {
					index = library.indexOf(libSensor);
					break;
				}
			}
			if (index != -1) {
				SensorModel detailed = (SensorModel) TreeCopier.copySensor(library.get(index));
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

	private void onLoadComplete(List<SensorModel> result, AsyncCallback<List<SensorModel>> callback) {
		forwardToView(tree, new AppEvent(StateListEvents.Done));
		forwardToView(tree, new AppEvent(StateListEvents.LoadComplete));
		if (null != callback) {
			callback.onSuccess(result);
		}
	}

	private void onLoadFailure(AsyncCallback<List<SensorModel>> callback) {
		forwardToView(tree, new AppEvent(StateListEvents.Done));
		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onMethodsFailure(int statusCode) {
		// TODO
	}

	private void onMethodsSuccess(String response, SensorModel state, List<SensorModel> sensors) {

		// parse list of methods from the response
		List<ServiceMethodModel> methods = new ArrayList<ServiceMethodModel>();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetMethodsResponseJso jso = JsonUtils.unsafeEval(response);
			methods = jso.getMethods();
		}

		if (null != methods) {
			state.set("methods", methods);
		}
	}

	private void onStateSensorsFailure(AsyncCallback<List<SensorModel>> callback) {
		onLoadFailure(callback);
	}

	private void onStateSensorsSuccess(String response, AsyncCallback<List<SensorModel>> callback) {

		// parse list of sensors from response
		List<SensorModel> sensors = new ArrayList<SensorModel>();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
			sensors.addAll(responseJso.getSensors());
		}

		UserModel user = Registry.<UserModel> get(Constants.REG_USER);
		List<SensorModel> states = new ArrayList<SensorModel>();
		for (SensorModel sensor : sensors) {
			if (sensor.getType() == 2 && user.equals(sensor.getOwner())) {
				states.add(sensor);
			}
		}

		onLoadComplete(states, callback);
	}
}
