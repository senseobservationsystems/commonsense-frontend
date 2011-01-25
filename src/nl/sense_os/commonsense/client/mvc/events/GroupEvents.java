package nl.sense_os.commonsense.client.mvc.events;

import com.extjs.gxt.ui.client.event.EventType;

public class GroupEvents {
    public static final EventType GroupListRequested = new EventType();
    public static final EventType GroupListNotUpdated = new EventType();
    public static final EventType GroupListUpdated = new EventType();
    public static final EventType ShowGroups = new EventType();
    public static final EventType GroupsBusy = new EventType();
    
    public static final EventType ShowGroupCreator = new EventType();
    public static final EventType CreateGroupRequested = new EventType();
    public static final EventType CreateGroupCancelled = new EventType();
    public static final EventType CreateGroupComplete = new EventType();
    public static final EventType CreateGroupFailed = new EventType();
    
    public static final EventType LeaveGroupRequested = new EventType();
    public static final EventType LeaveGroupComplete = new EventType();
    public static final EventType LeaveGroupFailed = new EventType();
    
    public static final EventType ShowInvitation = new EventType();
    public static final EventType InviteUserFailed = new EventType();
    public static final EventType InviteUserCancelled = new EventType();
    public static final EventType InviteUserRequested = new EventType();
    public static final EventType InviteUserComplete = new EventType();
}
