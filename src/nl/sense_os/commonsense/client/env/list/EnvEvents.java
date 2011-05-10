package nl.sense_os.commonsense.client.env.list;

import com.extjs.gxt.ui.client.event.EventType;

public class EnvEvents {
    public static final EventType ShowGrid = new EventType();

    protected static final EventType ListRequested = new EventType();
    public static final EventType ListUpdated = new EventType();
    protected static final EventType Working = new EventType();
    protected static final EventType Done = new EventType();

    protected static final EventType ListAjaxSuccess = new EventType();
    protected static final EventType ListAjaxFailure = new EventType();

    protected static final EventType DeleteRequest = new EventType();
    protected static final EventType DeleteAjaxSuccess = new EventType();
    protected static final EventType DeleteAjaxFailure = new EventType();
    protected static final EventType DeleteSuccess = new EventType();
    protected static final EventType DeleteFailure = new EventType();
}
