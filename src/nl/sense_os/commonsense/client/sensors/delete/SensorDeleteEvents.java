package nl.sense_os.commonsense.client.sensors.delete;

import com.extjs.gxt.ui.client.event.EventType;

public class SensorDeleteEvents {
    public static final EventType ShowDeleteDialog = new EventType();
    protected static final EventType DeleteRequest = new EventType();
    protected static final EventType DeleteAjaxFailure = new EventType();
    protected static final EventType DeleteAjaxSuccess = new EventType();
    public static final EventType DeleteSuccess = new EventType();
    public static final EventType DeleteFailure = new EventType();
}
