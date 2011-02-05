package nl.sense_os.commonsense.client.controllers;

import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.FeedbackForm;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

public class FeedbackController extends Controller {

    private static final String TAG = "FeedbackController";
    private FeedbackForm form;

    public FeedbackController() {
        registerEventTypes(StateEvents.ShowFeedback, StateEvents.FeedbackSubmit,
                StateEvents.FeedbackComplete);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.FeedbackSubmit)) {
            submitFeedback(event);
        } else {
            forwardToView(this.form, event);
        }
    }

    private void submitFeedback(AppEvent event) {
        Log.w(TAG, "Feedback submit is not implemented");
        Dispatcher.forwardEvent(StateEvents.FeedbackComplete);

    }

    @Override
    protected void initialize() {
        super.initialize();

        this.form = new FeedbackForm(this);
    }
}
