package nl.sense_os.commonsense.common.client.communication.httpresponse;

import com.google.gwt.core.client.JavaScriptObject;

public class LoginResponseJso extends JavaScriptObject {

    protected LoginResponseJso() {
        // empty protected constructor
    }

    public final native String getSessionId() /*-{
		return this.session_id;
    }-*/;
}
