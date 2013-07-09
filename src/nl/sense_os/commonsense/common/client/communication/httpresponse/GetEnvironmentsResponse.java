package nl.sense_os.commonsense.common.client.communication.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.common.client.model.Environment;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

import com.google.gwt.core.client.JsArray;

public class GetEnvironmentsResponse extends SenseApiResponse {

	protected GetEnvironmentsResponse() {
		// empty protected constructor
	}

	public final native JsArray<Environment> getRawEnvironments() /*-{
		if (undefined != this.environments) {
			return this.environments;
		} else {
			return [];
		}
	}-*/;

	public final List<Environment> getEnvironments() {
		JsArray<Environment> raw = getRawEnvironments();
		List<Environment> result = new ArrayList<Environment>(raw.length());
		for (int i = 0; i < raw.length(); i++) {
			result.add(raw.get(i));
		}
		return result;
	}
}
