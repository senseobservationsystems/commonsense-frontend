package nl.sense_os.commonsense.client.auth.login;

import nl.sense_os.commonsense.client.common.models.UserJso;
import nl.sense_os.commonsense.client.common.models.UserModel;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay for response to request for current user from CommonSense.
 */
public class CurrentUserResponseJso extends JavaScriptObject {

    protected CurrentUserResponseJso() {
        // empty protected constructor
    }

    public final native UserJso getRawUser() /*-{
		return this.user;
    }-*/;

    public final UserModel getUser() {
        UserJso jso = getRawUser();
        if (null != jso) {
            return new UserModel(jso);
        } else {
            return null;
        }
    }
}
