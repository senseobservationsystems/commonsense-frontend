package nl.sense_os.commonsense.client.mvc.events;

import com.extjs.gxt.ui.client.event.EventType;

public class GroupSensorsEvents {
    public static final EventType GroupSensorsRequested = new EventType();
    public static final EventType GroupSensorsNotUpdated = new EventType();
    public static final EventType GroupSensorsUpdated = new EventType();
    public static final EventType ShowGroupSensors = new EventType();
    public static final EventType GroupSensorsBusy = new EventType();
}
