package nl.sense_os.commonsense.client.controllers;

import java.util.List;

import nl.sense_os.commonsense.client.controllers.cors.FeedbackJsniRequests;
import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.FeedbackForm;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

public class FeedbackController extends Controller {

    private static final String TAG = "FeedbackController";
    private FeedbackForm form;

    public FeedbackController() {
        registerEventTypes(StateEvents.ShowFeedback, StateEvents.FeedbackSubmit,
                StateEvents.FeedbackComplete, StateEvents.FeedbackFailed);
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

    @Override
    protected void initialize() {
        super.initialize();

        this.form = new FeedbackForm(this);
    }

    private void markFeedback(TreeModel service, String label, ModelData[] feedback) {
        // Log.d(TAG, "markFeedback(TreeModel, String, ModelData[])");

        ModelData sensor = service.getChild(0);
        String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services/"
                + service.<String> get("id") + "/manualLearn";
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        String data = "{\"start_date\":\"" + feedback[0].get("start") + "\"";
        data += ",\"end_date\":\"" + feedback[0].get("end") + "\"";
        data += ",\"class_label\":\"" + label + "\"}";
        FeedbackJsniRequests.manualLearn(url, sessionId, data, service, label, feedback, this);
    }

    public void onFeedbackMarked(TreeModel service, String label, ModelData[] feedback) {
        // Log.d(TAG, "FeedbackMarked");

        if (feedback.length > 1) {
            // remove 0th feedback item
            ModelData[] temp = new ModelData[feedback.length - 1];
            System.arraycopy(feedback, 1, temp, 0, temp.length);
            feedback = temp;

            markFeedback(service, label, feedback);
        } else {
            Dispatcher.forwardEvent(StateEvents.FeedbackComplete);
        }
    }

    public void onFeedbackFailed(TreeModel service, String label, ModelData[] feedback) {
        Dispatcher.forwardEvent(StateEvents.FeedbackFailed);
    }

    private void submitFeedback(AppEvent event) {

        TreeModel service = event.<TreeModel> getData("service");
        String label = event.<String> getData("label");
        List<ModelData> feedback = event.<List<ModelData>> getData("feedback");

        Log.d(TAG, "Request to submit feedback for service " + service.get("text"));

        ModelData[] fbArray = new ModelData[feedback.size()];
        feedback.toArray(fbArray);
        markFeedback(service, label, fbArray);
    }
}
