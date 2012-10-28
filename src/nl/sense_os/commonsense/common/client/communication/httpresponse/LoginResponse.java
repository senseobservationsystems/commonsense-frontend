package nl.sense_os.commonsense.common.client.communication.httpresponse;

import com.google.gwt.core.client.JavaScriptObject;

public class LoginResponse extends JavaScriptObject {

	protected LoginResponse() {
		// empty protected constructor
	}

	public final native String getSessionId() /*-{
		if (undefined != this.session_id) {
			return this.session_id;
		} else {
			return '';
		}
	}-*/;
}
