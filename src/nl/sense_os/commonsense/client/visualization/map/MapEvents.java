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
     * Dispatched to request location data for a list of sensors
     * 
     * @param sensors
     *            TreeModel[] with sensors
     */
    public static final EventType LoadMap = new EventType();

    public static final EventType CreateMap = new EventType();

    /**
     * Dispatched when the map view is ready
     * 
     * @param data
     *            the MapPanel to display
     */
    public static final EventType MapReady = new EventType();

}
