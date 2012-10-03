package nl.sense_os.commonsense.common.client.model;

public class StringDataPoint extends DataPoint {

    protected StringDataPoint() {
        // empty protected constructor
    }

    public final String getValue() {
        return getRawValue();
    }
}
