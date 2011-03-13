package nl.sense_os.commonsense.client.states;

import com.extjs.gxt.ui.client.event.EventType;

public class StateEvents {
    public static final EventType ShowGrid = new EventType();

    protected static final EventType ListRequested = new EventType();
    protected static final EventType Done = new EventType();
    protected static final EventType Working = new EventType();

    protected static final EventType ShowCreator = new EventType();
    protected static final EventType CreateServiceRequested = new EventType();
    protected static final EventType CreateServiceComplete = new EventType();
    protected static final EventType CreateServiceCancelled = new EventType();
    protected static final EventType CreateServiceFailed = new EventType();
    protected static final EventType LoadSensors = new EventType();
    protected static final EventType LoadSensorsSuccess = new EventType();
    protected static final EventType LoadSensorsFailure = new EventType();
    protected static final EventType AvailableServicesRequested = new EventType();
    protected static final EventType AvailableServicesUpdated = new EventType();
    protected static final EventType AvailableServicesNotUpdated = new EventType();
    protected static final EventType AjaxAvailableServiceSuccess = new EventType();
    protected static final EventType AjaxAvailableServiceFailure = new EventType();

    protected static final EventType RemoveRequested = new EventType();
    protected static final EventType RemoveComplete = new EventType();
    protected static final EventType RemoveFailed = new EventType();

    protected static final EventType ShowSensorConnecter = new EventType();
    protected static final EventType ConnectRequested = new EventType();
    protected static final EventType ConnectSuccess = new EventType();
    protected static final EventType ConnectFailure = new EventType();
    protected static final EventType AvailableSensorsRequested = new EventType();
    protected static final EventType AvailableSensorsUpdated = new EventType();
    protected static final EventType AvailableSensorsNotUpdated = new EventType();

    protected static final EventType ShowEditor = new EventType();
    protected static final EventType InvokeMethodRequested = new EventType();
    protected static final EventType InvokeMethodComplete = new EventType();
    protected static final EventType InvokeMethodFailed = new EventType();
    protected static final EventType MethodsRequested = new EventType();
    protected static final EventType MethodsUpdated = new EventType();
    protected static final EventType MethodsNotUpdated = new EventType();

    public static final EventType ShowFeedback = new EventType();
    public static final EventType FeedbackReady = new EventType();
    public static final EventType FeedbackSubmit = new EventType();
    public static final EventType FeedbackComplete = new EventType();
    public static final EventType FeedbackCancelled = new EventType();
    public static final EventType FeedbackFailed = new EventType();

    // Ajax-related event types
    protected static final EventType AjaxMethodSuccess = new EventType();
    protected static final EventType AjaxMethodFailure = new EventType();
    protected static final EventType AjaxConnectSuccess = new EventType();
    protected static final EventType AjaxConnectFailure = new EventType();
    protected static final EventType AjaxDisconnectSuccess = new EventType();
    protected static final EventType AjaxDisconnectFailure = new EventType();
    protected static final EventType AjaxCreateSuccess = new EventType();
    protected static final EventType AjaxCreateFailure = new EventType();
    protected static final EventType AjaxGetMethodsSuccess = new EventType();
    protected static final EventType AjaxGetMethodsFailure = new EventType();
    protected static final EventType AjaxFeedbackSuccess = new EventType();
    protected static final EventType AjaxFeedbackFailure = new EventType();

    protected static final EventType ServiceNameRequest = new EventType();
    protected static final EventType ServiceNameSuccess = new EventType();
    protected static final EventType ServiceNameFailure = new EventType();
    protected static final EventType AjaxServiceNameSuccess = new EventType();
    protected static final EventType AjaxServiceNameFailure = new EventType();
}
