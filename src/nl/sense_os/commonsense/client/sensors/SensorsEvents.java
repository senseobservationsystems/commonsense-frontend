package nl.sense_os.commonsense.client.sensors;

import com.extjs.gxt.ui.client.event.EventType;

public class SensorsEvents {
    public static final EventType ShowRemoveDialog = new EventType();
    public static final EventType DeleteSuccess = new EventType();
    public static final EventType DeleteFailure = new EventType();

    protected static final EventType DeleteRequest = new EventType();
    protected static final EventType AjaxDeleteFailure = new EventType();
    protected static final EventType AjaxDeleteSuccess = new EventType();
}
