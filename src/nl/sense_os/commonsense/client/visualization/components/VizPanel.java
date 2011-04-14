package nl.sense_os.commonsense.client.visualization.components;

import java.util.Map;

import nl.sense_os.commonsense.client.json.overlays.JsoDataPoint;
import nl.sense_os.commonsense.shared.SensorModel;

public interface VizPanel {

    /**
     * Adds sensor data to the visualization.
     * 
     * @param sensor
     *            The sensor that the data belongs to.
     * @param values
     *            The sensor values to visualize.
     */
    public abstract void addData(SensorModel sensor, JsoDataPoint[] values);

    /**
     * Convenience method for adding data from more than one sensor at a time.
     * 
     * @param data
     *            Map with sensors and sensor values to display.
     * @see #addData(SensorModel, AbstractDataPoint[])
     */
    public abstract void addData(Map<SensorModel, JsoDataPoint[]> data);
}
