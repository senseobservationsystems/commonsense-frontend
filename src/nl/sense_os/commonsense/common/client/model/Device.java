package nl.sense_os.commonsense.common.client.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay for device objects that are sent from CommonSense back end using JSON.
 */
public class Device extends JavaScriptObject {

    protected Device() {
        // empty protected constructor
    }

    public final native int getId() /*-{
		return parseInt(this.id);
    }-*/;

    public final native String getType() /*-{
		return this.type;
    }-*/;

    public final native String getUuid() /*-{
		return this.uuid;
    }-*/;
}
