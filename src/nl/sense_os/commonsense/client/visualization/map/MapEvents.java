package nl.sense_os.commonsense.client.visualization.map;

import com.extjs.gxt.ui.client.event.EventType;

public class MapEvents {

    /**
     * Dispatched when the map view should be shown.
     * 
     * @param sensors
     *            TreeModel[] with the list of sensors to display trace for
     * @param startTime
     *            start time of trace (long)
     * @param endTime
     *            end time of trace (long)
     */
    public static final EventType Show = new EventType();

    /**
     * Dispatched when the map view is ready
     * 
     * @param data
     *            the MapPanel to display
     */
    public static final EventType MapReady = new EventType();

    /**
     * Dispatched to request location data for a list of sensors
     * 
     * @param sensor
     *            TreeModel[] with sensor properties
     * @param startDate
     *            start time of trace (double)
     * @param endDate
     *            end time of trace (double)
     * @param panel
     *            the MapPanel that this data should be displayed on
     */
    protected static final EventType LoadData = new EventType();

    /**
     * Dispatched to signal that a list of sensor values of ready
     * 
     * @param data
     *            SensorValueModel[] with data
     * @param sensor
     *            TreeModel with sensor properties
     * @param panel
     *            the MapPanel that this data should be displayed on
     */
    protected static final EventType AddData = new EventType();

    /**
     * Dispatched to signal that all data for the map is loaded
     * 
     * @param panel
     *            MapPanel that is complete
     */
    protected static final EventType LoadSuccess = new EventType();

    /**
     * Dispatched to signal that the data for the map failed to load
     * 
     * @param panel
     *            MapPanel that is affected
     */
    protected static final EventType LoadFailure = new EventType();

    public static final EventType AjaxDataSuccess = new EventType();
    public static final EventType AjaxDataFailure = new EventType();
}
