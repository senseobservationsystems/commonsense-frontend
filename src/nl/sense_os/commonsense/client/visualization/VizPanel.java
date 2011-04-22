package nl.sense_os.commonsense.client.visualization;

import nl.sense_os.commonsense.client.json.overlays.Timeseries;

import com.google.gwt.core.client.JsArray;

public interface VizPanel {

    /**
     * Adds data to the visualization.
     * 
     * @param data
     *            Timeseries to display.
     */
    public void addData(JsArray<Timeseries> data);
}
