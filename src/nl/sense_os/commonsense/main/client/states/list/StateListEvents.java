package nl.sense_os.commonsense.main.client.states.list;

import com.extjs.gxt.ui.client.event.EventType;

public class StateListEvents {
    public static final EventType ShowGrid = new EventType();

    protected static final EventType Done = new EventType();
    protected static final EventType Working = new EventType();

    // get list of states
    protected static final EventType LoadRequest = new EventType();
    protected static final EventType LoadComplete = new EventType();

    // disconnect sensor from service
    protected static final EventType RemoveRequested = new EventType();
    public static final EventType RemoveComplete = new EventType();
    protected static final EventType RemoveFailed = new EventType();

}
