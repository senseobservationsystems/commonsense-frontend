package nl.sense_os.commonsense.main.client.groups.create;

import com.extjs.gxt.ui.client.event.EventType;

public class GroupCreateEvents {

    public static final EventType ShowCreator = new EventType();
    protected static final EventType CreateRequested = new EventType();
    public static final EventType CreateComplete = new EventType();
    protected static final EventType CreateFailed = new EventType();
}
