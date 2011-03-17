package nl.sense_os.commonsense.client.environments;

import com.extjs.gxt.ui.client.event.EventType;

public class EnvEvents {
    public static final EventType ShowGrid = new EventType();

    protected static final EventType ListRequested = new EventType();
    protected static final EventType ListUpdated = new EventType();
    protected static final EventType ListNotUpdated = new EventType();
    protected static final EventType Working = new EventType();
    protected static final EventType Done = new EventType();
    protected static final EventType ShowCreator = new EventType();
}
