package nl.sense_os.commonsense.server.utility;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import java.sql.Timestamp;
import java.util.logging.Logger;

import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.SnifferValueModel;
import nl.sense_os.commonsense.server.data.SensorValue;

public class SensorValueConverter {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("SensorValueConverter");
    
    public static SensorValueModel entityToModel(SensorValue sensorValue) throws JSONException {
        SensorValueModel sensorValueModel = null;

        // ugly hack to send SnifferValues when required
        if (sensorValue.getValue().contains("node_id")) {
            // log.warning("SensorValueConverter... Found myria sensor value");
            
            // parse snifferValues from JSON
            Timestamp ts = sensorValue.getTimestamp();
            String nodeId = "", sensorName = "", value = "", variance = "";

            JSONObject obj = new JSONObject(sensorValue.getValue());
            nodeId = obj.getString("node_id");
            sensorName = obj.getString("sensor");
            value = obj.getString("value");
            variance = obj.getString("variance");

            sensorValueModel = new SnifferValueModel(ts, value, nodeId, sensorName, variance);
        } else {            
            // log.warning("SensorValueConverter... Found regular sensor value");
            
            // regular sensor value
            sensorValueModel = new SensorValueModel(sensorValue.getTimestamp(),
                    sensorValue.getValue());
        }
        return sensorValueModel;
    }

    public static SensorValue jsonToEntity(JSONObject jsonSensorValue) throws JSONException {
        SensorValue s = new SensorValue();

        String ts = (String) jsonSensorValue.get("t");
        s.setTimestamp(TimestampConverter.microEpochToTimestamp(ts));
        // cast as string if the value is not a JSON object (temporary fix)
        try {
            s.setValue((String) jsonSensorValue.get("v"));
        } catch (ClassCastException e) {
            // log.warning("\"v\" is a JSON Object");
            JSONObject v = (JSONObject) jsonSensorValue.get("v");
            s.setValue(v.toString());
        }

        return s;
    }

}
