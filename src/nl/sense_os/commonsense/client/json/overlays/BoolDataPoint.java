package nl.sense_os.commonsense.client.json.overlays;

import java.util.Date;

public class BoolDataPoint extends DataPoint implements AbstractBoolDataPoint {

    public BoolDataPoint(String rawValue, Date timestamp) {
        super(rawValue, timestamp);
    }

    @Override
    public boolean getBoolValue() {
        return Boolean.parseBoolean(this.getRawValue());
    }

}
