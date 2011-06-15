package nl.sense_os.commonsense.client.states.edit;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.ServiceMethodModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;

public class StateEditController extends Controller {
    private static final Logger LOG = Logger.getLogger(StateEditController.class.getName());
    private View editor;

    public StateEditController() {
        registerEventTypes(StateEditEvents.ShowEditor);

        // perform a method for this state
        registerEventTypes(StateEditEvents.InvokeMethodRequested,
                StateEditEvents.InvokeMethodAjaxSuccess, StateEditEvents.InvokeMethodAjaxFailure);
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

        } else if (type.equals(StateEditEvents.InvokeMethodAjaxFailure)) {
            LOG.warning("AjaxMethodFailure");
            final int code = event.getData("code");
            onInvokeMethodFailure(code);

        } else if (type.equals(StateEditEvents.InvokeMethodAjaxSuccess)) {
            // LOG.fine( "AjaxMethodSuccess");
            final String response = event.<String> getData("response");
            onInvokeMethodSuccess(response);

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
        final String method = params.size() > 0 ? "POST" : "GET";
        final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/" + stateSensor.getId()
                + "/" + serviceMethod.getName() + ".json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateEditEvents.InvokeMethodAjaxSuccess);
        final AppEvent onFailure = new AppEvent(StateEditEvents.InvokeMethodAjaxFailure);

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
