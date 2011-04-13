package nl.sense_os.commonsense.client.visualization.components;

import java.util.Map;

import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;

public interface VizPanel {

    /**
     * Adds sensor data to the visualization.
     * 
     * @param sensor
     *            The sensor that the data belongs to.
     * @param values
     *            The sensor values to visualize.
     */
    public abstract void addData(SensorModel sensor, SensorValueModel[] values);

    /**
     * Convenience method for adding data from more than one sensor at a time.
     * 
     * @param data
     *            Map with sensors and sensor values to display.
     * @see #addData(SensorModel, SensorValueModel[])
     */
    public abstract void addData(Map<SensorModel, SensorValueModel[]> data);
}
