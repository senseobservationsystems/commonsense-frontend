package nl.sense_os.commonsense.client.states.edit;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.ServiceMethodModel;

import com.extjs.gxt.ui.client.Registry;
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

public class StateEditController extends Controller {
    private static final Logger LOG = Logger.getLogger(StateEditController.class.getName());
    private View editor;

    public StateEditController() {
        registerEventTypes(StateEditEvents.ShowEditor);

        // perform a method for this state
        registerEventTypes(StateEditEvents.InvokeMethodRequested);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        /*
         * Invoke a service method
         */
        if (type.equals(StateEditEvents.InvokeMethodRequested)) {
            // LOG.fine( "InvokeMethodRequested");
            invokeMethod(event);

        } else

        /*
         * Pass through to editor view
         */
        {
            forwardToView(editor, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        editor = new StateEditor(this);
    }

    private void invokeMethod(AppEvent event) {

        // get event info
        SensorModel stateSensor = event.<SensorModel> getData("stateSensor");
        SensorModel sensor = (SensorModel) stateSensor.getChild(0);
        ServiceMethodModel serviceMethod = event.<ServiceMethodModel> getData("method");
        List<String> params = event.<List<String>> getData("parameters");

        // prepare request properties
        final Method method = params.size() > 0 ? RequestBuilder.POST : RequestBuilder.GET;
        final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/" + stateSensor.getId()
                + "/" + serviceMethod.getName() + ".json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        // create request body
        String body = null;
        if (params.size() > 0) {
            body = "{\"parameters\":[";
            for (String p : params) {
                body += "\"" + p + "\",";
            }
            body = body.substring(0, body.length() - 1);
            body += "]}";
        }

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("POST service method onError callback: " + exception.getMessage());
                onInvokeMethodFailure(0);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST service method response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onInvokeMethodSuccess(response.getText());
                } else {
                    LOG.warning("POST service method returned incorrect status: " + statusCode);
                    onInvokeMethodFailure(statusCode);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(method, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        try {
            builder.sendRequest(body, reqCallback);
        } catch (RequestException e) {
            LOG.warning("POST service method request threw exception: " + e.getMessage());
            onInvokeMethodFailure(0);
        }
    }

    private void onInvokeMethodFailure(int code) {
        forwardToView(editor, new AppEvent(StateEditEvents.InvokeMethodFailed));
    }

    private void onInvokeMethodSuccess(String response) {

        // parse result from the response
        String result = null;
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            ServiceMethodResponseJso jso = JsonUtils.unsafeEval(response);
            result = jso.getResult();
        }

        if (result != null) {
            forwardToView(editor, new AppEvent(StateEditEvents.InvokeMethodComplete, result));
        } else {
            onInvokeMethodFailure(0);
        }
    }
}
