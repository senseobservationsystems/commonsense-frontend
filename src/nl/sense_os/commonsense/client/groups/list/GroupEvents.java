package nl.sense_os.commonsense.client.groups.list;

import com.extjs.gxt.ui.client.event.EventType;

public class GroupEvents {

    protected static final EventType Working = new EventType();

    public static final EventType ShowGrid = new EventType();
    public static final EventType LoadRequest = new EventType();
    public static final EventType ListUpdated = new EventType();
    protected static final EventType GroupsAjaxSuccess = new EventType();
    protected static final EventType GroupsAjaxFailure = new EventType();
    protected static final EventType GroupMembersAjaxSuccess = new EventType();
    protected static final EventType GroupMembersAjaxFailure = new EventType();

    protected static final EventType LeaveRequested = new EventType();
    protected static final EventType LeaveComplete = new EventType();
    protected static final EventType LeaveFailed = new EventType();
    protected static final EventType AjaxLeaveSuccess = new EventType();
    protected static final EventType AjaxLeaveFailure = new EventType();
}