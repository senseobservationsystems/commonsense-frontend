package nl.sense_os.commonsense.client.ajax;

import java.util.HashMap;

import nl.sense_os.commonsense.client.controllers.VizController;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

public class Ajax {
	
    private static final String TAG = "Ajax";	
	
    /**
     * Requests sensor data from CommonSense. Calls back to
     * {@link #parseData(String, TreeModel, SensorValueModel[], int, VizController, String)},
     * {@link VizController#onDataAuthError()}, or {@link VizController#onDataFailed(TreeModel)}.
     * 
     * @param method HTTP method
     * @param url
     * @param sessionId
     * @param params HTTP params
     * @param event
     * @param handler AjaxController object to handle the callbacks
     * 
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public static native void request(
    		String method,
    		String url,
    		String sessionId,
    		HashMap<String, Object> params,
    		AppEvent event,
    		AjaxController handler) /*-{
            	
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            return (isIE8) ? new window.XDomainRequest() : new XMLHttpRequest();
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { onSuccess(); } 
                else if (xhr.status == 403) { onAuthError(); } 
                else { onFailure(); }
            }
        }

        function onAuthError() {
            handler.@nl.sense_os.commonsense.client.ajax.AjaxController::onAuthError()();
        }

        // @@ FIXME: this method should be as general as we can to use in any case.
        function onFailure() {
            handler.@nl.sense_os.commonsense.client.ajax.AjaxController::onFailure()();
        }

		// @@ FIXME: fix this method
        function onSuccess() {
            @nl.sense_os.commonsense.client.ajax.Ajax::setData(Ljava/lang/String; Lcom/extjs/gxt/ui/client/mvc/AppEvent;)(xhr.responseText, event);
        }

        if (xhr) {
            if (isIE8) {
                url = url + "&session_id=" + sessionId;
                xhr.open(method, url);
                xhr.onload = onSuccess;
                xhr.onerror = onFailure;
                xhr.send();
            } else {
                xhr.open(method, url, true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID", sessionId);
                xhr.send();
            }
        } else {
            onFailure();
        }
    }-*/;
    // @formatter:on

    /**
     * @@ TODO
     * 
     * @param response
     * @param event
     */
    private static void setData(String response, AppEvent event) {
       	// @@ FIXME: what kind of data should we put in the event?
       	
        EventType forwardEvt = (EventType) event.getData("forward_evt");
       	AppEvent dstEvent = new AppEvent(forwardEvt);
       	
    	// Put the ajax response in the event.  	
       	dstEvent.setData("response", response);

       	// Dispatch the destination event.       	
		Dispatcher.forwardEvent(dstEvent);
	}
    
}
