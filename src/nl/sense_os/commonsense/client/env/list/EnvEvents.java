package nl.sense_os.commonsense.client.env.list;

import com.extjs.gxt.ui.client.event.EventType;

public class EnvEvents {
    public static final EventType ShowGrid = new EventType();

    protected static final EventType ListRequested = new EventType();
    public static final EventType ListUpdated = new EventType();
    protected static final EventType Working = new EventType();
    protected static final EventType Done = new EventType();

    public static final EventType DeleteRequest = new EventType();
    public static final EventType DeleteSuccess = new EventType();
    protected static final EventType DeleteFailure = new EventType();
}
