package nl.sense_os.commonsense.client.ajax;

import com.extjs.gxt.ui.client.event.EventType;

public class AjaxEvents {

    /**
     * Event to initiate Ajax request.
     * 
     * @param method
     *            HTTP method (String)
     * @param url
     *            URL (String)
     * @param sessionId
     *            Optional session ID String for authentication. Will be sent as X-SESSION_ID header
     *            (or as URL parameter in IE)
     * @param body
     *            String with optional body for the request (e.g. for POST or PUT requests)
     * @param onSuccess
     *            AppEvent to dispatch after the request is complete. Requesting controller should
     *            register for this event.
     * @param onFailure
     *            AppEvent to dispatch if the request fails. Requesting controller should register
     *            for this event.
     */
    public static final EventType Request = new EventType();
}
