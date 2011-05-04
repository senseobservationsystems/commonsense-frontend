package nl.sense_os.commonsense.client.sensors.group;

import com.extjs.gxt.ui.client.event.EventType;

@Deprecated
public class GroupSensorsEvents {
    public static final EventType ShowTree = new EventType();
    protected static final EventType Done = new EventType();
    protected static final EventType Working = new EventType();

    // get list events
    protected static final EventType ListRequest = new EventType();
    protected static final EventType ListUpdated = new EventType();
    protected static final EventType AjaxGroupsSuccess = new EventType();
    protected static final EventType AjaxGroupsFailure = new EventType();
    protected static final EventType AjaxUnownedSuccess = new EventType();
    protected static final EventType AjaxUnownedFailure = new EventType();
    protected static final EventType AjaxOwnedSuccess = new EventType();
    protected static final EventType AjaxOwnedFailure = new EventType();
    protected static final EventType AjaxDirectSharesSuccess = new EventType();
    protected static final EventType AjaxDirectSharesFailure = new EventType();

    // remove shared sensor events
    protected static final EventType ShowUnshareDialog = new EventType();
    protected static final EventType UnshareRequest = new EventType();
    protected static final EventType AjaxUnshareFailure = new EventType();
    protected static final EventType AjaxUnshareSuccess = new EventType();
    protected static final EventType UnshareFailure = new EventType();
    protected static final EventType UnshareSuccess = new EventType();
}
