package nl.sense_os.commonsense.client.states.feedback;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.states.edit.ServiceMethodResponseJso;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
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

        registerEventTypes(FeedbackEvents.FeedbackSubmit);

        registerEventTypes(FeedbackEvents.LabelsRequest);
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

        } else

        /*
         * Get state labels.
         */
        if (type.equals(FeedbackEvents.LabelsRequest)) {
            // LOG.fine( "LabelsRequest");
            final SensorModel state = event.getData("state");
            final List<SensorModel> sensors = event.getData("sensors");
            getLabels(state, sensors);

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

        // parse result from the GetClassLabels response
        String resultString = null;
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            ServiceMethodResponseJso jso = JsonUtils.unsafeEval(response);
            resultString = jso.getResult();
        }

        // parse labels from raw result String
        if (resultString != null) {
            resultString = resultString.replaceAll("&quot;", "\"");
            JSONValue rawResult = JSONParser.parseStrict(resultString);
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
    }

    private void onLabelsComplete(SensorModel state, List<SensorModel> sensors, List<String> labels) {
        AppEvent event = new AppEvent(FeedbackEvents.LabelsSuccess);
        event.setData("state", state);
        event.setData("sensors", sensors);
        event.setData("labels", labels);
        forwardToView(this.feedback, event);
    }

    private void getLabels(final SensorModel state, final List<SensorModel> sensors) {

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
            final Method method = RequestBuilder.GET;
            final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/" + state.getId()
                    + "/GetClassLabels.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("GET class labels onError callback: " + exception.getMessage());
                    onLabelsFailure(0);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("GET class labels response received: " + response.getStatusText());
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onLabelsSuccess(response.getText(), state, sensors);
                    } else {
                        LOG.warning("GET class labels returned incorrect status: " + statusCode);
                        onLabelsFailure(statusCode);
                    }
                }
            };

            // send request
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("X-SESSION_ID", sessionId);
            try {
                builder.sendRequest(null, reqCallback);
            } catch (RequestException e) {
                LOG.warning("GET class labels request threw exception: " + e.getMessage());
                onLabelsFailure(0);
            }

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

    private void markFeedback(final SensorModel state, final List<FeedbackData> changes,
            final int index, final FeedbackPanel panel) {

        SensorModel sensor = (SensorModel) state.getChild(0);

        if (index < changes.size()) {
            FeedbackData change = changes.get(index);

            // TODO also process delete changes
            while (change.getType() == FeedbackData.TYPE_REMOVE) {
                LOG.warning("Skipping feedback deletion!");
                int newIndex = index;
                newIndex++;
                if (newIndex < changes.size()) {
                    change = changes.get(newIndex);
                } else {
                    onFeedbackComplete(panel);
                    return;
                }
            }

            // prepare request properties
            final Method method = RequestBuilder.POST;
            final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/" + state.getId()
                    + "/manualLearn.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);

            // prepare request body
            String body = "{\"start_date\":\""
                    + NumberFormat.getFormat("#.000").format(change.getStart() / 1000d) + "\"";
            body += ",\"end_date\":\""
                    + NumberFormat.getFormat("#.000").format(change.getEnd() / 1000d) + "\"";
            body += ",\"class_label\":\"" + change.getLabel() + "\"}";

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("POST feedback onError callback: " + exception.getMessage());
                    onFeedbackFailed(panel);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("POST feedback response received: " + response.getStatusText());
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onFeedbackMarked(state, changes, index, panel);
                    } else {
                        LOG.warning("POST feedback returned incorrect status: " + statusCode);
                        onFeedbackFailed(panel);
                    }
                }
            };

            // send request
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("X-SESSION_ID", sessionId);
            try {
                builder.sendRequest(body, reqCallback);
            } catch (RequestException e) {
                LOG.warning("POST feedback request threw exception: " + e.getMessage());
                onFeedbackFailed(panel);
            }

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
