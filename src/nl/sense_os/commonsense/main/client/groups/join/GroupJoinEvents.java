package nl.sense_os.commonsense.main.client.groups.join;

import com.extjs.gxt.ui.client.event.EventType;

public class GroupJoinEvents {

    public static final EventType Show = new EventType();
    protected static final EventType AllGroupsRequest = new EventType();
    protected static final EventType AllGroupsSuccess = new EventType();
    protected static final EventType AllGroupsFailure = new EventType();
    protected static final EventType JoinRequest = new EventType();
    protected static final EventType JoinFailure = new EventType();
    public static final EventType JoinSuccess = new EventType();
    protected static final EventType GroupDetailsRequest = new EventType();
    protected static final EventType GroupDetailsSuccess = new EventType();
    protected static final EventType GroupDetailsFailure = new EventType();
}
