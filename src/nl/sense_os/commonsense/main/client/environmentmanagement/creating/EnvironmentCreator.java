package nl.sense_os.commonsense.main.client.environmentmanagement.creating;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.lib.client.model.apiclass.Sensor;
import nl.sense_os.commonsense.lib.client.model.httpresponse.CreateEnvironmentResponse;
import nl.sense_os.commonsense.lib.client.model.httpresponse.CreateSensorResponse;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.main.client.environmentmanagement.creating.EnvironmentCreatorView.Presenter;
import nl.sense_os.commonsense.main.client.gxt.model.GxtDevice;
import nl.sense_os.commonsense.main.client.gxt.model.GxtEnvironment;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

public class EnvironmentCreator implements Presenter {

    private static final Logger LOG = Logger.getLogger(EnvironmentCreator.class.getName());
    private EnvironmentCreatorView creatorWindow;

    public EnvironmentCreator(MainClientFactory clientFactory) {

    }

    @Override
    public void onCancelClick() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSubmitClick() {

        String name = creatorWindow.getName();
        int floors = creatorWindow.getFloors();
        Polygon outline = creatorWindow.getOutline();
        List<GxtSensor> sensors = creatorWindow.getSensors();
        List<GxtDevice> devices = creatorWindow.getDevices();

        // add the devices's sensors
        List<GxtSensor> library = Registry.get(Constants.REG_SENSOR_LIST);
        for (GxtSensor sensor : library) {
            if (sensor.getDevice() != null && devices.contains(sensor.getDevice())) {
                LOG.finest("Add device sensor \'" + sensor + "\' to list of environment sensors");
                sensors.add(sensor);
            }
        }

        // start by updating the position of all devices
        updatePosition(devices, 0, name, floors, outline, sensors);

    }

    private void updatePosition(List<GxtDevice> devices, int index, String name, int floors,
            Polygon outline, List<GxtSensor> sensors) {

        if (index < devices.size()) {
            // update the position sensor for the device
            getPositionSensor(devices, index, name, floors, outline, sensors);

        } else {
            // all devices are positioned! continue with actually creating the environment
            createEnvironment(name, floors, outline, sensors);
        }
    }

    private void getPositionSensor(List<GxtDevice> devices, int index, String name, int floors,
            Polygon outline, List<GxtSensor> sensors) {

        // get the device
        GxtDevice device = devices.get(index);

        // try to find the position sensor of the device
        GxtSensor positionSensor = null;
        for (GxtSensor sensor : sensors) {
            // only check position sensors
            if (sensor.getName().equals("position")) {
                // check if it is the right device
                if (sensor.getDevice() != null && sensor.getDevice().equals(device)) {
                    // make sure we are the owner of the sensor
                    GxtUser user = Registry.get(Constants.REG_USER);
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

    public void start() {
        // TODO Auto-generated method stub

    }

    private void addSensors(final GxtEnvironment environment, final List<GxtSensor> sensors) {

        if (false == sensors.isEmpty()) {

            List<String> sensorIds = new ArrayList<String>();
            for (GxtSensor sensor : sensors) {
                sensorIds.add(sensor.getId());
            }

            // prepare request callback
            RequestCallback callback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    onAddSensorsFailure(environment, -1, exception);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    int statusCode = response.getStatusCode();
                    if (Response.SC_CREATED == statusCode) {
                        onAddSensorSuccess(environment, sensors);
                    } else {
                        onAddSensorsFailure(environment, statusCode,
                                new Throwable(response.getStatusText()));
                    }
                }
            };

            // send request
            CommonSenseClient.getClient().addEnvironmentSensors(callback, environment.getId(),
                    sensorIds);

        } else {
            onCreateComplete();
        }
    }

    private void addSensorToDevice(final GxtSensor sensor, final List<GxtDevice> devices,
            final int index, final String name, final int floors, final Polygon outline,
            final List<GxtSensor> sensors) {

        GxtDevice device = devices.get(index);

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                onSensorToDeviceFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                int statusCode = response.getStatusCode();
                if (Response.SC_CREATED == statusCode) {
                    onSensorToDeviceSuccess(response.getText(), sensor, devices, index, name,
                            floors, outline, sensors);
                } else {
                    onSensorToDeviceFailure(statusCode, new Throwable(response.getStatusText()));
                }
            }
        };

        // send request
        CommonSenseClient.getClient().addSensorDevice(callback, sensor.getId(), device.getId(),
                device.getType(),
                device.getUuid());
    }

