package nl.sense_os.commonsense.client.common.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Overlay for group objects that are sent from CommonSense back end using JSON.
 */
public class GroupJso extends JavaScriptObject {

    protected GroupJso() {
        // empty protected constructor
    }

    public native final String getDescription() /*-{
        return this.description;
    }-*/;

    public native final String getEmail() /*-{
        return this.email;
    }-*/;

    public native final int getId() /*-{
        return parseInt(this.id);
    }-*/;

    public native final String getName() /*-{
        return this.name;
    }-*/;

    public final List<String> getOptSensors() {
        JsArrayString raw = getRawOptSensors();
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < raw.length(); i++) {
            result.add(raw.get(i));
        }
        return result;
    }

    public native final JsArrayString getRawOptSensors() /*-{
        if (undefined != this.optional_sensors) {
            return this.optional_sensors;
        } else {
            return [];
        }
    }-*/;

    public native final JsArrayString getRawReqSensors() /*-{
        if (undefined != this.required_sensors) {
            return this.required_sensors;
        } else {
            return [];
        }
    }-*/;

    public final List<String> getReqSensors() {
        JsArrayString raw = getRawReqSensors();
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < raw.length(); i++) {
            result.add(raw.get(i));
        }
        return result;
    }

    public native final boolean hasAccessPassword() /*-{
        return this.has_access_password == true;
    }-*/;

    public native final boolean isAnonymous() /*-{
        return this.anonymous == true;
    }-*/;

    public native final boolean isHidden() /*-{
        return this.hidden == true;
    }-*/;

    public native final boolean isPublic() /*-{
        return this['public'] == true;
    }-*/;

    public native final boolean isShowEmailReq() /*-{
        return this.required_show_email == true;
    }-*/;

    public native final boolean isShowFirstNameReq() /*-{
        return this.required_show_first_name == true;
    }-*/;

    public native final boolean isShowIdReq() /*-{
        return this.required_show_id == true;
    }-*/;

    public native final boolean isShowPhoneReq() /*-{
        return this.required_show_phone_number == true;
    }-*/;

    public native final boolean isShowSurnameReq() /*-{
        return this.required_show_surname == true;
    }-*/;

    public native final boolean isShowUsernameReq() /*-{
        return this.required_show_username == true;
    }-*/;
}
