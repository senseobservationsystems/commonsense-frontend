package nl.sense_os.commonsense.client.ajax;

import java.util.HashMap;

import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

public class AjaxController extends Controller {

    private static final String TAG = "AjaxController";

    public AjaxController() {
        registerEventTypes(AjaxEvents.Request);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(AjaxEvents.Request)) {
            doRequest(event);
        } else {
            Log.w(TAG, "Unexpected event received");
        }
    }

    /**
     * Does a cross-origin Ajax request. Gets the request parameters and callback EventType from the
     * event parameter, and passes them on to the generic
     * {@link Ajax#request(String, String, String, String, HashMap, AppEvent, AjaxController)}
     * method.
     * 
     * @param event
     *            AppEvent containing the properties of the request, and the EventType of the event
     *            that should be dispatched after the request is complete.
     */
    private void doRequest(AppEvent event) {

        String method = event.<String> getData("method");
        String url = event.<String> getData("url");
        String sessionId = event.<String> getData("session_id");
        String body = event.<String> getData("body");
        EventType onSuccess = event.<EventType> getData("onSuccess");
        EventType onFailure = event.<EventType> getData("onFailure");

        // @@ TODO: are extra http params needed?
        HashMap<String, Object> params = new HashMap<String, Object>();

        Ajax.request(method, url, sessionId, body, params, onSuccess, onFailure, this);
    }

    /**
     * Dispatches an event to signal the request has failed.
     * 
     * @param statusCode
     *            HTTP code of the response (will be 0 on most browsers, except Chrome)
     * @param onFailure
     *            EventType of event to dispatch
     */
    public void onFailure(int statusCode, EventType onFailure) {
        Dispatcher.forwardEvent(onFailure, statusCode);
    }

    /**
     * Dispatches an event to signal the request has failed.
     * 
     * @param statusCode
     *            HTTP code of the response (should be 403)
     * @param onFailure
     *            EventType of event to dispatch
     */
    public void onAuthError(int statusCode, EventType onFailure) {
        // @@ TODO: what to do on authentication error?
        Dispatcher.forwardEvent(onFailure, statusCode);
    }

    /**
     * Dispatches event to signal the request has successfully completed.
     * 
     * @param response
     *            response body text
     * @param onSuccess
     *            EventType of event to dispatch
     */
    public void onSuccess(String response, EventType onSuccess) {
        Dispatcher.forwardEvent(onSuccess, response);
    }
}
