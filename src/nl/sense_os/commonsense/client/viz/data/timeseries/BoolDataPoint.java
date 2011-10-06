package nl.sense_os.commonsense.client.viz.data.timeseries;

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
