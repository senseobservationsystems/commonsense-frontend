package nl.sense_os.commonsense.client.states.feedback;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class FeedbackController extends Controller {

    private static final Logger LOG = Logger.getLogger(FeedbackController.class.getName());
    private View feedback;
    private View chooser;

    public FeedbackController() {

        LOG.setLevel(Level.WARNING);

        registerEventTypes(FeedbackEvents.FeedbackInit);
        registerEventTypes(FeedbackEvents.ShowChooser, FeedbackEvents.FeedbackChosen);

        registerEventTypes(FeedbackEvents.FeedbackSubmit, FeedbackEvents.FeedbackAjaxFailure,
                FeedbackEvents.FeedbackAjaxSuccess);

        registerEventTypes(FeedbackEvents.LabelsRequest, FeedbackEvents.LabelsAjaxSuccess,
                FeedbackEvents.LabelsAjaxFailure);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        /*
         * Submit feedback data.
         */
        if (type.equals(FeedbackEvents.FeedbackSubmit)) {
            // LOG.fine( "FeedbackSubmit");
            final SensorModel state = event.<SensorModel> getData("state");
            final List<FeedbackData> changes = event.<List<FeedbackData>> getData("changes");
            final FeedbackPanel panel = event.<FeedbackPanel> getData("panel");
            markFeedback(state, changes, 0, panel);

        } else if (type.equals(FeedbackEvents.FeedbackAjaxSuccess)) {
            // LOG.fine( "AjaxFeedbackSuccess");
            // final String response = event.<String> getData("response");
            final SensorModel state = event.<SensorModel> getData("state");
            final List<FeedbackData> changes = event.<List<FeedbackData>> getData("changes");
            final int index = event.getData("index");
            final FeedbackPanel panel = event.<FeedbackPanel> getData("panel");
            onFeedbackMarked(state, changes, index, panel);

        } else if (type.equals(FeedbackEvents.FeedbackAjaxFailure)) {
            LOG.warning("AjaxFeedbackFailure");
            // final int code = event.getData("code");
            final FeedbackPanel panel = event.<FeedbackPanel> getData("panel");
            onFeedbackFailed(panel);

        } else

        /*
         * Get state labels.
         */
        if (type.equals(FeedbackEvents.LabelsRequest)) {
            // LOG.fine( "LabelsRequest");
            final SensorModel state = event.getData("state");
            final List<SensorModel> sensors = event.getData("sensors");
            getLabels(state, sensors);

        } else if (type.equals(FeedbackEvents.LabelsAjaxSuccess)) {
            // LOG.fine( "LabelsAjaxSuccess");
            final String response = event.getData("response");
            final SensorModel state = event.getData("state");
            final List<SensorModel> sensors = event.getData("sensors");
            onLabelsSuccess(response, state, sensors);

        } else if (type.equals(FeedbackEvents.LabelsAjaxFailure)) {
            LOG.warning("LabelsAjaxFailure");
            final int code = event.getData("code");
            onLabelsFailure(code);

        } else

        /*
         * Feedback settings chooser
         */
        if (type.equals(FeedbackEvents.ShowChooser)) {
            forwardToView(this.chooser, event);

        } else

        /*
         * Pass through to view.
         */
        {
            forwardToView(this.feedback, event);
        }
    }

    private void onLabelsFailure(int code) {
        forwardToView(this.feedback, new AppEvent(FeedbackEvents.LabelsFailure));
    }

    private void onLabelsSuccess(String response, SensorModel state, List<SensorModel> sensors) {

        if (response != null) {
            JSONValue rawJson = JSONParser.parseStrict(response);
            if (null != rawJson) {
                JSONObject json = rawJson.isObject();
                if (null != json) {
                    JSONValue rawResult = json.get("result");
                    if (null != rawResult) {
                        JSONString rawResultString = rawResult.isString();
                        if (null != rawResultString) {
                            String resultString = rawResultString.stringValue();
                            resultString = resultString.replaceAll("&quot;", "\"");
                            rawResult = JSONParser.parseStrict(resultString);
                            if (null != rawResult) {
                                JSONObject result = rawResult.isObject();
                                JSONValue rawLabels = result.get("classLabels");
                                if (null != rawLabels) {
                                    JSONArray labels = rawLabels.isArray();
                                    if (null != labels) {
                                        List<String> list = new ArrayList<String>();
                                        JSONString rawString;
                                        for (int i = 0; i < labels.size(); i++) {
                                            rawString = labels.get(i).isString();
                                            if (null != rawString) {
                                                list.add(rawString.stringValue());
                                            } else {
                                                LOG.warning("label is not a JSON string");
                                                onLabelsFailure(0);
                                            }
                                        }

                                        onLabelsComplete(state, sensors, list);

                                    } else {
                                        LOG.warning("\"classLabels\" is not a JSON array");
                                        onLabelsFailure(0);
                                    }
                                } else {
                                    LOG.warning("\"classLabels\" is not valid JSON");
                                    onLabelsFailure(0);
                                }
                            } else {
                                LOG.warning("result is not valid JSON");
                                onLabelsFailure(0);
                            }
                        } else {
                            LOG.warning("\"result\" is not a JSON string");
                            onLabelsFailure(0);
                        }
                    } else {
                        LOG.warning("\"result\" is not valid JSON");
                        onLabelsFailure(0);
                    }
                } else {
                    LOG.warning("response is not a JSON object");
                    onLabelsFailure(0);
                }
            } else {
                LOG.warning("response is not valid JSON");
                onLabelsFailure(0);
            }
        } else {
            LOG.warning("response=null");
            onLabelsFailure(0);
        }

    }

    private void onLabelsComplete(SensorModel state, List<SensorModel> sensors, List<String> labels) {
        AppEvent event = new AppEvent(FeedbackEvents.LabelsSuccess);
        event.setData("state", state);
        event.setData("sensors", sensors);
        event.setData("labels", labels);
        forwardToView(this.feedback, event);
    }

    private void getLabels(SensorModel state, List<SensorModel> sensors) {

        List<ModelData> methods = state.<List<ModelData>> get("methods");
        boolean canHazClassLabels = false;
        for (ModelData method : methods) {
            if (method.get("name").equals("GetClassLabels")) {
                canHazClassLabels = true;
                break;
            }
        }
        if (false == canHazClassLabels) {
            onLabelsComplete(state, sensors, new ArrayList<String>());
        }

        if (sensors.size() > 0) {
            SensorModel sensor = sensors.get(0);

            // prepare request properties
            final String method = "GET";
            final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/" + state.getId()
                    + "/GetClassLabels.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(FeedbackEvents.LabelsAjaxSuccess);
            onSuccess.setData("state", state);
            onSuccess.setData("sensors", sensors);
            final AppEvent onFailure = new AppEvent(FeedbackEvents.LabelsAjaxFailure);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);
            Dispatcher.forwardEvent(ajaxRequest);
        } else {
            LOG.warning("No sensors!");
            onLabelsFailure(0);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.feedback = new FeedbackView(this);
        this.chooser = new FeedbackChooser(this);
    }

    private void markFeedback(SensorModel state, List<FeedbackData> changes, int index,
            FeedbackPanel panel) {

        SensorModel sensor = (SensorModel) state.getChild(0);

        if (index < changes.size()) {
            FeedbackData change = changes.get(index);

            // TODO also process delete changes
            while (change.getType() == FeedbackData.TYPE_REMOVE) {
                LOG.warning("Skipping feedback deletion!");
                index++;
                if (index < changes.size()) {
                    change = changes.get(index);
                } else {
                    onFeedbackComplete(panel);
                    return;
                }
            }

            // prepare request properties
            final String method = "POST";
            final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/" + state.getId()
                    + "/manualLearn.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(FeedbackEvents.FeedbackAjaxSuccess);
            onSuccess.setData("state", state);
            onSuccess.setData("changes", changes);
            onSuccess.setData("index", index);
            onSuccess.setData("panel", panel);
            final AppEvent onFailure = new AppEvent(FeedbackEvents.FeedbackAjaxFailure);
            onFailure.setData("panel", panel);

            // prepare request body
            String body = "{\"start_date\":\""
                    + NumberFormat.getFormat("#.000").format(change.getStart() / 1000d) + "\"";
            body += ",\"end_date\":\""
                    + NumberFormat.getFormat("#.000").format(change.getEnd() / 1000d) + "\"";
            body += ",\"class_label\":\"" + change.getLabel() + "\"}";

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("body", body);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);
            Dispatcher.forwardEvent(ajaxRequest);

        } else {
            onFeedbackComplete(panel);
        }
    }

    private void onFeedbackMarked(SensorModel state, List<FeedbackData> changes, int index,
            FeedbackPanel panel) {
        index++;
        markFeedback(state, changes, index, panel);
    }

    private void onFeedbackFailed(FeedbackPanel panel) {
        LOG.fine("Feedback failure");
        AppEvent failure = new AppEvent(FeedbackEvents.FeedbackFailed);
        failure.setData("panel", panel);
        forwardToView(this.feedback, failure);
    }

    private void onFeedbackComplete(FeedbackPanel panel) {
        AppEvent complete = new AppEvent(FeedbackEvents.FeedbackComplete);
        complete.setData("panel", panel);
        forwardToView(this.feedback, complete);
    }
}
