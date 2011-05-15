package nl.sense_os.commonsense.server.utility;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;
import nl.sense_os.commonsense.shared.models.SensorModel;
import nl.sense_os.commonsense.shared.models.TagModel;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class SensorConverter {

    private static final Logger log = Logger.getLogger("SensorConverter");

    public static SensorModel parseSensor(JSONObject json) throws JSONException {

        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(SensorModel.ID, json.getString(SensorModel.ID));
        properties.put(SensorModel.PAGER_TYPE, json.getString(SensorModel.PAGER_TYPE));
        properties.put(SensorModel.PHYSICAL_SENSOR, json.getString(SensorModel.PHYSICAL_SENSOR));
        properties.put(SensorModel.NAME, json.getString(SensorModel.NAME));
        properties.put(SensorModel.TYPE, json.getString(SensorModel.TYPE));
        properties.put(SensorModel.DATA_TYPE, json.optString(SensorModel.DATA_TYPE));
        properties.put(SensorModel.DATA_TYPE_ID, json.optString(SensorModel.DATA_TYPE_ID));
        properties.put(SensorModel.DATA_STRUCTURE, json.optString(SensorModel.DATA_STRUCTURE));
        JSONObject owner = json.optJSONObject(SensorModel.OWNER);
        if (null != owner) {
            properties.put(SensorModel.OWNER, UserConverter.parseUser(owner));
        }
        JSONObject device = json.optJSONObject(SensorModel.DEVICE);
        if (null != owner) {
            properties.put(SensorModel.DEVICE, DeviceConverter.parseDevice(device));
        }

        // front end-only properties
        properties.put("tagType", TagModel.TYPE_SENSOR);
        String name = (String) properties.get(SensorModel.NAME);
        String deviceType = (String) properties.get(SensorModel.PHYSICAL_SENSOR);
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
