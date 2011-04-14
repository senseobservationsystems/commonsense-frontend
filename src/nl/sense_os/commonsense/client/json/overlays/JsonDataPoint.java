package nl.sense_os.commonsense.client.json.overlays;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nl.sense_os.commonsense.client.utility.Log;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class JsonDataPoint extends DataPoint implements AbstractJsonDataPoint {

    private static final String TAG = "JsonDataPoint";
    private Map<String, Object> fields;

    public JsonDataPoint(String rawValue, Date timestamp) {
        super(rawValue, timestamp);
    }

    public final Map<String, Object> getFields() {

        if (fields == null) {

            fields = new HashMap<String, Object>();

            final JSONValue json = JSONParser.parseStrict(this.getCleanValue());
            if (null != json) {
                final JSONObject object = json.isObject();

                if (null != object) {

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
                } else {
                    Log.e(TAG, "Sensor value is not a valid JSONObject: " + json.toString());
                }
            } else {
                Log.e(TAG, "Sensor value is not valid JSON: " + this.getCleanValue());
            }
        }

        return fields;
    }

}
