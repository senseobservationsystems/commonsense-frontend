package nl.sense_os.commonsense.common.client.communication.httpresponse;

import java.util.List;

import nl.sense_os.commonsense.common.client.model.Service;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

import com.google.gwt.core.client.JsArray;

public class BatchAvailServicesResponse extends SenseApiResponse {

	protected BatchAvailServicesResponse() {
		// empty protected constructor
	}

	public final native JsArray<AvailServicesResponseEntry> getEntries() /*-{
		if (undefined != this.available_services) {
			return this.available_services;
		} else {
			return [];
		}
	}-*/;

	public final native int getTotal() /*-{
		return this.total;
	}-*/;

	public final int getSensorId(int index) {
		JsArray<AvailServicesResponseEntry> entries = getEntries();
		return entries.get(index).getSensorId();
	}

	public final List<Service> getServices(int index) {
		JsArray<AvailServicesResponseEntry> entries = getEntries();
		return entries.get(index).getServices();
	}
}
