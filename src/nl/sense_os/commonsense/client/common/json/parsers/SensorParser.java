package nl.sense_os.commonsense.client.common.json.parsers;

import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.DeviceModel;
import nl.sense_os.commonsense.shared.EnvironmentModel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class SensorParser {

    private static final String TAG = "SensorParser";

    public static SensorModel parseSensor(JSONObject json) {

        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put(SensorModel.DISPLAY_NAME, json.get(SensorModel.DISPLAY_NAME).isString()
                .stringValue());
        props.put(SensorModel.ID, json.get(SensorModel.ID).isString().stringValue());
        props.put(SensorModel.NAME, json.get(SensorModel.NAME).isString().stringValue());
        props.put(SensorModel.PHYSICAL_SENSOR, json.get(SensorModel.PHYSICAL_SENSOR).isString()
                .stringValue());
        props.put(SensorModel.TYPE, json.get(SensorModel.TYPE).isString().stringValue());
        props.put(SensorModel.PAGER_TYPE, json.get(SensorModel.PAGER_TYPE).isString().stringValue());

        // make sure there is a display name
        String displayName = (String) props.get(SensorModel.DISPLAY_NAME);
        if (displayName.length() == 0) {
            String name = (String) props.get(SensorModel.NAME);
            // String type = (String) props.get(SensorModel.TYPE);
            // String deviceType = (String) props.get(SensorModel.PHYSICAL_SENSOR);
            // if (!type.equals("1") || "".equals(deviceType) || deviceType.equals(name)) {
            props.put(SensorModel.DISPLAY_NAME, name);
            // } else {
            // props.put(SensorModel.DISPLAY_NAME, name + " (" + deviceType + ")");
            // }
        }

        // optional properties
        if (null != json.get(SensorModel.DATA_TYPE_ID)) {
            props.put(SensorModel.DATA_TYPE_ID, json.get(SensorModel.DATA_TYPE_ID).isString()
                    .stringValue());
        }
        if (null != json.get(SensorModel.DATA_TYPE)) {
            props.put(SensorModel.DATA_TYPE, json.get(SensorModel.DATA_TYPE).isString()
                    .stringValue());
        }
        if (null != json.get(SensorModel.DATA_STRUCTURE)) {
            props.put(SensorModel.DATA_STRUCTURE, json.get(SensorModel.DATA_STRUCTURE).isString()
                    .stringValue());
        }

        // special owner object
        JSONValue rawOwner = json.get(SensorModel.OWNER);
        if (null != rawOwner && null == rawOwner.isNull()) {
            JSONObject ownerJson = rawOwner.isObject();
            UserModel owner = UserParser.parseUser(ownerJson);
            props.put(SensorModel.OWNER, owner);
        } else {
            UserModel owner = Registry.get(Constants.REG_USER);
            props.put(SensorModel.OWNER, owner);
        }

        // special device object
        JSONValue rawDevice = json.get(SensorModel.DEVICE);
        if (null != rawDevice && null == rawDevice.isNull()) {
            JSONObject deviceJson = rawDevice.isObject();
            DeviceModel device = DeviceParser.parse(deviceJson);
            props.put(SensorModel.DEVICE, device);
        }

        // special environment object
        JSONValue rawEnvironment = json.get(SensorModel.ENVIRONMENT);
        if (null != rawEnvironment && null == rawEnvironment.isNull()) {
            JSONObject environmentJson = rawEnvironment.isObject();
            EnvironmentModel environment = EnvironmentParser.parse(environmentJson);
            props.put(SensorModel.ENVIRONMENT, environment);
        }

        // front end-only properties
        props.put("tagType", TagModel.TYPE_SENSOR);
        props.put("text", props.get(SensorModel.DISPLAY_NAME));

        return new SensorModel(props);
    }

    public static int parseSensors(String jsonString, List<SensorModel> list) {

        int total = 0;
        try {
            JSONObject json = JSONParser.parseStrict(jsonString).isObject();

            // get array of raw sensors
            JSONArray sensors = json.get("sensors").isArray();

            // get total count of sensors
            JSONValue optTotal = json.get("total");
            if (null != optTotal) {
                total = (int) optTotal.isNumber().doubleValue();
            } else {
                total = sensors.size();
            }

            JSONObject sensor;
            for (int i = 0; i < sensors.size(); i++) {
                sensor = sensors.get(i).isObject();
                list.add(parseSensor(sensor));
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception parsing sensors list: " + e.getMessage());
            Log.e(TAG, "Raw response: " + jsonString);
            total = -1;
        }

        return total;
    }
}
