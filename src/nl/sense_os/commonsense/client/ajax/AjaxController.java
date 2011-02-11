package nl.sense_os.commonsense.client.ajax;

import java.util.HashMap;

import nl.sense_os.commonsense.client.events.VizEvents;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

public class AjaxController extends Controller {

	public AjaxController() {
		registerEventTypes(AjaxEvents.OnRequest);
	}
	
	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		
		if (type.equals(AjaxEvents.OnRequest)) {
			String method = event.getData("method");
			String url = event.getData("url");
			String sessionId = event.getData("session_id");			
			//Controller handler = event.getData("handler");
			
			// @@ TODO: are extra http params needed?
			
			HashMap<String, Object> params = new HashMap<String, Object>();

			Ajax.request(method, url, sessionId, params, event, this);			
		}
		// ...
		// @@ TODO: Implement this!
	}

	public void onFailure() {
		// @@ TODO: Implement this!
		Dispatcher.forwardEvent(VizEvents.DataNotReceived);		
	}
	
	public void onAuthError() {
		// @@ TODO: Implement this!	
		Dispatcher.forwardEvent(VizEvents.DataNotReceived);
	}
	
	/*
	public void onSuccess() {
		// @@ TODO: Implement this!	
		Dispatcher.forwardEvent(VizEvents.DataReceived, data);
	}*/
		
}
