package nl.sense_os.commonsense.client.states.list;

import com.extjs.gxt.ui.client.event.EventType;

public class StateEvents {
    public static final EventType ShowGrid = new EventType();

    protected static final EventType Done = new EventType();
    protected static final EventType Working = new EventType();

    // get list of states
    protected static final EventType ListRequested = new EventType();
    protected static final EventType AjaxSensorsSuccess = new EventType();
    protected static final EventType AjaxSensorsFailure = new EventType();
    protected static final EventType AjaxConnectedSuccess = new EventType();
    protected static final EventType AjaxConnectedFailure = new EventType();
    protected static final EventType GetMethodsAjaxSuccess = new EventType();
    protected static final EventType GetMethodsAjaxFailure = new EventType();

    // disconnect sensor from service
    protected static final EventType RemoveRequested = new EventType();
    public static final EventType RemoveComplete = new EventType();
    protected static final EventType RemoveFailed = new EventType();
    protected static final EventType AjaxDisconnectSuccess = new EventType();
    protected static final EventType AjaxDisconnectFailure = new EventType();

    // check/create default states
    protected static final EventType CheckDefaults = new EventType();
    public static final EventType CheckDefaultsSuccess = new EventType();
    protected static final EventType CheckDefaultsFailure = new EventType();
    protected static final EventType AjaxDefaultsSuccess = new EventType();
    protected static final EventType AjaxDefaultsFailure = new EventType();
}
