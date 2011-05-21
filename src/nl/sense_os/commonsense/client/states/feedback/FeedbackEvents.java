package nl.sense_os.commonsense.client.states.feedback;

import com.extjs.gxt.ui.client.event.EventType;

public class FeedbackEvents {

    public static final EventType FeedbackInit = new EventType();
    public static final EventType ShowFeedback = new EventType();

    protected static final EventType FeedbackSubmit = new EventType();
    protected static final EventType FeedbackComplete = new EventType();
    protected static final EventType FeedbackCancelled = new EventType();
    protected static final EventType FeedbackFailed = new EventType();
    protected static final EventType FeedbackAjaxSuccess = new EventType();
    protected static final EventType FeedbackAjaxFailure = new EventType();

    protected static final EventType LabelsRequest = new EventType();
    protected static final EventType LabelsSuccess = new EventType();
    protected static final EventType LabelsFailure = new EventType();
    protected static final EventType LabelsAjaxSuccess = new EventType();
    protected static final EventType LabelsAjaxFailure = new EventType();
}