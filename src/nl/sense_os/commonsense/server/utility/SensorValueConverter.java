package nl.sense_os.commonsense.server.utility;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import java.sql.Timestamp;
import java.util.logging.Logger;

import nl.sense_os.commonsense.dto.BooleanValueModel;
import nl.sense_os.commonsense.dto.FloatValueModel;
import nl.sense_os.commonsense.dto.JsonValueModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.StringValueModel;
import nl.sense_os.commonsense.server.data.BooleanValue;
import nl.sense_os.commonsense.server.data.FloatValue;
import nl.sense_os.commonsense.server.data.JsonValue;
import nl.sense_os.commonsense.server.data.SensorValue;
import nl.sense_os.commonsense.server.data.StringValue;

public class SensorValueConverter {

    private static final Logger log = Logger.getLogger("SensorValueConverter");

    public static SensorValueModel entityToModel(SensorValue sensorValue) throws JSONException {
        SensorValueModel sensorValueModel = null;

        // find out the sensor type
        final Timestamp ts = sensorValue.getTimestamp();
        final String name = sensorValue.getName();
        final int dataType = sensorValue.getType();
        switch (dataType) {
        case SensorValue.BOOL:
            BooleanValue bv = (BooleanValue) sensorValue;
            sensorValueModel = new BooleanValueModel(ts, name, bv.getValue());
            break;
        case SensorValue.FLOAT:
            FloatValue fv = (FloatValue) sensorValue;
            sensorValueModel = new FloatValueModel(ts, name, fv.getValue());
            break;
        case SensorValue.JSON:
            JsonValue jv = (JsonValue) sensorValue;
            sensorValueModel = new JsonValueModel(ts, name, jv.getFields());
            break;
        case SensorValue.STRING:
            StringValue sv = (StringValue) sensorValue;
            sensorValueModel = new StringValueModel(ts, name, sv.getValue());
            break;
        }
        return sensorValueModel;
    }

    public static SensorValue jsonToEntity(JSONObject jsonSensorValue, String name, String dataType)
            throws JSONException {
        SensorValue s = null;

        Timestamp ts = TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("t"));
        String value = jsonSensorValue.getString("v");

        if (dataType.equals("string")) {
            s = new StringValue(ts, name, value);
        } else if (dataType.equals("json")) {
            s = new JsonValue(ts, name, value);
        } else if (dataType.equals("float")) {
            s = new FloatValue(ts, name, Double.parseDouble(value));
        } else if (dataType.equals("bool")) {
            s = new BooleanValue(ts, name, Boolean.parseBoolean(value));
        } else {
            log.warning("Error converting sensor value: Unknown data type.");
        }

        return s;
    }
}