package nl.sense_os.commonsense.client.sensors.library;

import com.extjs.gxt.ui.client.event.EventType;

public class LibraryEvents {

    public static final EventType ShowLibrary = new EventType();

    // controller state
    protected static final EventType Done = new EventType();
    protected static final EventType Working = new EventType();

    public static final EventType LoadRequest = new EventType();
    public static final EventType ListUpdated = new EventType();
    protected static final EventType FullDetailsAjaxFailure = new EventType();
    protected static final EventType FullDetailsAjaxSuccess = new EventType();
    protected static final EventType GroupsAjaxSuccess = new EventType();
    protected static final EventType GroupsAjaxFailure = new EventType();
    protected static final EventType GroupSensorsAjaxSuccess = new EventType();
    protected static final EventType GroupSensorsAjaxFailure = new EventType();
    protected static final EventType UsersAjaxSuccess = new EventType();
    protected static final EventType UsersAjaxFailure = new EventType();
    protected static final EventType AvailServicesAjaxSuccess = new EventType();
    protected static final EventType AvailServicesAjaxFailure = new EventType();
}
