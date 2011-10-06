package nl.sense_os.commonsense.client.auth.login;

import com.google.gwt.core.client.JavaScriptObject;

public class LoginResponseJso extends JavaScriptObject {

    protected LoginResponseJso() {
        // empty protected constructor
    }

    public final native String getSessionId() /*-{
		return this.session_id;
    }-*/;
}
