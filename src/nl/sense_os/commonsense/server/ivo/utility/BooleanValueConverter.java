package nl.sense_os.commonsense.server.ivo.utility;

import nl.sense_os.commonsense.server.ivo.data.BooleanValue;
import nl.sense_os.commonsense.shared.sensorvalues.BooleanValueModel;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class BooleanValueConverter extends SensorValueConverter {

    public static BooleanValueModel entityToModel(BooleanValue bv) throws JSONException {
        return new BooleanValueModel(
        		bv.getTimestamp(),
        		bv.getValue());
    }

    public static BooleanValue jsonToEntity(int deviceId, int sensorType, JSONObject jsonSensorValue) throws JSONException {
    	return new BooleanValue(
    			deviceId,
    			sensorType,
    			TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("t")),
    			Boolean.parseBoolean(jsonSensorValue.getString("v")));
    }

    public static BooleanValue jsonToEntity(JSONObject jsonSensorValue) throws JSONException {
    	return new BooleanValue(
    			jsonSensorValue.getInt("device_id"),
    			jsonSensorValue.getInt("sensor_type"),
    			TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("date")),
    			Boolean.parseBoolean(jsonSensorValue.getString("sensor_value")));
    }

    public static BooleanValueModel[] jsonsToModels(JSONArray jsonBooleanValues, int parentId, int taggedId) throws JSONException {
    	BooleanValueModel[] booleanValues = new BooleanValueModel[jsonBooleanValues.length()];
        for (int i = 0; i < jsonBooleanValues.length(); i++) {
            JSONObject jsonBooleanValue = (JSONObject) jsonBooleanValues.get(i);
            booleanValues[i] = entityToModel(jsonToEntity(parentId, taggedId, jsonBooleanValue));
        }
        return booleanValues;
    }

}
