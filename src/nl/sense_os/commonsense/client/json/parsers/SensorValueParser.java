package nl.sense_os.commonsense.client.json.parsers;

import java.util.HashMap;

import nl.sense_os.commonsense.client.json.overlays.JsoDataPoint;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.sensorvalues.BooleanValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.StringValueModel;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * Contains methods for parsing JSON sensor values into SensorValueModel instances.
 * 
 * @deprecated use {@link JsoDataPoint} types instead.
 */
@Deprecated
public class SensorValueParser {

    private static final String TAG = "SensorValueParser";

    /**
     * @param dataPoint
     *            The raw sensor data point to parse the value for.
     * @return A BooleanValueModel, or <code>null</code> if parsing failed.
     */
    public static BooleanValueModel parseBoolValue(JsoDataPoint dataPoint) {

        final boolean value = Boolean.parseBoolean(dataPoint.getRawValue());
        if (value || dataPoint.getRawValue() != null) {
            return new BooleanValueModel(dataPoint.getTimestamp(), value);
        } else {
            Log.e(TAG, "Sensor value is not valid boolean value: " + dataPoint.getRawValue());
            return null;
        }
    }

    /**
     * @param dataPoint
     *            The raw sensor data point to parse the value for.
     * @return A FloatValueModel, or <code>null</code> if parsing failed.
     */
    public static FloatValueModel parseFloatValue(JsoDataPoint dataPoint) {

        try {
            final double value = Double.parseDouble(dataPoint.getRawValue());
            return new FloatValueModel(dataPoint.getTimestamp(), value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Sensor value is not valid float value: " + dataPoint.getRawValue());
            return null;
        }
    }

    /**
     * @param dataPoint
     *            The raw sensor data point to parse the value for.
     * @return A JsonValueModel, or <code>null</code> if parsing failed.
     */
    public static JsonValueModel parseJsonValue(JsoDataPoint dataPoint) {

        final JSONValue json = JSONParser.parseStrict(dataPoint.getCleanValue());
        if (null != json) {
            final JSONObject object = json.isObject();

            if (null != object) {
                final HashMap<String, Object> fields = new HashMap<String, Object>();
                for (String fieldKey : object.keySet()) {
                    final JSONValue fieldValue = object.get(fieldKey);

                    final JSONNumber numberField = fieldValue.isNumber();
                    if (null != numberField) {
                        fields.put(fieldKey, numberField.doubleValue());
                        continue;
                    }

                    final JSONString stringField = fieldValue.isString();
                    if (null != stringField) {
                        fields.put(fieldKey, stringField.stringValue());
                        continue;
                    }
                    fields.put(fieldKey, fieldValue.toString());
                }

                return new JsonValueModel(dataPoint.getTimestamp(), fields);
            } else {
                Log.e(TAG, "Sensor value is not a valid JSONObject: " + json.toString());
                return null;
            }
        } else {
            Log.e(TAG, "Sensor value is not valid JSON: " + dataPoint.getCleanValue());
            return null;
        }
    }

    /**
     * @param dataPoint
     *            The raw sensor data point to parse the value for.
     * @return A StringValueModel, or <code>null</code> if parsing failed.
     */
    public static StringValueModel parseStringValue(JsoDataPoint dataPoint) {

        return new StringValueModel(dataPoint.getTimestamp(), dataPoint.getCleanValue());
    }
}
