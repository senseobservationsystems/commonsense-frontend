package nl.sense_os.commonsense.client.groups.join;

import com.extjs.gxt.ui.client.event.EventType;

public class GroupJoinEvents {

    public static final EventType Show = new EventType();
    protected static final EventType PublicGroupsRequested = new EventType();
    protected static final EventType JoinRequest = new EventType();
    protected static final EventType JoinFailure = new EventType();
    public static final EventType JoinSuccess = new EventType();
}