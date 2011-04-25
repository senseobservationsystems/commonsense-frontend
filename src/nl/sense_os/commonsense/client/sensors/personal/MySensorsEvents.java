package nl.sense_os.commonsense.client.sensors.personal;

import com.extjs.gxt.ui.client.event.EventType;

public class MySensorsEvents {

    // layout-related event types
    public static final EventType ShowTree = new EventType();

    // Controller state
    protected static final EventType Working = new EventType();
    protected static final EventType Done = new EventType();

    // Get list of sensors
    public static final EventType ListRequested = new EventType();
    public static final EventType ListUpdated = new EventType();
    public static final EventType AjaxSensorsFailure = new EventType();
    public static final EventType AjaxSensorsSuccess = new EventType();
    public static final EventType AjaxDevicesFailure = new EventType();
    public static final EventType AjaxDevicesSuccess = new EventType();

    // Share sensors
    protected static final EventType ShowShareDialog = new EventType();
    protected static final EventType ShareRequest = new EventType();
    public static final EventType AjaxShareFailure = new EventType();
    public static final EventType AjaxShareSuccess = new EventType();
    protected static final EventType ShareComplete = new EventType();
    protected static final EventType ShareFailed = new EventType();
    protected static final EventType ShareCancelled = new EventType();

    // Delete events
    public static final EventType ShowDeleteDialog = new EventType();
    protected static final EventType DeleteRequest = new EventType();
    protected static final EventType AjaxDeleteFailure = new EventType();
    protected static final EventType AjaxDeleteSuccess = new EventType();
    public static final EventType DeleteSuccess = new EventType();
    public static final EventType DeleteFailure = new EventType();
}
