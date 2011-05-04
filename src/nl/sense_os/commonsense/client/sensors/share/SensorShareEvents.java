package nl.sense_os.commonsense.client.sensors.share;

import com.extjs.gxt.ui.client.event.EventType;

public class SensorShareEvents {
    public static final EventType ShowShareDialog = new EventType();
    protected static final EventType ShareRequest = new EventType();
    protected static final EventType ShareAjaxFailure = new EventType();
    protected static final EventType ShareAjaxSuccess = new EventType();
    public static final EventType ShareComplete = new EventType();
    public static final EventType ShareFailed = new EventType();
    public static final EventType ShareCancelled = new EventType();
}
