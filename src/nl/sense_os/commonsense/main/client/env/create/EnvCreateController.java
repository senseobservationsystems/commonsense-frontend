package nl.sense_os.commonsense.main.client.env.create;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.communication.httpresponse.CreateEnvironmentResponse;
import nl.sense_os.commonsense.common.client.communication.httpresponse.CreateSensorResponse;
import nl.sense_os.commonsense.common.client.constant.Constants;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.common.client.model.ExtDevice;
import nl.sense_os.commonsense.common.client.model.ExtEnvironment;
import nl.sense_os.commonsense.common.client.model.ExtSensor;
import nl.sense_os.commonsense.common.client.model.ExtUser;
import nl.sense_os.commonsense.common.client.model.Sensor;

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
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

public class EnvCreateController extends Controller {

	private static final Logger LOG = Logger.getLogger(EnvCreateController.class.getName());
	private View creator;

	public EnvCreateController() {
		registerEventTypes(EnvCreateEvents.ShowCreator);
		registerEventTypes(EnvCreateEvents.OutlineComplete);
		registerEventTypes(EnvCreateEvents.CreateRequest, EnvCreateEvents.CreateSuccess);
	}

	private void addSensors(final ExtEnvironment environment, final List<ExtSensor> sensors) {

		if (false == sensors.isEmpty()) {

			// prepare body
			String sensorsArray = "[";
			for (ExtSensor sensor : sensors) {
				if (sensor.getAlias() == -1) {
					sensorsArray += "{\"id\":" + sensor.getId() + "},";
				}
			}
			sensorsArray = sensorsArray.substring(0, sensorsArray.length() - 1) + "]";
			String body = "{\"sensors\":" + sensorsArray + "}";

			// prepare request properties
			final Method method = RequestBuilder.POST;
			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
			urlBuilder.setPath(Urls.PATH_ENV + "/" + environment.getId() + "/sensors.json");
			final String url = urlBuilder.buildString();
			final String sessionId = SessionManager.getSessionId();

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("POST environment sensors onError callback: "
							+ exception.getMessage());
					onAddSensorsFailure(environment);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("POST environment sensors response received: "
							+ response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_CREATED == statusCode) {
						onAddSensorSuccess(environment, sensors);
					} else {
						LOG.warning("POST environment sensors returned incorrect status: "
								+ statusCode);
						onAddSensorsFailure(environment);
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
				LOG.warning("POST environment sensors request threw exception: " + e.getMessage());
				reqCallback.onError(null, e);
			}

		} else {
			onCreateComplete();
		}
	}

