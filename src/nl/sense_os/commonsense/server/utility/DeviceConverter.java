package nl.sense_os.commonsense.server.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.DeviceModel;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class DeviceConverter {

    private static final Logger log = Logger.getLogger("DeviceConverter");

    public static DeviceModel parseDevice(JSONObject device) throws JSONException {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(DeviceModel.ID, device.getString(DeviceModel.ID));
        properties.put(DeviceModel.TYPE, device.getString(DeviceModel.TYPE));
        properties.put(DeviceModel.UUID, device.getString(DeviceModel.UUID));

        // front end-only properties
        properties.put("tagType", TagModel.TYPE_DEVICE);
        if (properties.get(DeviceModel.TYPE).equals("myrianode")) {
            String text = properties.get(DeviceModel.TYPE) + " "
                    + properties.get(DeviceModel.UUID);
            properties.put("text", text);
        } else {
            properties.put("text", properties.get(DeviceModel.TYPE));
        }

        return new DeviceModel(properties);
    }

    public static List<DeviceModel> parseDevices(String response) throws WrongResponseException {

        List<DeviceModel> result = new ArrayList<DeviceModel>();
        try {
            JSONObject responseObj = new JSONObject(response);

            JSONArray devices = responseObj.getJSONArray("devices");
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);
                DeviceModel model = DeviceConverter.parseDevice(device);
                result.add(model);
            }

        } catch (JSONException e) {
            log.severe("JSONException parsing devices list: " + e.getMessage());
            log.severe("Raw response: " + response);
            throw (new WrongResponseException(e.getMessage()));
        }

        // return list of tags
        return result;
    }

}
