package nl.sense_os.commonsense.client.data;

import com.extjs.gxt.ui.client.event.EventType;

public class DataEvents {
    public static final EventType DataRequest = new EventType();
    public static final EventType RefreshRequest = new EventType();

    protected static final EventType HideProgress = new EventType();
    protected static final EventType ShowProgress = new EventType();
    protected static final EventType UpdateDataProgress = new EventType();
    protected static final EventType UpdateMainProgress = new EventType();

    protected static final EventType AjaxDataSuccess = new EventType();
    protected static final EventType AjaxDataFailure = new EventType();

}
