package nl.sense_os.commonsense.common.client.communication.httpresponse;

import nl.sense_os.commonsense.common.client.model.Environment;

import com.google.gwt.core.client.JavaScriptObject;

public class CreateEnvironmentResponse extends JavaScriptObject {

	protected CreateEnvironmentResponse() {
		// empty protected constructor
	}

	public final native Environment getEnvironment() /*-{
		if (undefined != this.environment) {
			return this.environment;
		} else {
			return {};
		}
	}-*/;
}
