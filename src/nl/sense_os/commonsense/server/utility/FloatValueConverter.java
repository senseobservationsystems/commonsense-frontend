package nl.sense_os.commonsense.server.utility;

import nl.sense_os.commonsense.dto.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.server.data.FloatValue;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class FloatValueConverter extends SensorValueConverter {

    public static FloatValueModel entityToModel(FloatValue fv) throws JSONException {
        return new FloatValueModel(
        		fv.getTimestamp(),
        		fv.getValue());
    }

    public static FloatValue jsonToEntity(int deviceId, int sensorType, JSONObject jsonSensorValue) throws JSONException {
    	return new FloatValue(
    			deviceId,
    			sensorType,
    			TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("t")),
    			Double.parseDouble(jsonSensorValue.getString("v")));
    }

    public static FloatValue jsonToEntity(JSONObject jsonSensorValue) throws JSONException {
    	return new FloatValue(
    			jsonSensorValue.getInt("device_id"),
    			jsonSensorValue.getInt("sensor_type"),
    			TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("date")),
    			Double.parseDouble(jsonSensorValue.getString("sensor_value")));
    }

    public static FloatValueModel[] jsonsToModels(JSONArray jsonFloatValues, int parentId, int taggedId) throws JSONException {
    	FloatValueModel[] floatValues = new FloatValueModel[jsonFloatValues.length()];
        for (int i = 0; i < jsonFloatValues.length(); i++) {
            JSONObject jsonFloatValue = (JSONObject) jsonFloatValues.get(i);
            floatValues[i] = entityToModel(jsonToEntity(parentId, taggedId, jsonFloatValue));
        }
        return floatValues;
    }

}
