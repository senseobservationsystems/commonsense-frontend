package nl.sense_os.commonsense.client.states;

import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

public class FeedbackController extends Controller {

    @SuppressWarnings("unused")
    private static final String TAG = "FeedbackController";
    private FeedbackForm form;

    public FeedbackController() {
        registerEventTypes(StateEvents.ShowFeedback, StateEvents.FeedbackSubmit,
                StateEvents.FeedbackComplete, StateEvents.FeedbackFailed);
        registerEventTypes(StateEvents.AjaxFeedbackFailure, StateEvents.AjaxFeedbackSuccess);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.FeedbackSubmit)) {

            // Log.d(TAG, "FeedbackSubmit");
            final TreeModel service = event.<TreeModel> getData("service");
            final String label = event.<String> getData("label");
            final List<ModelData> feedback = event.<List<ModelData>> getData("feedback");
            markFeedback(service, label, feedback);

        } else if (type.equals(StateEvents.AjaxFeedbackFailure)) {

            // Log.d(TAG, "AjaxFeedbackFailure");
            final int code = event.getData("code");
            onFeedbackFailed(code);

        } else if (type.equals(StateEvents.AjaxFeedbackSuccess)) {

            // Log.d(TAG, "AjaxFeedbackSuccess");
            // final String response = event.<String> getData("response");
            final TreeModel service = event.<TreeModel> getData("service");
            final String label = event.<String> getData("label");
            final List<ModelData> feedback = event.<List<ModelData>> getData("feedback");
            onFeedbackMarked(service, label, feedback);

        } else {
            forwardToView(this.form, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.form = new FeedbackForm(this);
    }

    private void markFeedback(TreeModel service, String label, List<ModelData> feedback) {

        ModelData sensor = service.getChild(0);
        ModelData feedbackPeriod = feedback.get(0);

        // prepare request properties
        final String method = "POST";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services/"
                + service.<String> get("id") + "/manualLearn.json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEvents.AjaxFeedbackSuccess);
        onSuccess.setData("service", service);
        onSuccess.setData("label", label);
        onSuccess.setData("feedback", feedback);
        final AppEvent onFailure = new AppEvent(StateEvents.AjaxFeedbackFailure);

        // prepare request body
        String body = "{\"start_date\":\"" + feedbackPeriod.get("start") + "\"";
        body += ",\"end_date\":\"" + feedbackPeriod.get("end") + "\"";
        body += ",\"class_label\":\"" + label + "\"}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    public void onFeedbackMarked(TreeModel service, String label, List<ModelData> feedback) {

        if (feedback.size() > 1) {
            // remove 0th feedback item, repeat
            feedback.remove(0);
            markFeedback(service, label, feedback);
        } else {
            forwardToView(this.form, new AppEvent(StateEvents.FeedbackComplete));
        }
    }

    public void onFeedbackFailed(int code) {
        forwardToView(this.form, new AppEvent(StateEvents.FeedbackFailed));
    }
}
