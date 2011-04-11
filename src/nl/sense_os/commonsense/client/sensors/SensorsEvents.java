package nl.sense_os.commonsense.client.sensors;

import com.extjs.gxt.ui.client.event.EventType;

public class SensorsEvents {
    public static final EventType ShowDeleteDialog = new EventType();
    protected static final EventType DeleteRequest = new EventType();
    protected static final EventType AjaxDeleteFailure = new EventType();
    protected static final EventType AjaxDeleteSuccess = new EventType();
    public static final EventType DeleteSuccess = new EventType();
    public static final EventType DeleteFailure = new EventType();

    public static final EventType ShowUnshareDialog = new EventType();
    protected static final EventType UnshareRequest = new EventType();
    protected static final EventType AjaxUnshareFailure = new EventType();
    protected static final EventType AjaxUnshareSuccess = new EventType();
    public static final EventType UnshareFailure = new EventType();
    public static final EventType UnshareSuccess = new EventType();
}
