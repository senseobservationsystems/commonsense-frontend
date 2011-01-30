package nl.sense_os.commonsense.client.events;

import com.extjs.gxt.ui.client.event.EventType;

public class GroupEvents {
    public static final EventType ShowGrid = new EventType();
    public static final EventType ListRequested = new EventType();
    public static final EventType ListUpdated = new EventType();
    public static final EventType ListNotUpdated = new EventType();
    
    public static final EventType ShowCreator = new EventType();
    public static final EventType CreateRequested = new EventType();
    public static final EventType CreateComplete = new EventType();
    public static final EventType CreateFailed = new EventType();
    public static final EventType CreateCancelled = new EventType();
    
    public static final EventType LeaveRequested = new EventType();
    public static final EventType LeaveComplete = new EventType();
    public static final EventType LeaveFailed = new EventType();
    
    public static final EventType ShowInviter = new EventType();
    public static final EventType InviteRequested = new EventType();
    public static final EventType InviteComplete = new EventType();
    public static final EventType InviteFailed = new EventType();
    public static final EventType InviteCancelled = new EventType();
    
    public static final EventType Working = new EventType();
}
