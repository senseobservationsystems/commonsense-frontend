package nl.sense_os.commonsense.common.client.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay for user objects that are sent from CommonSense back end using JSON.
 */
public class UserJso extends JavaScriptObject {

    protected UserJso() {
        // empty protected constructor
    }

    public final native String getEmail() /*-{
		if (undefined != this.email) {
			return this.email;
		} else {
			return '';
		}
    }-*/;

    public final native int getId() /*-{
		return parseInt(this.id);
    }-*/;

    public final native String getMobile() /*-{
		if (undefined != this.mobile) {
			return this.mobile;
		} else {
			return '';
		}
    }-*/;

    public final native String getName() /*-{
		if (undefined != this.name) {
			return this.name;
		} else {
			return '';
		}
    }-*/;

    public final native String getSurname() /*-{
		if (undefined != this.surname) {
			return this.surname;
		} else {
			return '';
		}
    }-*/;

    public final native String getUsername() /*-{
		if (undefined != this.username) {
			return this.username;
		} else {
			return '';
		}
    }-*/;
}
