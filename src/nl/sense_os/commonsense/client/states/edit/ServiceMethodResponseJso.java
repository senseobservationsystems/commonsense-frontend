package nl.sense_os.commonsense.client.states.edit;

import com.google.gwt.core.client.JavaScriptObject;

public class ServiceMethodResponseJso extends JavaScriptObject {

    protected ServiceMethodResponseJso() {
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
