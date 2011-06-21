package nl.sense_os.commonsense.client.sensors.unshare;

import com.extjs.gxt.ui.client.event.EventType;

public class UnshareEvents {
    public static final EventType ShowUnshareDialog = new EventType();
    protected static final EventType UnshareRequest = new EventType();
    public static final EventType UnshareComplete = new EventType();
    protected static final EventType UnshareFailed = new EventType();
}
