package nl.sense_os.commonsense.client.states.edit;

import com.extjs.gxt.ui.client.event.EventType;

public class StateEditEvents {
    public static final EventType ShowEditor = new EventType();

    protected static final EventType InvokeMethodRequested = new EventType();
    protected static final EventType InvokeMethodComplete = new EventType();
    protected static final EventType InvokeMethodFailed = new EventType();
}
