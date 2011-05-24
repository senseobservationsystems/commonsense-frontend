package nl.sense_os.commonsense.client.env.create;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.json.parsers.EnvironmentParser;
import nl.sense_os.commonsense.client.common.json.parsers.SensorParser;
import nl.sense_os.commonsense.client.common.models.DeviceModel;
import nl.sense_os.commonsense.client.common.models.EnvironmentModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

public class EnvCreateController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(EnvCreateController.class.getName());
    private View creator;

    public EnvCreateController() {
        registerEventTypes(EnvCreateEvents.ShowCreator);
        registerEventTypes(EnvCreateEvents.OutlineComplete);
        registerEventTypes(EnvCreateEvents.CreateRequest, EnvCreateEvents.CreateSuccess,
                EnvCreateEvents.CreateAjaxSuccess, EnvCreateEvents.CreateAjaxFailure,
                EnvCreateEvents.AddSensorsAjaxSuccess, EnvCreateEvents.AddSensorsAjaxFailure,
                EnvCreateEvents.SetPositionAjaxSuccess, EnvCreateEvents.SetPositionAjaxFailure,
                EnvCreateEvents.CreateSensorAjaxSuccess, EnvCreateEvents.CreateSensorAjaxFailure,
                EnvCreateEvents.SensorToDeviceAjaxSuccess,
                EnvCreateEvents.SensorToDeviceAjaxFailure);

        LOGGER.setLevel(Level.ALL);
    }

    private void addSensors(EnvironmentModel environment, List<SensorModel> sensors) {

        if (false == sensors.isEmpty()) {

            // prepare body
            String sensorsArray = "[";
            for (SensorModel sensor : sensors) {
                if (sensor.getAlias() == null) {
                    sensorsArray += "{\"id\":" + sensor.getId() + "},";
                }
            }
            sensorsArray = sensorsArray.substring(0, sensorsArray.length() - 1) + "]";
            String body = "{\"sensors\":" + sensorsArray + "}";

            // prepare request properties
            final String method = "POST";
            final String url = Urls.ENVIRONMENTS + "/" + environment.getId() + "/sensors.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(EnvCreateEvents.AddSensorsAjaxSuccess);
            onSuccess.setData("environment", environment);
            onSuccess.setData("sensors", sensors);
            final AppEvent onFailure = new AppEvent(EnvCreateEvents.AddSensorsAjaxFailure);
            onFailure.setData("environment", environment);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("body", body);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);

            Dispatcher.forwardEvent(ajaxRequest);

        } else {
            onCreateComplete();
        }
    }

    private void addSensorToDevice(SensorModel sensor, List<DeviceModel> devices, int index,
            String name, int floors, Polygon outline, List<SensorModel> sensors) {

        DeviceModel device = devices.get(index);

        // prepare body
        String body = "{\"device\":{";
        body += "\"" + DeviceModel.ID + "\":\"" + device.getId() + "\",";
        body += "\"" + DeviceModel.TYPE + "\":\"" + device.getType() + "\",";
        body += "\"" + DeviceModel.UUID + "\":\"" + device.getUuid() + "\"}";
        body += "}";

        // prepare request properties
        final String method = "POST";
        final String url = Urls.SENSORS + "/" + sensor.getId() + "/device.json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(EnvCreateEvents.SensorToDeviceAjaxSuccess);
        onSuccess.setData("sensor", sensor);
        onSuccess.setData("devices", devices);
        onSuccess.setData("index", index);
        onSuccess.setData("name", name);
        onSuccess.setData("floors", floors);
        onSuccess.setData("outline", outline);
        onSuccess.setData("sensors", sensors);
        final AppEvent onFailure = new AppEvent(EnvCreateEvents.SensorToDeviceAjaxFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void createEnvironment(String name, int floors, Polygon outline,
            List<SensorModel> sensors) {

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
        final String method = "POST";
        final String url = Urls.ENVIRONMENTS + ".json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(EnvCreateEvents.CreateAjaxSuccess);
        onSuccess.setData("sensors", sensors);
        final AppEvent onFailure = new AppEvent(EnvCreateEvents.CreateAjaxFailure);

        String body = "{\"environment\":{";
        body += "\"" + EnvironmentModel.NAME + "\":\"" + name + "\",";
        body += "\"" + EnvironmentModel.FLOORS + "\":" + floors + ",";
        body += "\"" + EnvironmentModel.OUTLINE + "\":\"" + gpsOutline + "\",";
        body += "\"" + EnvironmentModel.POSITION + "\":\"" + position + "\"}";
        body += "}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void createSensor(List<DeviceModel> devices, int index, String name, int floors,
            Polygon outline, List<SensorModel> sensors) {

        // prepare body
        String dataStructure = "{\\\"latitude\\\":\\\"string\\\",\\\"longitude\\\":\\\"string\\\",\\\"altitude\\\":\\\"string\\\"}";
        String sensor = "{";
        sensor += "\"" + SensorModel.NAME + "\":\"position\",";
        sensor += "\"" + SensorModel.DISPLAY_NAME + "\":\"position\",";
        sensor += "\"" + SensorModel.PHYSICAL_SENSOR + "\":\"position\",";
        sensor += "\"" + SensorModel.DATA_TYPE + "\":\"json\",";
        sensor += "\"" + SensorModel.DATA_STRUCTURE + "\":\"" + dataStructure + "\"";
        sensor += "}";
        String body = "{\"sensor\":" + sensor + "}";

        // prepare request properties
        final String method = "POST";
        final String url = Urls.SENSORS + ".json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(EnvCreateEvents.CreateSensorAjaxSuccess);
        onSuccess.setData("devices", devices);
        onSuccess.setData("index", index);
        onSuccess.setData("name", name);
        onSuccess.setData("floors", floors);
        onSuccess.setData("outline", outline);
        onSuccess.setData("sensors", sensors);
        final AppEvent onFailure = new AppEvent(EnvCreateEvents.CreateSensorAjaxFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void getPositionSensor(List<DeviceModel> devices, int index, String name, int floors,
            Polygon outline, List<SensorModel> sensors) {

        // get the device
        DeviceModel device = devices.get(index);

        // try to find the position sensor of the device
        SensorModel positionSensor = null;
        for (SensorModel sensor : sensors) {
            // only check position sensors
            if (sensor.getName().equals("position")) {
                // check if it is the right device
                if (sensor.getDevice() != null && sensor.getDevice().equals(device)) {
                    // make sure we are the owner of the sensor
                    UserModel user = Registry.get(Constants.REG_USER);
                    if (sensor.getOwner() == null || sensor.getOwner().equals(user)) {
                        positionSensor = sensor;
                        break;
                    }
                }
            }
        }

        if (null != positionSensor) {
            // position sensor present: set its position
            LOGGER.finest("Found position sensor for \'" + device + "\'");
            setPosition(positionSensor, devices, index, name, floors, outline, sensors);
        } else {
            // device has no position sensor yet: create it
            LOGGER.finest("Did not find position sensor for \'" + device + "\'");
            createSensor(devices, index, name, floors, outline, sensors);
        }
    }
    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(EnvCreateEvents.CreateRequest)) {
            LOGGER.finest("CreateRequest");
            final String name = event.<String> getData("name");
            final int floors = event.getData("floors");
            final Polygon outline = event.<Polygon> getData("outline");
            final List<DeviceModel> devices = event.<List<DeviceModel>> getData("devices");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onCreateRequest(name, floors, outline, devices, sensors);

        } else

        if (type.equals(EnvCreateEvents.CreateAjaxSuccess)) {
            LOGGER.finest("CreateAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onCreateEnvironmentSuccess(response, sensors);

        } else if (type.equals(EnvCreateEvents.CreateAjaxFailure)) {
            LOGGER.warning("CreateAjaxFailure");
            // final int code = event.getData("code");
            onCreateEnvironmentFailure();

        } else

        if (type.equals(EnvCreateEvents.AddSensorsAjaxSuccess)) {
            LOGGER.finest("AddSensorsAjaxSuccess");
            // final String response = event.<String> getData("response");
            final EnvironmentModel environment = event.getData("environment");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onAddSensorSuccess(environment, sensors);

        } else if (type.equals(EnvCreateEvents.AddSensorsAjaxFailure)) {
            LOGGER.warning("AddSensorsAjaxFailure");
            // final int code = event.getData("code");
            final EnvironmentModel environment = event.getData("environment");
            onAddSensorsFailure(environment);

        } else

        if (type.equals(EnvCreateEvents.CreateSensorAjaxSuccess)) {
            LOGGER.finest("CreateSensorAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<DeviceModel> devices = event.getData("devices");
            final int index = event.getData("index");
            final String name = event.<String> getData("name");
            final int floors = event.getData("floors");
            final Polygon outline = event.<Polygon> getData("outline");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onCreateSensorSuccess(response, devices, index, name, floors, outline, sensors);

        } else if (type.equals(EnvCreateEvents.CreateSensorAjaxFailure)) {
            LOGGER.warning("CreateSensorAjaxFailure");
            // final int code = event.getData("code");
            onCreateSensorFailure();

        } else

        if (type.equals(EnvCreateEvents.SensorToDeviceAjaxSuccess)) {
            LOGGER.finest("SensorToDeviceAjaxSuccess");
            final String response = event.getData("response");
            final SensorModel sensor = event.getData("sensor");
            final List<DeviceModel> devices = event.getData("devices");
            final int index = event.getData("index");
            final String name = event.<String> getData("name");
            final int floors = event.getData("floors");
            final Polygon outline = event.<Polygon> getData("outline");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onSensorToDeviceSuccess(response, sensor, devices, index, name, floors, outline,
                    sensors);

        } else if (type.equals(EnvCreateEvents.SensorToDeviceAjaxFailure)) {
            LOGGER.warning("SensorToDeviceAjaxFailure");
            // final int code = event.getData("code");
            onSensorToDeviceFailure();

        } else

        if (type.equals(EnvCreateEvents.SetPositionAjaxSuccess)) {
            LOGGER.finest("SetPositionAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<DeviceModel> devices = event.getData("devices");
            final int index = event.getData("index");
            final String name = event.<String> getData("name");
            final int floors = event.getData("floors");
            final Polygon outline = event.<Polygon> getData("outline");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onSetPositionSuccess(response, devices, index, name, floors, outline, sensors);

        } else if (type.equals(EnvCreateEvents.SetPositionAjaxFailure)) {
            LOGGER.warning("SetPositionAjaxFailure");
            // final int code = event.getData("code");
            onSetPositionFailure();

        } else

        {
            forwardToView(this.creator, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.creator = new EnvCreator(this);
    }

    private void onAddSensorsFailure(EnvironmentModel environment) {
        onCreateFailure();
    }

    private void onAddSensorSuccess(EnvironmentModel environment, List<SensorModel> sensors) {

        // update the sensors with the new environment
        for (SensorModel sensor : sensors) {
            sensor.set(SensorModel.ENVIRONMENT, environment);
        }

        onCreateComplete();
    }

    private void onCreateComplete() {
        Dispatcher.forwardEvent(EnvCreateEvents.CreateSuccess);
    }

    private void onCreateEnvironmentFailure() {
        onCreateFailure();
    }

    private void onCreateEnvironmentSuccess(String response, List<SensorModel> sensors) {
        JSONObject responseJson = JSONParser.parseLenient(response).isObject();
        if (null != responseJson) {
            JSONObject envJson = responseJson.get("environment").isObject();
            EnvironmentModel environment = EnvironmentParser.parse(envJson);

            // update global environment list
            Registry.<List<EnvironmentModel>> get(Constants.REG_ENVIRONMENT_LIST).add(environment);

            // continue with adding sensors
            addSensors(environment, sensors);

        } else {
            onCreateEnvironmentFailure();
        }
    }

    private void onCreateFailure() {
        forwardToView(this.creator, new AppEvent(EnvCreateEvents.CreateFailure));

    }

    private void onCreateRequest(String name, int floors, Polygon outline,
            List<DeviceModel> devices, List<SensorModel> sensors) {

        // add the devices's sensors
        List<SensorModel> library = Registry.get(Constants.REG_SENSOR_LIST);
        for (SensorModel sensor : library) {
            if (sensor.getDevice() != null && devices.contains(sensor.getDevice())) {
                LOGGER.finest("Add device sensor \'" + sensor + "\' to list of environment sensors");
                sensors.add(sensor);
            }
        }

        // start by updating the position of all devices
        updatePosition(devices, 0, name, floors, outline, sensors);
    }

    private void onCreateSensorFailure() {
        onCreateEnvironmentFailure();
    }

    private void onCreateSensorSuccess(String response, List<DeviceModel> devices, int index,
            String name, int floors, Polygon outline, List<SensorModel> sensors) {

        // parse the new sensor details from the response
        JSONValue rawJson = JSONParser.parseLenient(response);
        if (null != rawJson && null != rawJson.isObject()) {
            JSONObject sensorJson = rawJson.isObject().get("sensor").isObject();
            SensorModel positionSensor = SensorParser.parseSensor(sensorJson);

            // add the new sensor to the list of sensors for this environment
            sensors.add(positionSensor);

            // add the new sensor to the global library
            Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST).add(positionSensor);

            // add the new position sensor to the proper device
            addSensorToDevice(positionSensor, devices, index, name, floors, outline, sensors);

        } else {
            onCreateSensorFailure();
        }
    }

    private void onSensorToDeviceFailure() {
        onCreateFailure();
    }

    private void onSensorToDeviceSuccess(String response, SensorModel sensor,
            List<DeviceModel> devices, int index, String name, int floors, Polygon outline,
            List<SensorModel> sensors) {

        // update the sensor model
        sensor.set(SensorModel.DEVICE, devices.get(index));
        sensor.set(SensorModel.TYPE, "1");

        setPosition(sensor, devices, index, name, floors, outline, sensors);
    }

    private void onSetPositionFailure() {
        onCreateFailure();
    }

    private void onSetPositionSuccess(String response, List<DeviceModel> devices, int index,
            String name, int floors, Polygon outline, List<SensorModel> sensors) {
        index++;
        updatePosition(devices, index, name, floors, outline, sensors);
    }

    private void setPosition(SensorModel positionSensor, List<DeviceModel> devices, int index,
            String name, int floors, Polygon outline, List<SensorModel> sensors) {

        DeviceModel device = devices.get(index);
        LatLng latLng = device.<LatLng> get("latlng");

        // prepare request properties
        final String method = "POST";
        final String url = Urls.SENSORS + "/" + positionSensor.getId() + "/data.json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(EnvCreateEvents.SetPositionAjaxSuccess);
        onSuccess.setData("devices", devices);
        onSuccess.setData("index", index);
        onSuccess.setData("name", name);
        onSuccess.setData("floors", floors);
        onSuccess.setData("outline", outline);
        onSuccess.setData("sensors", sensors);
        final AppEvent onFailure = new AppEvent(EnvCreateEvents.SetPositionAjaxFailure);

        String value = "{\\\"latitude\\\":" + latLng.getLatitude() + ",\\\"longitude\\\":"
                + latLng.getLongitude() + ",\\\"provider\\\":\\\"environment\\\"}";
        String body = "{\"data\":[";
        body += "{\"value\":\"" + value + "\",\"date\":"
                + NumberFormat.getFormat("#.#").format(System.currentTimeMillis() / 1000) + "}";
        body += "]}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void updatePosition(List<DeviceModel> devices, int index, String name, int floors,
            Polygon outline, List<SensorModel> sensors) {

        if (index < devices.size()) {
            // update the position sensor for the device
            getPositionSensor(devices, index, name, floors, outline, sensors);

        } else {
            // all devices are positioned! continue with actually creating the environment
            createEnvironment(name, floors, outline, sensors);
        }
    }
}
