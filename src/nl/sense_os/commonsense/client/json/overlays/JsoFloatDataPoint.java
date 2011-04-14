package nl.sense_os.commonsense.client.json.overlays;

/**
 * JavaScript object overlay for data point of float type.
 */
public class JsoFloatDataPoint extends JsoDataPoint implements AbstractFloatDataPoint {

    protected JsoFloatDataPoint() {
        // empty protected constructor
    }

    public final double getFloatValue() {
        return Double.parseDouble(getRawValue());
    }
}
