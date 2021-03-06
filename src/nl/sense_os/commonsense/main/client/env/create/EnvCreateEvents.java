package nl.sense_os.commonsense.main.client.env.create;

import com.extjs.gxt.ui.client.event.EventType;

public class EnvCreateEvents {

    public static final EventType ShowCreator = new EventType();
    protected static final EventType CreateRequest = new EventType();
    public static final EventType CreateSuccess = new EventType();
    protected static final EventType CreateFailure = new EventType();

    protected static final EventType Forward = new EventType();
    protected static final EventType Back = new EventType();
    protected static final EventType Cancel = new EventType();
    public static final EventType OutlineComplete = new EventType();
}
