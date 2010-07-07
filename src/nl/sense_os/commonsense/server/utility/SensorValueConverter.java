package nl.sense_os.commonsense.server.utility;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import java.sql.Timestamp;

import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.SnifferValueModel;
import nl.sense_os.commonsense.server.data.SensorValue;

public class SensorValueConverter {

    public static SensorValueModel entityToModel(SensorValue sensorValue) {
        SensorValueModel sensorValueModel = null;
        
        // ugly hack to send SnifferValues when required
        if (sensorValue.getValue().contains("node_id")) {
            
            // parse snifferValues from JSON
            Timestamp ts = sensorValue.getTimestamp();
            String nodeId = "", sensorName = "", value = "", variance = "";
            try {
                JSONObject obj = new JSONObject(sensorValue.getValue());
                nodeId = obj.getString("node_id");
                sensorName = obj.getString("sensor");
                value = obj.getString("value");
                variance = obj.getString("variance");
            } catch (JSONException e) {
                
            }
            
            sensorValueModel = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        } else {
            // regular sensor value
            sensorValueModel = new SensorValueModel(sensorValue.getTimestamp(),
                    sensorValue.getValue());
        }
        return sensorValueModel;
    }

    public static SensorValue jsonToEntity(JSONObject jsonSensorValue) {
        SensorValue s = new SensorValue();
        try {
            String ts = (String) jsonSensorValue.get("t");
            s.setTimestamp(TimestampConverter.microEpochToTimestamp(ts));
            s.setValue((String) jsonSensorValue.get("v"));
        } catch (JSONException e) {
        }
        return s;
    }

}
