package nl.sense_os.commonsense.client.sensors.library;

import com.extjs.gxt.ui.client.event.EventType;

public class SensorLibraryEvents {

    public static final EventType ShowLibrary = new EventType();

    // controller state
    protected static final EventType Done = new EventType();
    protected static final EventType Working = new EventType();

    public static final EventType ListRequested = new EventType();
    public static final EventType ListUpdated = new EventType();
    protected static final EventType ListAjaxFailure = new EventType();
    protected static final EventType ListAjaxSuccess = new EventType();
    protected static final EventType ListPhysicalAjaxFailure = new EventType();
    protected static final EventType ListPhysicalAjaxSuccess = new EventType();

    protected static final EventType AjaxGroupsSuccess = new EventType();
    protected static final EventType AjaxGroupsFailure = new EventType();
    protected static final EventType AjaxUnownedSuccess = new EventType();
    protected static final EventType AjaxUnownedFailure = new EventType();
    protected static final EventType AjaxOwnedSuccess = new EventType();
    protected static final EventType AjaxOwnedFailure = new EventType();
    protected static final EventType AjaxDirectSharesSuccess = new EventType();
    protected static final EventType AjaxDirectSharesFailure = new EventType();
}
