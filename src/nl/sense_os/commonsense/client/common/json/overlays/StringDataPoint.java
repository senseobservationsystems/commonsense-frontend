package nl.sense_os.commonsense.client.common.json.overlays;

public class StringDataPoint extends DataPoint {

    protected StringDataPoint() {
        // empty protected constructor
    }

    public final String getValue() {
        return getRawValue();
    }
}