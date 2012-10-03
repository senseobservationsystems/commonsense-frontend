package nl.sense_os.commonsense.common.client.model;

/**
 * JavaScript object overlay for data point of boolean type.
 */
public class BoolDataPoint extends DataPoint {

    protected BoolDataPoint() {
        // empty protected constructor
    }

    public final native boolean getValue() /*-{
		return this.value;
    }-*/;
}
