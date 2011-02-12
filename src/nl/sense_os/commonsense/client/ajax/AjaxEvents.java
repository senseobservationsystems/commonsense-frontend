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
     *            EventType for the event for dispatch after the request is complete
     * @param onFailure
     *            EventType for the event for dispatch if the request fails
     */
    public static final EventType Request = new EventType();
}
