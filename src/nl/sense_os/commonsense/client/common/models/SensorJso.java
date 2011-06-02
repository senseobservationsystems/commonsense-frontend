package nl.sense_os.commonsense.client.common.models;

import nl.sense_os.commonsense.client.common.constants.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay for sensor objects that are sent from CommonSense back end using JSON.
 */
public class SensorJso extends JavaScriptObject {

    protected SensorJso() {
        // protected empty constructor
    }

    public final native String getDataStructure() /*-{
		return this.data_structure;
    }-*/;

    public final native String getDataType() /*-{
		return this.data_type;
    }-*/;

    public final DeviceModel getDevice() {
        DeviceJso jso = getRawDevice();
        if (null != jso) {
            return new DeviceModel(jso);
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

    public final EnvironmentModel getEnvironment() {
        EnvironmentJso jso = getRawEnvironment();
        if (null != jso) {
            return new EnvironmentModel(jso);
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

    public final UserModel getOwner() {
        UserJso jso = getRawOwner();
        if (null != jso) {
            return new UserModel(jso);
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

    public final native DeviceJso getRawDevice() /*-{
		return this.device;
    }-*/;

    public final native EnvironmentJso getRawEnvironment() /*-{
		return this.environment;
    }-*/;

    public final native UserJso getRawOwner() /*-{
		return this.owner;
    }-*/;

    public final native int getType() /*-{
		return parseInt(this.type);
    }-*/;
}
