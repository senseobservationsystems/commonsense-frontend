package nl.sense_os.commonsense.shared.sensorvalues;

import java.util.Date;

import nl.sense_os.commonsense.client.common.json.overlays.FloatDataPoint;

/**
 * Model for sensor values of float data type.
 * 
 * @deprecated To benefit from the speedier JavaScriptObject overlays, use {@link FloatDataPoint}
 *             instead.
 */
@Deprecated
public class FloatValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public FloatValueModel() {

    }

    public FloatValueModel(Date timestamp, double value) {
        super(timestamp, SensorValueModel.FLOAT);
        setValue(value);
    }

    public double getValue() {
        return get("value", Double.MIN_VALUE);
    }

    public FloatValueModel setValue(double value) {
        set("value", value);
        return this;
    }
}
