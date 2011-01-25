package nl.sense_os.commonsense.client.mvc.events;

import com.extjs.gxt.ui.client.event.EventType;

public class MySensorsEvents {
    public static final EventType MySensorsRequested = new EventType();
    public static final EventType MySensorsNotUpdated = new EventType();
    public static final EventType MySensorsUpdated = new EventType();
    public static final EventType ShowMySensors = new EventType();
    public static final EventType MySensorsBusy = new EventType();
}
