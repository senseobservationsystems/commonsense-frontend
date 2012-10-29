package nl.sense_os.commonsense.shared.client.communication.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.shared.client.model.Service;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class AvailServicesResponseEntry extends JavaScriptObject {

	protected AvailServicesResponseEntry() {
		// empty protected constructor
	}

    public native final String getSensorId() /*-{
		return this.sensor_id;
    }-*/;

	public final native JsArray<Service> getRawServices() /*-{
		if (undefined != this.services) {
			return this.services;
		} else {
			return [];
		}
    }-*/;

	public final List<Service> getServices() {
		List<Service> list = new ArrayList<Service>();

		JsArray<Service> services = getRawServices();
		if (null != services) {
			for (int i = 0; i < services.length(); i++) {
				list.add(services.get(i));
			}
		}
		return list;
	}
}
