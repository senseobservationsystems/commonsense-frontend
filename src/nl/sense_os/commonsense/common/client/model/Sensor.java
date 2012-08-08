package nl.sense_os.commonsense.common.client.model;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.common.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay for sensor objects that are sent from CommonSense back end using JSON.
 */
public class Sensor extends JavaScriptObject {

	protected Sensor() {
		// protected empty constructor
	}

	public final native String getDataStructure() /*-{
		return this.data_structure;
	}-*/;

	public final native String getDataType() /*-{
		return this.data_type;
	}-*/;

	public final ExtDevice getDevice() {
		Device jso = getRawDevice();
		if (null != jso) {
			return new ExtDevice(jso);
		} else {
			return null;
		}
	}

	public final native String getDisplayName() /*-{
		if (undefined == this.display_name || this.display_name === '') {
			return this.name;
		} else {
			return this.display_name;
		}
	}-*/;

	public final ExtEnvironment getEnvironment() {
		Environment jso = getRawEnvironment();
		if (null != jso) {
			return new ExtEnvironment(jso);
		} else {
			return null;
		}
	}

	public final native int getId() /*-{
		return parseInt(this.id);
	}-*/;

	public final native String getName() /*-{
		return this.name;
	}-*/;

	public final ExtUser getOwner() {
		User jso = getRawOwner();
		if (null != jso) {
			return new ExtUser(jso);
		} else {
			return Registry.get(Constants.REG_USER);
		}
	}

	public final native String getPagerType() /*-{
		return this.pager_type;
	}-*/;

	public final native String getPhysicalSensor() /*-{
		return this.device_type;
	}-*/;

	public final native Device getRawDevice() /*-{
		return this.device;
	}-*/;

	public final native Environment getRawEnvironment() /*-{
		return this.environment;
	}-*/;

	public final native User getRawOwner() /*-{
		return this.owner;
	}-*/;

	public final native int getType() /*-{
		return parseInt(this.type);
	}-*/;

	public final native JsArray<User> getRawUsers() /*-{
		if (undefined != this.users) {
			return this.users;
		} else {
			return [];
		}
	}-*/;

	public final List<ExtUser> getUsers() {
		List<ExtUser> list = new ArrayList<ExtUser>();

		JsArray<User> rawUsers = getRawUsers();

		for (int i = 0; i < rawUsers.length(); i++) {
			list.add(new ExtUser(rawUsers.get(i)));
		}

		return list;
	}
}
