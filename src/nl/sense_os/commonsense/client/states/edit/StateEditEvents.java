package nl.sense_os.commonsense.client.states.edit;

import com.extjs.gxt.ui.client.event.EventType;

public class StateEditEvents {
    public static final EventType ShowEditor = new EventType();

    protected static final EventType MethodsRequested = new EventType();
    protected static final EventType MethodsUpdated = new EventType();
    protected static final EventType MethodsNotUpdated = new EventType();
    protected static final EventType GetMethodsAjaxSuccess = new EventType();
    protected static final EventType GetMethodsAjaxFailure = new EventType();

    protected static final EventType InvokeMethodRequested = new EventType();
    protected static final EventType InvokeMethodComplete = new EventType();
    protected static final EventType InvokeMethodFailed = new EventType();
    protected static final EventType InvokeMethodAjaxSuccess = new EventType();
    protected static final EventType InvokeMethodAjaxFailure = new EventType();
}
