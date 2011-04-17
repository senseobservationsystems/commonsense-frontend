package nl.sense_os.commonsense.client.json.overlays;

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
