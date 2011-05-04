package nl.sense_os.commonsense.client.sensors.personal;

import com.extjs.gxt.ui.client.event.EventType;

@Deprecated
public class MySensorsEvents {

    // layout-related event types
    public static final EventType ShowTree = new EventType();

    // Controller state
    protected static final EventType Working = new EventType();
    protected static final EventType Done = new EventType();

    // get tree of sensors
    public static final EventType TreeRequested = new EventType();
    public static final EventType TreeUpdated = new EventType();
    protected static final EventType AjaxSensorsFailure = new EventType();
    protected static final EventType AjaxSensorsSuccess = new EventType();
    protected static final EventType AjaxDevicesFailure = new EventType();
    protected static final EventType AjaxDevicesSuccess = new EventType();
}
