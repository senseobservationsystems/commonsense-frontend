package nl.sense_os.commonsense.common.client.communication.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.common.client.model.ServiceMethod;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

import com.google.gwt.core.client.JsArray;

public class GetMethodsResponse extends SenseApiResponse {

	protected GetMethodsResponse() {
		// protected empty constructor
	}

	public native final JsArray<ServiceMethod> getRawMethods() /*-{
		if (undefined != this.methods) {
			return this.methods;
		} else {
			return [];
		}
	}-*/;

	public final List<ServiceMethod> getMethods() {

		JsArray<ServiceMethod> rawMethods = getRawMethods();

		List<ServiceMethod> list = new ArrayList<ServiceMethod>();
		for (int i = 0; i < rawMethods.length(); i++) {
			list.add(rawMethods.get(i));
		}

		return list;
	}
}
