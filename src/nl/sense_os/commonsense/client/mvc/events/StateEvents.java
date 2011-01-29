package nl.sense_os.commonsense.client.mvc.events;

import com.extjs.gxt.ui.client.event.EventType;

public class StateEvents {
    public static final EventType ShowGrid = new EventType();
    public static final EventType ListRequested = new EventType();
    public static final EventType ListUpdated = new EventType();
    public static final EventType ListNotUpdated = new EventType();
    public static final EventType Working = new EventType();

    public static final EventType ShowCreator = new EventType();
    public static final EventType CreateRequested = new EventType();
    public static final EventType CreateComplete = new EventType();
    public static final EventType CreateCancelled = new EventType();
    public static final EventType CreateFailed = new EventType();

    public static final EventType ListAvailableRequested = new EventType();
    public static final EventType ListAvailableUpdated = new EventType();
    public static final EventType ListAvailableNotUpdated = new EventType();

    public static final EventType RemoveRequested = new EventType();
    public static final EventType RemoveComplete = new EventType();
    public static final EventType RemoveFailed = new EventType();
}
