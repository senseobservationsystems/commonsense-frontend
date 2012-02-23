package nl.sense_os.commonsense.client.viz.tabs;

import com.extjs.gxt.ui.client.event.EventType;

public class VizEvents {
    public static final EventType ShowTypeChoice = new EventType();
    public static final EventType TypeChoiceCancelled = new EventType();

    public static final EventType Show = new EventType();

    private VizEvents() {
        // private constructor
    }
}
