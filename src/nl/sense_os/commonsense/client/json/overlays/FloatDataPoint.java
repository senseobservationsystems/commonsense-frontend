package nl.sense_os.commonsense.client.json.overlays;

import java.util.Date;

public class FloatDataPoint extends DataPoint implements AbstractFloatDataPoint {

    public FloatDataPoint(String rawValue, Date timestamp) {
        super(rawValue, timestamp);
    }

    public FloatDataPoint(double value, Date timestamp) {
        super("" + value, timestamp);
    }

    @Override
    public double getFloatValue() {
        return Double.parseDouble(getRawValue());
    }

}
