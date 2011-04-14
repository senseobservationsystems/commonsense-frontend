package nl.sense_os.commonsense.shared.sensorvalues;

import java.util.Date;

import nl.sense_os.commonsense.client.json.overlays.JsoBoolDataPoint;

/**
 * Model for sensor values of boolean data type.
 * 
 * @deprecated To benefit from the speedier JavaScriptObject overlays, use {@link JsoBoolDataPoint}
 *             instead.
 */
@Deprecated
public class BooleanValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public BooleanValueModel() {

    }

    public BooleanValueModel(Date timestamp, boolean value) {
        super(timestamp, SensorValueModel.BOOL);
        setValue(value);
    }

    public boolean getValue() {
        return get("value", false);
    }

    public BooleanValueModel setValue(boolean value) {
        set("value", value);
        return this;
    }
}
