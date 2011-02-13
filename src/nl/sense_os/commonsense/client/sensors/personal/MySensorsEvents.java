package nl.sense_os.commonsense.client.sensors.personal;

import com.extjs.gxt.ui.client.event.EventType;

public class MySensorsEvents {

    // layout-related event types
    public static final EventType ShowTree = new EventType();

    // MySensors view and controller-only event types
    protected static final EventType ShowShareDialog = new EventType();
    protected static final EventType ShareRequested = new EventType();
    protected static final EventType ListRequested = new EventType();
    protected static final EventType ShareComplete = new EventType();
    protected static final EventType ShareFailed = new EventType();
    protected static final EventType ShareCancelled = new EventType();
    protected static final EventType Working = new EventType();
    protected static final EventType Done = new EventType();

    // Ajax-related event types
    public static final EventType AjaxShareFailure = new EventType();
    public static final EventType AjaxShareSuccess = new EventType();
}
