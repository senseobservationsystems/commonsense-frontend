package nl.sense_os.commonsense.client.mvc.events;

import com.extjs.gxt.ui.client.event.EventType;

public class VizEvents {
    public static final EventType VizReady = new EventType();
    public static final EventType TagsRequested = new EventType();
    public static final EventType TagsNotUpdated = new EventType();
    public static final EventType TagsUpdated = new EventType();
    public static final EventType GroupsRequested = new EventType();
    public static final EventType GroupsNotUpdated = new EventType();
    public static final EventType GroupsUpdated = new EventType();
}