	private void addSensorToDevice(final ExtSensor sensor, final List<ExtDevice> devices,
			final int index, final String name, final int floors, final Polygon outline,
			final List<ExtSensor> sensors) {

		ExtDevice device = devices.get(index);

		// prepare body
		String body = "{\"device\":{";
		body += "\"" + ExtDevice.ID + "\":\"" + device.getId() + "\",";
		body += "\"" + ExtDevice.TYPE + "\":\"" + device.getType() + "\",";
		body += "\"" + ExtDevice.UUID + "\":\"" + device.getUuid() + "\"}";
		body += "}";

		// prepare request properties
		final Method method = RequestBuilder.POST;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/device.json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST sensor device onError callback: " + exception.getMessage());
				onSensorToDeviceFailure();
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("POST sensor device response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_CREATED == statusCode) {
					onSensorToDeviceSuccess(response.getText(), sensor, devices, index, name,
							floors, outline, sensors);
				} else {
					LOG.warning("POST sensor device returned incorrect status: " + statusCode);
					onSensorToDeviceFailure();
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
			LOG.warning("POST sensor device request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void createEnvironment(String name, int floors, Polygon outline,
			final List<ExtSensor> sensors) {

		// create GPS outline String
		String gpsOutline = "";
		for (int i = 0; i < outline.getVertexCount(); i++) {
			LatLng vertex = outline.getVertex(i);
			gpsOutline += vertex.toUrlValue() + ";";
		}
		gpsOutline = gpsOutline.substring(0, gpsOutline.length() - 1);

		// create GPS position String
		String position = outline.getBounds().getCenter().toUrlValue();

		// prepare request properties
		final Method method = RequestBuilder.POST;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_ENV + ".json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		String body = "{\"environment\":{";
		body += "\"" + ExtEnvironment.NAME + "\":\"" + name + "\",";
		body += "\"" + ExtEnvironment.FLOORS + "\":" + floors + ",";
		body += "\"" + ExtEnvironment.OUTLINE + "\":\"" + gpsOutline + "\",";
		body += "\"" + ExtEnvironment.POSITION + "\":\"" + position + "\"}";
		body += "}";

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST environments onError callback: " + exception.getMessage());
				onCreateEnvironmentFailure();
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("POST environments response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_CREATED == statusCode) {
					onCreateEnvironmentSuccess(response.getText(), sensors);
				} else {
					LOG.warning("POST environments returned incorrect status: " + statusCode);
					onCreateEnvironmentFailure();
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
			LOG.warning("POST environments request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void createSensor(final List<ExtDevice> devices, final int index, final String name,
			final int floors, final Polygon outline, final List<ExtSensor> sensors) {

		// prepare body
		String dataStructure = "{\\\"latitude\\\":\\\"string\\\",\\\"longitude\\\":\\\"string\\\",\\\"altitude\\\":\\\"string\\\"}";
		String sensor = "{";
		sensor += "\"" + ExtSensor.NAME + "\":\"position\",";
		sensor += "\"" + ExtSensor.DISPLAY_NAME + "\":\"position\",";
		sensor += "\"" + ExtSensor.DESCRIPTION + "\":\"position\",";
		sensor += "\"" + ExtSensor.DATA_TYPE + "\":\"json\",";
		sensor += "\"" + ExtSensor.DATA_STRUCTURE + "\":\"" + dataStructure + "\"";
		sensor += "}";
		String body = "{\"sensor\":" + sensor + "}";

		// prepare request properties
		final Method method = RequestBuilder.POST;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + ".json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST sensor onError callback: " + exception.getMessage());
				onCreateSensorFailure();
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("POST sensor response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_CREATED == statusCode) {
					onCreateSensorSuccess(response.getText(), devices, index, name, floors,
							outline, sensors);
				} else {
					LOG.warning("POST sensor returned incorrect status: " + statusCode);
					onCreateSensorFailure();
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
			LOG.warning("POST sensor request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void getPositionSensor(List<ExtDevice> devices, int index, String name, int floors,
			Polygon outline, List<ExtSensor> sensors) {

		// get the device
		ExtDevice device = devices.get(index);

		// try to find the position sensor of the device
		ExtSensor positionSensor = null;
		for (ExtSensor sensor : sensors) {
			// only check position sensors
			if (sensor.getName().equals("position")) {
				// check if it is the right device
				if (sensor.getDevice() != null && sensor.getDevice().equals(device)) {
					// make sure we are the owner of the sensor
					ExtUser user = Registry.get(Constants.REG_USER);
					if (sensor.getOwner() == null || sensor.getOwner().equals(user)) {
						positionSensor = sensor;
						break;
					}
				}
			}
		}

		if (null != positionSensor) {
			// position sensor present: set its position
			LOG.finest("Found position sensor for \'" + device + "\'");
			setPosition(positionSensor, devices, index, name, floors, outline, sensors);
		} else {
			// device has no position sensor yet: create it
			LOG.finest("Did not find position sensor for \'" + device + "\'");
			createSensor(devices, index, name, floors, outline, sensors);
		}
	}

	@Override
	public void handleEvent(AppEvent event) {
		final EventType type = event.getType();

		if (type.equals(EnvCreateEvents.CreateRequest)) {
			LOG.finest("CreateRequest");
			final String name = event.<String> getData("name");
			final int floors = event.getData("floors");
			final Polygon outline = event.<Polygon> getData("outline");
			final List<ExtDevice> devices = event.<List<ExtDevice>> getData("devices");
			final List<ExtSensor> sensors = event.<List<ExtSensor>> getData("sensors");
			onCreateRequest(name, floors, outline, devices, sensors);

		} else

		/*
		 * Pass through to view
		 */
		{
			forwardToView(creator, event);
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		creator = new EnvCreator(this);
	}

	private void onAddSensorsFailure(ExtEnvironment environment) {
		onCreateFailure();
	}

	private void onAddSensorSuccess(ExtEnvironment environment, List<ExtSensor> sensors) {

		// update the sensors with the new environment
		for (ExtSensor sensor : sensors) {
			sensor.set(ExtSensor.ENVIRONMENT, environment);
		}

		onCreateComplete();
	}

	private void onCreateComplete() {
		Dispatcher.forwardEvent(EnvCreateEvents.CreateSuccess);
	}

	private void onCreateEnvironmentFailure() {
		onCreateFailure();
	}

	private void onCreateEnvironmentSuccess(String response, List<ExtSensor> sensors) {

		// parse the response
		ExtEnvironment environment = null;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			CreateEnvironmentResponse jso = JsonUtils.unsafeEval(response);
			environment = new ExtEnvironment(jso.getEnvironment());
		}

		if (null != environment) {

			// update global environment list
			Registry.<List<ExtEnvironment>> get(Constants.REG_ENVIRONMENT_LIST).add(environment);

			// continue with adding sensors
			addSensors(environment, sensors);

		} else {
			onCreateEnvironmentFailure();
		}
	}

	private void onCreateFailure() {
		forwardToView(creator, new AppEvent(EnvCreateEvents.CreateFailure));

	}

	private void onCreateRequest(String name, int floors, Polygon outline, List<ExtDevice> devices,
			List<ExtSensor> sensors) {

		// add the devices's sensors
		List<ExtSensor> library = Registry.get(Constants.REG_SENSOR_LIST);
		for (ExtSensor sensor : library) {
			if (sensor.getDevice() != null && devices.contains(sensor.getDevice())) {
				LOG.finest("Add device sensor \'" + sensor + "\' to list of environment sensors");
				sensors.add(sensor);
			}
		}

		// start by updating the position of all devices
		updatePosition(devices, 0, name, floors, outline, sensors);
	}

	private void onCreateSensorFailure() {
		onCreateEnvironmentFailure();
	}

	private void onCreateSensorSuccess(String response, List<ExtDevice> devices, int index,
			String name, int floors, Polygon outline, List<ExtSensor> sensors) {

		// parse the new sensor details from the response
		Sensor positionSensor = null;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			CreateSensorResponse jso = JsonUtils.unsafeEval(response);
			positionSensor = jso.getSensor();
		}

		if (null != positionSensor) {

			ExtSensor extSensor = new ExtSensor(positionSensor);

			// add the new sensor to the list of sensors for this environment
			sensors.add(extSensor);

			// add the new sensor to the global library
			Registry.<List<ExtSensor>> get(Constants.REG_SENSOR_LIST).add(extSensor);

			// add the new position sensor to the proper device
			addSensorToDevice(extSensor, devices, index, name, floors, outline, sensors);

		} else {
			onCreateSensorFailure();
		}
	}

	private void onSensorToDeviceFailure() {
		onCreateFailure();
	}

	private void onSensorToDeviceSuccess(String response, ExtSensor sensor,
			List<ExtDevice> devices, int index, String name, int floors, Polygon outline,
			List<ExtSensor> sensors) {

		// update the sensor model
		sensor.setDevice(devices.get(index));
		sensor.setType(1);

		setPosition(sensor, devices, index, name, floors, outline, sensors);
	}

	private void onSetPositionFailure() {
		onCreateFailure();
	}

	private void onSetPositionSuccess(String response, List<ExtDevice> devices, int index,
			String name, int floors, Polygon outline, List<ExtSensor> sensors) {
		index++;
		updatePosition(devices, index, name, floors, outline, sensors);
	}

	private void setPosition(ExtSensor positionSensor, final List<ExtDevice> devices,
			final int index, final String name, final int floors, final Polygon outline,
			final List<ExtSensor> sensors) {

		ExtDevice device = devices.get(index);
		LatLng latLng = device.<LatLng> get("latlng");

		// prepare request properties
		final Method method = RequestBuilder.POST;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + positionSensor.getId() + "/data.json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		String value = "{\\\"latitude\\\":" + latLng.getLatitude() + ",\\\"longitude\\\":"
				+ latLng.getLongitude() + ",\\\"provider\\\":\\\"environment\\\"}";
		String body = "{\"data\":[";
		body += "{\"value\":\"" + value + "\",\"date\":"
				+ NumberFormat.getFormat("#.#").format(System.currentTimeMillis() / 1000) + "}";
		body += "]}";

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST position onError callback: " + exception.getMessage());
				onSetPositionFailure();
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("POST position response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_CREATED == statusCode) {
					onSetPositionSuccess(response.getText(), devices, index, name, floors, outline,
							sensors);
				} else {
					LOG.warning("POST position returned incorrect status: " + statusCode);
					onSetPositionFailure();
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
			LOG.warning("POST position request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void updatePosition(List<ExtDevice> devices, int index, String name, int floors,
			Polygon outline, List<ExtSensor> sensors) {

		if (index < devices.size()) {
			// update the position sensor for the device
			getPositionSensor(devices, index, name, floors, outline, sensors);

		} else {
			// all devices are positioned! continue with actually creating the environment
			createEnvironment(name, floors, outline, sensors);
		}
	}
}
