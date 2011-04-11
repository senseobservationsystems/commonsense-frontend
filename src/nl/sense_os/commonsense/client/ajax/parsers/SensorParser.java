package nl.sense_os.commonsense.client.ajax.parsers;

import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.TagModel;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class SensorParser {

    private static final String TAG = "SensorParser";

    public static SensorModel parseSensor(JSONObject json) {

        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put(SensorModel.ID, json.get(SensorModel.ID).isString().stringValue());
        props.put(SensorModel.PAGER_TYPE, json.get(SensorModel.PAGER_TYPE).isString().stringValue());
        props.put(SensorModel.DEVICE_TYPE, json.get(SensorModel.DEVICE_TYPE).isString()
                .stringValue());
        props.put(SensorModel.NAME, json.get(SensorModel.NAME).isString().stringValue());
        props.put(SensorModel.TYPE, json.get(SensorModel.TYPE).isString().stringValue());

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
        if (null != json.get(SensorModel.OWNER)) {
            JSONObject owner = json.get(SensorModel.OWNER).isObject();
            props.put(SensorModel.OWNER, UserParser.parseUser(owner));
        }
        if (null != json.get(SensorModel.DEVICE_DEVTYPE)) {
            props.put(SensorModel.DEVICE_DEVTYPE, json.get(SensorModel.DEVICE_DEVTYPE).isString()
                    .stringValue());
        }
        if (null != json.get(SensorModel.DEVICE_ID)) {
            props.put(SensorModel.DEVICE_ID, json.get(SensorModel.DEVICE_ID).isString()
                    .stringValue());
        }

        // front end-only properties
        props.put("tagType", TagModel.TYPE_SENSOR);
        String name = (String) props.get(SensorModel.NAME);
        String deviceType = (String) props.get(SensorModel.DEVICE_TYPE);
        if (name.equals(deviceType) || "".equals(deviceType)) {
            props.put("text", name);
        } else {
            props.put("text", name + " (" + deviceType + ")");
        }

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
