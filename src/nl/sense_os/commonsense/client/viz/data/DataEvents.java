package nl.sense_os.commonsense.client.viz.data;

import com.extjs.gxt.ui.client.event.EventType;

public class DataEvents {
    protected static final EventType DataRequest = new EventType();
    public static final EventType LatestValuesRequest = new EventType();

    protected static final EventType HideProgress = new EventType();
    protected static final EventType ShowProgress = new EventType();
    protected static final EventType UpdateDataProgress = new EventType();
    protected static final EventType UpdateMainProgress = new EventType();
}
