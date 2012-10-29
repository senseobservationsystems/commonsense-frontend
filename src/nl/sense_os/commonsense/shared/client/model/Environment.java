package nl.sense_os.commonsense.shared.client.model;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay for environment objects that are sent from CommonSense back end using JSON.
 */
public class Environment extends JavaScriptObject {

    protected Environment() {
        // empty protected constructor
    }

    public final Date getDate() {
        long date = Math.round(getRawDate() * 1000);
        return new Date(date);
    }

    public final native String getId() /*-{
		return this.id;
    }-*/;

    public final native String getName() /*-{
		return this.name;
    }-*/;

    public final native int getFloors() /*-{
		if (undefined == this.floors) {
			return -1;
		} else {
			return parseInt(this.floors);
		}
    }-*/;

    public final native String getRawOutline() /*-{
		if (undefined != this.gps_outline) {
			return this.gps_outline;
		} else {
			return '';
		}
    }-*/;

	public final native String getPosition() /*-{
		if (undefined != this.position) {
			return this.position;
		} else {
			return '';
		}
    }-*/;

    private final native double getRawDate() /*-{
		if (undefined != this.date) {
			return this.date;
		} else {
			return -1;
		}
    }-*/;
}
