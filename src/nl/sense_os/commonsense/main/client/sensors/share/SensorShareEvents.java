package nl.sense_os.commonsense.main.client.sensors.share;

import com.extjs.gxt.ui.client.event.EventType;

public class SensorShareEvents {
    public static final EventType ShowShareDialog = new EventType();
    protected static final EventType ShareRequest = new EventType();
    public static final EventType ShareComplete = new EventType();
    protected static final EventType ShareFailed = new EventType();
    protected static final EventType ShareCancelled = new EventType();
}
