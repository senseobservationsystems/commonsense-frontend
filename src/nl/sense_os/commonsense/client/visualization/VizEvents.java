package nl.sense_os.commonsense.client.visualization;

import com.extjs.gxt.ui.client.event.EventType;

public class VizEvents {
    public static final EventType DataRequested = new EventType();
    public static final EventType DataNotReceived = new EventType();
    public static final EventType DataReceived = new EventType();

    public static final EventType ShowTypeChoice = new EventType();
    public static final EventType TypeChoiceCancelled = new EventType();

    public static final EventType ShowLineChart = new EventType();

    public static final EventType ShowTable = new EventType();

    public static final EventType ShowMap = new EventType();
    public static final EventType MapReady = new EventType();

    public static final EventType ShowNetwork = new EventType();

    public static final EventType Show = new EventType();

    // Ajax-related event types
    public static final EventType AjaxDataSuccess = new EventType();
    public static final EventType AjaxDataFailure = new EventType();
}