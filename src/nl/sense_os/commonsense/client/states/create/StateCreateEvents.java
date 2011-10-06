package nl.sense_os.commonsense.client.states.create;

import com.extjs.gxt.ui.client.event.EventType;

public class StateCreateEvents {

    public static final EventType ShowCreator = new EventType();

    // create the new service
    protected static final EventType CreateServiceRequested = new EventType();
    public static final EventType CreateServiceComplete = new EventType();
    protected static final EventType CreateServiceCancelled = new EventType();
    protected static final EventType CreateServiceFailed = new EventType();

    // load all sensors to create service from
    protected static final EventType LoadSensors = new EventType();
    protected static final EventType LoadSensorsSuccess = new EventType();
    protected static final EventType LoadSensorsFailure = new EventType();

    // get available services for a certain sensor
    protected static final EventType AvailableServicesRequested = new EventType();
    protected static final EventType AvailableServicesUpdated = new EventType();
    protected static final EventType AvailableServicesNotUpdated = new EventType();
}
