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
            // Log.d(TAG, "Request");
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
        AppEvent onSuccess = event.<AppEvent> getData("onSuccess");
        AppEvent onFailure = event.<AppEvent> getData("onFailure");

        Ajax.request(method, url, sessionId, body, onSuccess, onFailure, this);
    }

    /**
     * Dispatches an event to signal the request has failed. The dispatched event contains all the
     * necessary information about the request, plus the failed request's HTTP status code.
     * 
     * @param statusCode
     *            HTTP code of the response (will be 0 on most browsers, except Chrome)
     * @param onFailure
     *            AppEvent to dispatch
     */
    public void onFailure(String method, String url, String sessionId, String body, int statusCode,
            AppEvent onFailure) {
        Log.w(TAG, "onFailure");
        onFailure.setData("method", method);
        onFailure.setData("url", url);
        onFailure.setData("session_id", sessionId);
        onFailure.setData("body", body);
        onFailure.setData("code", statusCode);
        Dispatcher.forwardEvent(onFailure);
    }

    /**
     * Dispatches an event to signal the request has failed. Adds the request's HTTP status code to
     * the "code" property of the onFailure AppEvent. NB: this is not a reliable indicator that
     * there was an authentication error! This method will only be called in Google Chrome, other
     * browsers will return to {@link #onFailure(int, AppEvent)} when the request was forbidden.
     * 
     * @param statusCode
     *            HTTP code of the response (should be 403)
     * @param onFailure
     *            AppEvent to dispatch
     */
    public void onAuthError(String method, String url, String sessionId, String body,
            int statusCode, AppEvent onFailure) {
        Log.w(TAG, "onAuthError");
        onFailure(method, url, sessionId, body, statusCode, onFailure);
    }

    /**
     * Dispatches event to signal the request has successfully completed. Adds the response String
     * to the "response" property of the onSuccess AppEvent.
     * 
     * @param response
     *            response body text
     * @param onSuccess
     *            AppEvent to dispatch
     */
    public void onSuccess(String response, AppEvent onSuccess) {
        // Log.d(TAG, "onSuccess");
        onSuccess.setData("response", response);
        Dispatcher.forwardEvent(onSuccess);
    }

    public void onTimeOut(String method, String url, String sessionId, String body,
            AppEvent onFailure) {
        Log.w(TAG, "onTimeOut");
        onFailure(method, url, sessionId, body, -1, onFailure);
    }
}
