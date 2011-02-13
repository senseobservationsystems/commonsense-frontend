package nl.sense_os.commonsense.client.groups;

import com.extjs.gxt.ui.client.event.EventType;

public class GroupEvents {
    public static final EventType ShowGrid = new EventType();
    public static final EventType ListUpdated = new EventType();

    protected static final EventType ListRequested = new EventType();
    protected static final EventType Working = new EventType();

    protected static final EventType ShowCreator = new EventType();
    protected static final EventType CreateRequested = new EventType();
    protected static final EventType CreateComplete = new EventType();
    protected static final EventType CreateFailed = new EventType();
    protected static final EventType CreateCancelled = new EventType();

    protected static final EventType LeaveRequested = new EventType();
    protected static final EventType LeaveComplete = new EventType();
    protected static final EventType LeaveFailed = new EventType();

    protected static final EventType ShowInviter = new EventType();
    protected static final EventType InviteRequested = new EventType();
    protected static final EventType InviteComplete = new EventType();
    protected static final EventType InviteFailed = new EventType();
    protected static final EventType InviteCancelled = new EventType();

    // Ajax-related events
    public static final EventType AjaxCreateSuccess = new EventType();
    public static final EventType AjaxCreateFailure = new EventType();
    public static final EventType AjaxInviteSuccess = new EventType();
    public static final EventType AjaxInviteFailure = new EventType();
    public static final EventType AjaxLeaveSuccess = new EventType();
    public static final EventType AjaxLeaveFailure = new EventType();
}
