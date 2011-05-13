package nl.sense_os.commonsense.client.states.list;

import com.extjs.gxt.ui.client.event.EventType;

public class StateListEvents {
    public static final EventType ShowGrid = new EventType();

    protected static final EventType Done = new EventType();
    protected static final EventType Working = new EventType();

    // get list of states
    protected static final EventType LoadRequest = new EventType();
    protected static final EventType LoadComplete = new EventType();
    protected static final EventType AjaxStateSensorsSuccess = new EventType();
    protected static final EventType AjaxStateSensorsFailure = new EventType();
    protected static final EventType ConnectedAjaxSuccess = new EventType();
    protected static final EventType ConnectedAjaxFailure = new EventType();
    protected static final EventType GetMethodsAjaxSuccess = new EventType();
    protected static final EventType GetMethodsAjaxFailure = new EventType();

    // disconnect sensor from service
    protected static final EventType RemoveRequested = new EventType();
    public static final EventType RemoveComplete = new EventType();
    protected static final EventType RemoveFailed = new EventType();
    protected static final EventType AjaxDisconnectSuccess = new EventType();
    protected static final EventType AjaxDisconnectFailure = new EventType();

}
