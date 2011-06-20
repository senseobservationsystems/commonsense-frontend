package nl.sense_os.commonsense.client.groups.invite;

import com.extjs.gxt.ui.client.event.EventType;

public class InviteEvents {
    public static final EventType ShowInviter = new EventType();
    protected static final EventType InviteRequested = new EventType();
    public static final EventType InviteComplete = new EventType();
    protected static final EventType InviteFailed = new EventType();
}
