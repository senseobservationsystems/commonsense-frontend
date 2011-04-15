package nl.sense_os.commonsense.shared.sensorvalues;

import java.util.Date;
import java.util.Map;

import nl.sense_os.commonsense.client.json.overlays.JsonDataPoint;

/**
 * Model for sensor values of JSON data type.
 * 
 * @deprecated To benefit from the speedier JavaScriptObject overlays, use {@link JsonDataPoint}
 *             instead.
 */
@Deprecated
public class JsonValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public JsonValueModel() {
        // empty constructor for serializing
    }

    public JsonValueModel(Date timestamp, Map<String, Object> fields) {
        super(timestamp, SensorValueModel.JSON);

        setFields(fields);
    }

    public Map<String, Object> getFields() {
        return get("fields");
    }

    public JsonValueModel setFields(Map<String, Object> fields) {
        set("fields", fields);
        return this;
    }
}
