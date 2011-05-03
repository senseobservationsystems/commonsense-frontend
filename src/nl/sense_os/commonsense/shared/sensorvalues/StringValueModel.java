package nl.sense_os.commonsense.shared.sensorvalues;

import java.util.Date;

import nl.sense_os.commonsense.client.common.json.overlays.DataPoint;

/**
 * Model for sensor values of string data type.
 * 
 * @deprecated To benefit from the speedier JavaScriptObject overlays, use {@link DataPoint}
 *             instead.
 */
@Deprecated
public class StringValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public StringValueModel() {
        // empty constructor for serializing
    }

    public StringValueModel(Date timestamp, String value) {
        super(timestamp, SensorValueModel.STRING);
        setValue(value);
    }

    public String getValue() {
        return get("value");
    }

    public StringValueModel setValue(String value) {
        set("value", value);
        return this;
    }
}
