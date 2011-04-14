package nl.sense_os.commonsense.server.ivo.utility;

import nl.sense_os.commonsense.server.ivo.data.JsonValue;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

@Deprecated
public class JsonValueConverter extends SensorValueConverter {

    public static JsonValueModel entityToModel(JsonValue jv) throws JSONException {
        return new JsonValueModel(jv.getTimestamp(), jv.getFieldMap());
    }

    public static JsonValue jsonToEntity(int deviceId, int sensorType, JSONObject jsonSensorValue)
            throws JSONException {
        return new JsonValue(deviceId, sensorType,
                TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("t")),
                jsonSensorValue.getString("v").replace("\\\"", "\""));
    }

    public static JsonValue jsonToEntity(JSONObject jsonSensorValue) throws JSONException {
        return new JsonValue(jsonSensorValue.getInt("device_id"),
                jsonSensorValue.getInt("sensor_type"),
                TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("date")),
                jsonSensorValue.getString("sensor_value").replace("\\\"", "\""));
    }

    public static JsonValueModel[] jsonsToModels(JSONArray jsonJsonValues, int parentId,
            int taggedId) throws JSONException {
        JsonValueModel[] jsonValues = new JsonValueModel[jsonJsonValues.length()];
        for (int i = 0; i < jsonJsonValues.length(); i++) {
            JSONObject jsonJsonValue = (JSONObject) jsonJsonValues.get(i);
            jsonValues[i] = entityToModel(jsonToEntity(parentId, taggedId, jsonJsonValue));
        }
        return jsonValues;
    }

}
