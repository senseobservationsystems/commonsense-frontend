package nl.sense_os.commonsense.client.states.feedback;

import com.extjs.gxt.ui.client.event.EventType;

public class FeedbackEvents {

    public static final EventType ShowFeedback = new EventType();

    protected static final EventType FeedbackSubmit = new EventType();
    protected static final EventType FeedbackComplete = new EventType();
    protected static final EventType FeedbackCancelled = new EventType();
    protected static final EventType FeedbackFailed = new EventType();
    protected static final EventType AjaxFeedbackSuccess = new EventType();
    protected static final EventType AjaxFeedbackFailure = new EventType();

}
