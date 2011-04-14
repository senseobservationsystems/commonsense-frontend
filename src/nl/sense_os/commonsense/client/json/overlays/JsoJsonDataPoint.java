package nl.sense_os.commonsense.client.json.overlays;

import java.util.HashMap;
import java.util.Map;

import nl.sense_os.commonsense.client.utility.Log;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * JavaScript object overlay for data point of JSON type.
 */
public class JsoJsonDataPoint extends JsoDataPoint {

    private static final String TAG = "JsoJsonDataPoint";

    // private Map<String, Object> fields;

    protected JsoJsonDataPoint() {
        // empty protected constructor
    }

    public final Map<String, JsoDataPoint> getFields() {

        // if (fields == null) {

        final String jsoStart = "{\"date\":\"" + getDate() + "\",\"id\":\"" + getId()
                + "\",\"sensor_id\":\"" + getSensorId() + "\",\"week\":\"" + getWeek()
                + "\",\"month\":\"" + getMonth() + "\",\"year\":\"" + getYear() + "\",\"value\":";
        final String jsoEnd = "}";

        Map<String, JsoDataPoint> fields = new HashMap<String, JsoDataPoint>();

        final JSONValue json = JSONParser.parseStrict(this.getCleanValue());
        if (null != json) {
            final JSONObject object = json.isObject();

            if (null != object) {

                for (String fieldKey : object.keySet()) {
                    final JSONValue fieldValue = object.get(fieldKey);

                    final JSONNumber numberField = fieldValue.isNumber();
                    if (null != numberField) {
                        String value = numberField.toString();
                        fields.put(
                                fieldKey,
                                JsonUtils.<JsoFloatDataPoint> unsafeEval(jsoStart + "\"" + value
                                        + "\"" + jsoEnd));
                        continue;
                    }

                    final JSONString stringField = fieldValue.isString();
                    if (null != stringField) {
                        String value = stringField.toString();
                        Log.d(TAG, jsoStart + value + jsoEnd);
                        fields.put(fieldKey,
                                JsonUtils.<JsoDataPoint> unsafeEval(jsoStart + value + jsoEnd));
                        continue;
                    }

                    Log.e(TAG, "Field value is not a valid sensor value: " + fieldValue.toString());
                }
            } else {
                Log.e(TAG, "Sensor value is not a valid JSONObject: " + json.toString());
            }
        } else {
            Log.e(TAG, "Sensor value is not valid JSON: " + this.getCleanValue());
        }
        // }

        return fields;
    }
}
