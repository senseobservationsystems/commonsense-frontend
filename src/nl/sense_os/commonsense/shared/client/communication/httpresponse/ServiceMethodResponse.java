package nl.sense_os.commonsense.shared.client.communication.httpresponse;

import com.google.gwt.core.client.JavaScriptObject;

public class ServiceMethodResponse extends JavaScriptObject {

    protected ServiceMethodResponse() {
        // empty protected constructor
    }

    public final native String getResult() /*-{
        if (undefined != this.result) {
            return this.result;
        } else {
            return null;
        }
    }-*/;
}
