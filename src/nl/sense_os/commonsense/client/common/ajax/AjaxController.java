package nl.sense_os.commonsense.client.common.ajax;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

public class AjaxController extends Controller {

    private static final Logger LOG = Logger.getLogger(AjaxController.class.getName());

    public AjaxController() {
        LOG.setLevel(Level.WARNING);
        registerEventTypes(AjaxEvents.Request);
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

        LOG.fine("Request URL: '" + url + "'");

        Ajax.request(method, url, sessionId, body, onSuccess, onFailure, this);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(AjaxEvents.Request)) {
            LOG.finest("Request");
            doRequest(event);
        } else {
            LOG.warning("Unexpected event received");
        }
    }

    /**
     * Dispatches an event to signal the request has failed due to an authentication error. Adds the
     * request's HTTP status code to the "code" property of the onFailure AppEvent. NB: this is not
     * a reliable indicator that there was an authentication error! At the time of writing only
     * Google Chrome and FF4 support this, other browsers will return directly to
     * {@link #onFailure(String, String, String, String, int, AppEvent)} when the request was
     * forbidden.
     * 
     * @param method
     *            HTTP method of the failed request.
     * @param url
     *            URL of the failed request.
     * @param sessionId
     *            The session ID of the failed request.
     * @param body
     *            The body of the failed request.
     * @param onFailure
     *            Callback event type to signal failure.
     */
    public void onAuthError(String method, String url, String sessionId, String body,
            int statusCode, AppEvent onFailure) {
        LOG.warning("Authentication error!");
        onFailure(method, url, sessionId, body, statusCode, onFailure);
    }

    /**
     * Dispatches an event to signal the request has failed. The dispatched event contains all the
     * necessary information about the request, plus the failed request's HTTP status code if this
     * is available.
     * 
     * @param method
     *            HTTP method of the failed request.
     * @param url
     *            URL of the failed request.
     * @param sessionId
     *            The session ID of the failed request.
     * @param body
     *            The body of the failed request.
     * @param onFailure
     *            Callback event type to signal failure.
     */
    public void onFailure(String method, String url, String sessionId, String body, int statusCode,
            AppEvent onFailure) {
        LOG.warning("Failure! Code: " + statusCode);
        onFailure.setData("method", method);
        onFailure.setData("url", url);
        onFailure.setData("session_id", sessionId);
        onFailure.setData("body", body);
        onFailure.setData("code", statusCode);
        Dispatcher.forwardEvent(onFailure);
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
        LOG.finest("Success");
        LOG.fine("Response: '" + response + "'");
        onSuccess.setData("response", response);
        Dispatcher.forwardEvent(onSuccess);
    }

    /**
     * Dispatches an event to signal the request has failed due to time out, by calling through to
     * {@link #onFailure(String, String, String, String, int, AppEvent)}.
     * 
     * @param method
     *            HTTP method of the failed request.
     * @param url
     *            URL of the failed request.
     * @param sessionId
     *            The session ID of the failed request.
     * @param body
     *            The body of the failed request.
     * @param onFailure
     *            Callback event type to signal failure.
     */
    public void onTimeOut(String method, String url, String sessionId, String body,
            AppEvent onFailure) {
        LOG.warning("Time out!");
        onFailure(method, url, sessionId, body, -1, onFailure);
    }
}
