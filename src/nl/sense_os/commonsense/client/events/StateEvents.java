package nl.sense_os.commonsense.client.events;

import com.extjs.gxt.ui.client.event.EventType;

public class StateEvents {
    public static final EventType ShowGrid = new EventType();
    public static final EventType ListRequested = new EventType();
    public static final EventType Done = new EventType();
    public static final EventType Working = new EventType();

    public static final EventType ShowCreator = new EventType();
    public static final EventType CreateServiceRequested = new EventType();
    public static final EventType CreateServiceComplete = new EventType();
    public static final EventType CreateServiceCancelled = new EventType();
    public static final EventType CreateServiceFailed = new EventType();

    public static final EventType AvailableServicesRequested = new EventType();
    public static final EventType AvailableServicesUpdated = new EventType();
    public static final EventType AvailableServicesNotUpdated = new EventType();

    public static final EventType RemoveRequested = new EventType();
    public static final EventType RemoveComplete = new EventType();
    public static final EventType RemoveFailed = new EventType();

    public static final EventType ShowSensorConnecter = new EventType();
    public static final EventType ConnectRequested = new EventType();
    public static final EventType ConnectComplete = new EventType();
    public static final EventType ConnectFailed = new EventType();
    public static final EventType AvailableSensorsRequested = new EventType();
    public static final EventType AvailableSensorsUpdated = new EventType();
    public static final EventType AvailableSensorsNotUpdated = new EventType();

    public static final EventType ShowEditor = new EventType();
    public static final EventType InvokeMethodRequested = new EventType();
    public static final EventType InvokeMethodComplete = new EventType();
    public static final EventType InvokeMethodFailed = new EventType();
    public static final EventType MethodsRequested = new EventType();
    public static final EventType MethodsUpdated = new EventType();
    public static final EventType MethodsNotUpdated = new EventType();

    public static final EventType ShowFeedback = new EventType();
    public static final EventType FeedbackReady = new EventType();
    public static final EventType FeedbackSubmit = new EventType();
    public static final EventType FeedbackComplete = new EventType();
}
