package nl.sense_os.commonsense.server.utility;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class SensorConverter {

    private static final Logger log = Logger.getLogger("SensorConverter");

    public static SensorModel parseSensor(JSONObject json) throws JSONException {

        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(SensorModel.KEY_ID, json.getString(SensorModel.KEY_ID));
        properties.put(SensorModel.KEY_DATA_TYPE_ID, json.optString(SensorModel.KEY_DATA_TYPE_ID));
        properties.put(SensorModel.KEY_PAGER_TYPE, json.getString(SensorModel.KEY_PAGER_TYPE));
        properties.put(SensorModel.KEY_DEVICE_TYPE, json.getString(SensorModel.KEY_DEVICE_TYPE));
        properties.put(SensorModel.KEY_NAME, json.getString(SensorModel.KEY_NAME));
        properties.put(SensorModel.KEY_TYPE, json.getString(SensorModel.KEY_TYPE));
        properties.put(SensorModel.KEY_DATA_TYPE, json.optString(SensorModel.KEY_DATA_TYPE));
        properties.put(SensorModel.KEY_DATA_TYPE_ID, json.optString(SensorModel.KEY_DATA_TYPE_ID));
        properties.put(SensorModel.KEY_DATA_STRUCTURE,
                json.optString(SensorModel.KEY_DATA_STRUCTURE));
        properties.put(SensorModel.KEY_OWNER_ID, json.optString(SensorModel.KEY_OWNER_ID));
        properties.put(SensorModel.KEY_DEVICE_DEVTYPE,
                json.optString(SensorModel.KEY_DEVICE_DEVTYPE));
        properties.put(SensorModel.KEY_DEVICE_ID, json.optString(SensorModel.KEY_DEVICE_ID));

        // front end-only properties
        properties.put("tagType", TagModel.TYPE_SENSOR);
        String name = (String) properties.get(SensorModel.KEY_NAME);
        String deviceType = (String) properties.get(SensorModel.KEY_DEVICE_TYPE);
        if (name.equals(deviceType) || "".equals(deviceType)) {
            properties.put("text", name);
        } else {
            properties.put("text", name + " (" + deviceType + ")");
        }

        return new SensorModel(properties);
    }

    public static int parseSensors(String response, List<SensorModel> list)
            throws WrongResponseException {

        // Convert JSON response to list of tags
        try {
            JSONObject responseObj = new JSONObject(response);
            JSONArray sensors = responseObj.getJSONArray("sensors");
            int total = responseObj.optInt("total", sensors.length());
            for (int i = 0; i < sensors.length(); i++) {
                JSONObject sensor = sensors.getJSONObject(i);

                list.add(SensorConverter.parseSensor(sensor));
            }

            // return total count
            return total;

        } catch (JSONException e) {
            log.severe("JSONException parsing sensors list: " + e.getMessage());
            log.severe("Raw response: " + response);
            throw (new WrongResponseException(e.getMessage()));
        }
    }
}
