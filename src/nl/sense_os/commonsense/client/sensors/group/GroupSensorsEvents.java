package nl.sense_os.commonsense.client.sensors.group;

import com.extjs.gxt.ui.client.event.EventType;

public class GroupSensorsEvents {
    public static final EventType ShowTree = new EventType();
    public static final EventType ListUpdated = new EventType();
    public static final EventType ListRequest = new EventType();

    protected static final EventType Done = new EventType();
    protected static final EventType Working = new EventType();

    protected static final EventType AjaxGroupsSuccess = new EventType();
    protected static final EventType AjaxGroupsFailure = new EventType();
    protected static final EventType AjaxUnownedSuccess = new EventType();
    protected static final EventType AjaxUnownedFailure = new EventType();
    protected static final EventType AjaxOwnedSuccess = new EventType();
    protected static final EventType AjaxOwnedFailure = new EventType();
}