    private void createEnvironment(String name, int floors, Polygon outline,
            final List<GxtSensor> sensors) {

        // create GPS outline String
        String gpsOutline = "";
        for (int i = 0; i < outline.getVertexCount(); i++) {
            LatLng vertex = outline.getVertex(i);
            gpsOutline += vertex.toUrlValue() + ";";
        }
        gpsOutline = gpsOutline.substring(0, gpsOutline.length() - 1);

        // create GPS position String
        String position = outline.getBounds().getCenter().toUrlValue();

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                onCreateEnvironmentFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                int statusCode = response.getStatusCode();
                if (Response.SC_CREATED == statusCode) {
                    onCreateEnvironmentSuccess(response.getText(), sensors);
                } else {
                    onCreateEnvironmentFailure(statusCode, new Throwable(response.getStatusText()));
                }
            }
        };

        // send request
        CommonSenseClient.getClient().createEnvironment(callback, name, floors, gpsOutline,
                position);
    }

    private void createSensor(final List<GxtDevice> devices, final int index, final String name,
            final int floors, final Polygon outline, final List<GxtSensor> sensors) {

        String dataStructure = "{\\\"latitude\\\":\\\"string\\\",\\\"longitude\\\":\\\"string\\\",\\\"altitude\\\":\\\"string\\\"}";

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                onCreateSensorFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                int statusCode = response.getStatusCode();
                if (Response.SC_CREATED == statusCode) {
                    onCreateSensorSuccess(response.getText(), devices, index, name, floors,
                            outline, sensors);
                } else {
                    onCreateSensorFailure(statusCode, new Throwable(response.getStatusText()));
                }
            }
        };

        // send request
        CommonSenseClient.getClient().createSensor(callback, "position", "position", "position",
                "json", dataStructure);
    }

    private void onAddSensorsFailure(GxtEnvironment environment, int code, Throwable error) {
        onCreateFailure(code, error);
    }

    private void onAddSensorSuccess(GxtEnvironment environment, List<GxtSensor> sensors) {

        // update the sensors with the new environment
        for (GxtSensor sensor : sensors) {
            sensor.set(GxtSensor.ENVIRONMENT, environment);
        }

        onCreateComplete();
    }

    private void onCreateComplete() {
        Dispatcher.forwardEvent(EnvCreateEvents.CreateSuccess);
    }

    private void onCreateEnvironmentFailure(int code, Throwable error) {
        onCreateFailure(code, error);
    }

    private void onCreateEnvironmentSuccess(String response, List<GxtSensor> sensors) {

        // parse the response
        GxtEnvironment environment = null;
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            CreateEnvironmentResponse jso = JsonUtils.unsafeEval(response);
            environment = new GxtEnvironment(jso.getEnvironment());
        }

        if (null != environment) {

            // update global environment list
            Registry.<List<GxtEnvironment>> get(Constants.REG_ENVIRONMENT_LIST).add(
                    environment);

            // continue with adding sensors
            addSensors(environment, sensors);

        } else {
            onCreateEnvironmentFailure(-1, new Throwable(
                    "Failed to parse response creating environment"));
        }
    }

    private void onCreateFailure(int code, Throwable error) {
        // TODO show error dialog

    }

    private void onCreateSensorFailure(int code, Throwable error) {
        onCreateEnvironmentFailure(code, error);
    }

    private void onCreateSensorSuccess(String response, List<GxtDevice> devices, int index,
            String name, int floors, Polygon outline, List<GxtSensor> sensors) {

        // parse the new sensor details from the response
        Sensor positionSensor = null;
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            CreateSensorResponse jso = JsonUtils.unsafeEval(response);
            positionSensor = jso.getSensor();
        }

        if (null != positionSensor) {

            GxtSensor gxtSensor = new GxtSensor(positionSensor);

            // add the new sensor to the list of sensors for this environment
            sensors.add(gxtSensor);

            // add the new sensor to the global library
            Registry.<List<GxtSensor>> get(
                    nl.sense_os.commonsense.shared.client.util.Constants.REG_SENSOR_LIST).add(
                    gxtSensor);

            // add the new position sensor to the proper device
            addSensorToDevice(gxtSensor, devices, index, name, floors, outline, sensors);

        } else {
            onCreateSensorFailure(-1, new Throwable("Incorrect response creating sensor"));
        }
    }

    private void onSensorToDeviceFailure(int code, Throwable error) {
        onCreateFailure(code, error);
    }

    private void onSensorToDeviceSuccess(String response, GxtSensor sensor,
            List<GxtDevice> devices, int index, String name, int floors, Polygon outline,
            List<GxtSensor> sensors) {

        // update the sensor model
        sensor.setDevice(devices.get(index));
        sensor.setType(1);

        setPosition(sensor, devices, index, name, floors, outline, sensors);
    }

    private void onSetPositionFailure(int code, Throwable error) {
        onCreateFailure(code, error);
    }

    private void onSetPositionSuccess(String response, List<GxtDevice> devices, int index,
            String name, int floors, Polygon outline, List<GxtSensor> sensors) {
        index++;
        updatePosition(devices, index, name, floors, outline, sensors);
    }

    private void setPosition(GxtSensor positionSensor, final List<GxtDevice> devices,
            final int index, final String name, final int floors, final Polygon outline,
            final List<GxtSensor> sensors) {

        GxtDevice device = devices.get(index);
        LatLng latLng = device.<LatLng> get("latlng");
        String value = "{\\\"latitude\\\":" + latLng.getLatitude() + ",\\\"longitude\\\":"
                + latLng.getLongitude() + ",\\\"provider\\\":\\\"environment\\\"}";


        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                onSetPositionFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                int statusCode = response.getStatusCode();
                if (Response.SC_CREATED == statusCode) {
                    onSetPositionSuccess(response.getText(), devices, index, name, floors, outline,
                            sensors);
                } else {
                    onSetPositionFailure(statusCode, new Throwable(response.getStatusText()));
                }
            }
        };

        // send request
        CommonSenseClient.getClient().createSensorData(callback, positionSensor.getId(), value,
                System.currentTimeMillis());
    }

}
