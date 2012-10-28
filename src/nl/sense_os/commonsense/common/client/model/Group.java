package nl.sense_os.commonsense.common.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay for group objects that are sent from CommonSense back end using JSON.
 */
public class Group extends JavaScriptObject {

    protected Group() {
        // empty protected constructor
    }

    public native final String getDescription() /*-{
		return this.description;
    }-*/;

    public native final String getEmail() /*-{
		return this.email;
    }-*/;

    public native final String getId() /*-{
		return this.id;
    }-*/;

    public native final String getName() /*-{
		return this.name;
    }-*/;

    public final List<String> getOptSensors() {
        String raw = getRawOptSensors();
        String[] split = raw.split(",");
        ArrayList<String> result = new ArrayList<String>(split.length);
        for (String s : split) {
            String name = s.trim();
            if (name.length() > 0) {
                result.add(name);
            }
        }
        return result;
    }

    public native final String getRawOptSensors() /*-{
		if (undefined != this.optional_sensors) {
			return this.optional_sensors;
		} else {
			return '';
		}
    }-*/;

    public native final String getRawReqSensors() /*-{
		if (undefined != this.required_sensors) {
			return this.required_sensors;
		} else {
			return '';
		}
    }-*/;

    public final List<String> getReqSensors() {
        String raw = getRawReqSensors();
        String[] split = raw.split(",");
        ArrayList<String> result = new ArrayList<String>(split.length);
        for (String s : split) {
            String name = s.trim();
            if (name.length() > 0) {
                result.add(name);
            }
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

    public native final boolean isAllowReadUsers() /*-{
		return this.default_list_users == true;
    }-*/;

    public native final boolean isAllowCreateUsers() /*-{
		return this.default_add_users == true;
    }-*/;

    public native final boolean isAllowDeleteUsers() /*-{
		return this.default_remove_users == true;
    }-*/;

    public native final boolean isAllowReadSensors() /*-{
		return this.default_list_sensors == true;
    }-*/;

    public native final boolean isAllowCreateSensors() /*-{
		return this.default_add_sensors == true;
    }-*/;

    public native final boolean isAllowDeleteSensors() /*-{
		return this.default_remove_sensors == true;
    }-*/;
}
