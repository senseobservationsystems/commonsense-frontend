package nl.sense_os.commonsense.client.common.json.overlays;

/**
 * JavaScript object overlay for data point of float type.
 */
public class FloatDataPoint extends DataPoint {

    protected FloatDataPoint() {
        // empty protected constructor
    }

    public final native double getValue() /*-{
		return this.value;
    }-*/;
}
