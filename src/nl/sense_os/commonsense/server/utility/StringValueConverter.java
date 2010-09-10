package nl.sense_os.commonsense.server.utility;

import nl.sense_os.commonsense.dto.StringValueModel;
import nl.sense_os.commonsense.server.data.StringValue;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class StringValueConverter extends SensorValueConverter {

    public static StringValueModel entityToModel(StringValue sv) throws JSONException {
        return new StringValueModel(
        		sv.getTimestamp(),
        		sv.getValue());
    }

    public static StringValue jsonToEntity(int deviceId, int sensorType, JSONObject jsonSensorValue) throws JSONException {
    	return new StringValue(
    			deviceId,
    			sensorType,
    			TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("t")),
    			jsonSensorValue.getString("v").replace("\\\"", "\""));
    }

    public static StringValue jsonToEntity(JSONObject jsonSensorValue) throws JSONException {
    	return new StringValue(
    			jsonSensorValue.getInt("device_id"),
    			jsonSensorValue.getInt("sensor_type"),
    			TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("date")),
    			jsonSensorValue.getString("sensor_value").replace("\\\"", "\""));
    }

    public static StringValueModel[] jsonsToModels(JSONArray jsonStringValues, int parentId, int taggedId) throws JSONException {
    	StringValueModel[] stringValues = new StringValueModel[jsonStringValues.length()];
        for (int i = 0; i < jsonStringValues.length(); i++) {
            JSONObject jsonStringValue = (JSONObject) jsonStringValues.get(i);
            stringValues[i] = entityToModel(jsonToEntity(parentId, taggedId, jsonStringValue));
        }
        return stringValues;
    }
}
