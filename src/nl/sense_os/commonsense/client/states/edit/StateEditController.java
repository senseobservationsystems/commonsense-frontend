package nl.sense_os.commonsense.client.states.edit;

import java.util.List;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class StateEditController extends Controller {
    private static final String TAG = "StateEditController";
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
            // Log.d(TAG, "InvokeMethodRequested");
            invokeMethod(event);

        } else if (type.equals(StateEditEvents.InvokeMethodAjaxFailure)) {
            Log.w(TAG, "AjaxMethodFailure");
            final int code = event.getData("code");
            invokeMethodErrorCallback(code);

        } else if (type.equals(StateEditEvents.InvokeMethodAjaxSuccess)) {
            // Log.d(TAG, "AjaxMethodSuccess");
            final String response = event.<String> getData("response");
            invokeMethodCallback(response);

        } else

        /*
         * Pass through to editor view
         */
        {
            forwardToView(this.editor, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.editor = new StateEditor(this);
    }

    private void invokeMethod(AppEvent event) {

        // get event info
        TreeModel service = event.<TreeModel> getData("service");
        ModelData sensor = service.getChild(0);
        ModelData serviceMethod = event.<ModelData> getData("method");
        List<String> params = event.<List<String>> getData("parameters");

        // prepare request properties
        final String method = params.size() > 0 ? "POST" : "GET";
        final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services/"
                + service.<String> get("id") + "/" + serviceMethod.<String> get("name") + ".json";
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

    private void invokeMethodCallback(String response) {

        if (response != null) {
            // try to get "methods" array
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonVal = json.get("result");
            if (null != jsonVal) {
                JSONString jsonResult = jsonVal.isString();
                if (null != jsonResult) {
                    String result = jsonResult.stringValue();
                    forwardToView(this.editor, new AppEvent(StateEditEvents.InvokeMethodComplete,
                            result));
                } else {
                    Log.e(TAG,
                            "Error parsing service methods response: \"result\" is not a JSON String");
                    invokeMethodErrorCallback(0);
                }
            } else {
                Log.e(TAG, "Error parsing service methods response: \"result\" is is not found");
                invokeMethodErrorCallback(0);
            }
        } else {
            Log.e(TAG, "Error parsing service methods response: response=null");
            invokeMethodErrorCallback(0);
        }
    }

    private void invokeMethodErrorCallback(int code) {
        forwardToView(this.editor, new AppEvent(StateEditEvents.InvokeMethodFailed));
    }

}
