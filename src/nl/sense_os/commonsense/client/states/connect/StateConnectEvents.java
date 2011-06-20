package nl.sense_os.commonsense.client.states.connect;

import com.extjs.gxt.ui.client.event.EventType;

public class StateConnectEvents {
    public static final EventType ShowSensorConnecter = new EventType();

    // get name of service
    protected static final EventType ServiceNameRequest = new EventType();
    protected static final EventType ServiceNameSuccess = new EventType();
    protected static final EventType ServiceNameFailure = new EventType();

    // get available sensors to connect
    protected static final EventType AvailableSensorsRequested = new EventType();
    protected static final EventType AvailableSensorsUpdated = new EventType();
    protected static final EventType AvailableSensorsNotUpdated = new EventType();

    // connect the sensor
    protected static final EventType ConnectRequested = new EventType();
    public static final EventType ConnectSuccess = new EventType();
    protected static final EventType ConnectFailure = new EventType();
}
