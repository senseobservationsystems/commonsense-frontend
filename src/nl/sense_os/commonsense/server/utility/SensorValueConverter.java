package nl.sense_os.commonsense.server.utility;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import java.sql.Timestamp;
import java.util.logging.Logger;

import nl.sense_os.commonsense.dto.MyriaHumValueModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.MyriaTempValueModel;
import nl.sense_os.commonsense.server.data.SensorValue;

public class SensorValueConverter {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("SensorValueConverter");

    @SuppressWarnings("deprecation")
    public static SensorValueModel entityToModel(SensorValue sensorValue) throws JSONException {
        SensorValueModel sensorValueModel = null;

        // find out the sensor type
        final Timestamp ts = sensorValue.getTimestamp();
        final int sensorType = sensorValue.getType();
        final String value = sensorValue.getValue();
        switch (sensorType) {
        case SensorValue.AUDIOSTREAM:
            sensorValueModel = new SensorValueModel(ts, sensorType, value);
            break;
        case SensorValue.BLUETOOTH_ADDR:
            // log.warning("SensorValueConverter... Found myria sensor value");

        case SensorValue.MYRIA_TEMPERATURE:
            JSONObject myriaTemp = new JSONObject(value);
            int myriaTempNodeId = myriaTemp.getInt("node_id");
            String myriaTempSensorName = myriaTemp.getString("sensor");
            int myriaTempVal = myriaTemp.getInt("value");
            int myriaTempVar = myriaTemp.getInt("variance");
            sensorValueModel = new MyriaTempValueModel(ts, myriaTempNodeId, myriaTempSensorName,
                    myriaTempVal, myriaTempVar);
            break;
        case SensorValue.MYRIA_HUMIDITY:
            JSONObject myriaHum = new JSONObject(value);
            int myriaHumNodeId = myriaHum.getInt("node_id");
            String myriaHumSensorName = myriaHum.getString("sensor");
            int myriaHumVal = myriaHum.getInt("value");
            int myriaHumVar = myriaHum.getInt("variance");
            sensorValueModel = new MyriaHumValueModel(ts, myriaHumNodeId, myriaHumSensorName,
                    myriaHumVal, myriaHumVar);
            break;
        case SensorValue.NOISE:
            // log.warning("SensorValueConverter... Found regular sensor value");

            // regular sensor value
            sensorValueModel = new SensorValueModel(sensorValue.getTimestamp(),
                    sensorValue.getType(), sensorValue.getValue());

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
