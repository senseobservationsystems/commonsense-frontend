package nl.sense_os.commonsense.client.ajax;

import java.util.HashMap;

import com.extjs.gxt.ui.client.event.EventType;

public class Ajax {

    private static final String TAG = "Ajax";

    /**
     * Does cross-domain request using the JSNI to make this work for IE. Calls back to
     * {@link AjaxController#onSuccess(String, EventType)} after the request is complete, or to
     * {@link AjaxController#onFailure()} or {@link AjaxController#onAuthError()} if something went
     * wrong.
     * 
     * @param method
     *            HTTP method
     * @param url
     * @param sessionId
     *            Optional session ID for authentication. Will be sent as X-SESSION_ID header (or as
     *            URL parameter in IE)
     * @param body
     *            String with optional body for the request (e.g. for POST or PUT requests)
     * @param params
     *            HTTP parameters
     * @param onSuccess
     *            EventType for the event for dispatch after the request is complete
     * @param onFailure
     *            EventType for the event for dispatch if the request fails
     * @param handler
     *            AjaxController instance to return the ajax result to
     * 
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public static native void request(
    		String method,
    		String url,
            String sessionId,
    		String body,
    		HashMap<String, Object> params,
            EventType onSuccess,
            EventType onFailure,
    		AjaxController handler) /*-{

		var isIE8 = window.XDomainRequest ? true : false;
		var xhr = createCrossDomainRequest();

		function createCrossDomainRequest() {
			return (isIE8) ? new window.XDomainRequest() : new XMLHttpRequest();
		}

		function readyStateHandler() {
			if (xhr.readyState == 4) {
				if (xhr.status == 200) {
					handleSuccess();
				} else if (xhr.status == 403) {
					handleAuthError();
				} else {
					handleFailure();
				}
			}
		}

		// NB: this is only called by Chrome, other browsers do not give the status code of failed requests
		function handleAuthError() {
			handler.@nl.sense_os.commonsense.client.ajax.AjaxController::onAuthError(ILcom/extjs/gxt/ui/client/event/EventType;)(xhr.status, onFailure);
		}

		// @@ FIXME: this method should be as general as we can to use in any case.
		function handleFailure() {
			handler.@nl.sense_os.commonsense.client.ajax.AjaxController::onFailure(ILcom/extjs/gxt/ui/client/event/EventType;)(xhr.status, onFailure);
		}

		function handleSuccess() {
			handler.@nl.sense_os.commonsense.client.ajax.AjaxController::onSuccess(Ljava/lang/String;Lcom/extjs/gxt/ui/client/event/EventType;)(xhr.responseText, onSuccess);
		}

		if (xhr) {
			if (isIE8) {
				if (undefined != sessionId) {
					url = url + "&session_id=" + sessionId;
				}
				xhr.open(method, url);
				xhr.onload = onSuccess;
				xhr.onerror = onFailure;
				xhr.send(body);
			} else {
				xhr.open(method, url, true);
				xhr.onreadystatechange = readyStateHandler;
				if (undefined != sessionId) {
					xhr.setRequestHeader("X-SESSION_ID", sessionId);
				}
				xhr.send(body);
			}
		} else {
			handleFailure();
		}
    }-*/;
    // @formatter:on
}
