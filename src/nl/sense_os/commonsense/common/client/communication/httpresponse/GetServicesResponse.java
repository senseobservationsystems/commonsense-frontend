package nl.sense_os.commonsense.common.client.communication.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.common.client.model.Service;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

import com.google.gwt.core.client.JsArray;

public class GetServicesResponse extends SenseApiResponse {

	protected GetServicesResponse() {
		// empty protected constructor
	}

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
