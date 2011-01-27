package nl.sense_os.commonsense.client.mvc.events;

import com.extjs.gxt.ui.client.event.EventType;

public class MySensorsEvents {
    public static final EventType ShowTree = new EventType();
    public static final EventType ListRequested = new EventType();
    public static final EventType ListUpdated = new EventType();
    public static final EventType ListNotUpdated = new EventType();
    
    public static final EventType ShowShareDialog = new EventType();
    public static final EventType ShareRequested = new EventType();
    public static final EventType ShareComplete = new EventType();
    public static final EventType ShareFailed = new EventType();
    public static final EventType ShareCancelled = new EventType();
    
    public static final EventType Working = new EventType();
}
