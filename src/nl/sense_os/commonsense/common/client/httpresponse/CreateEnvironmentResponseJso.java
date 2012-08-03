package nl.sense_os.commonsense.common.client.httpresponse;

import nl.sense_os.commonsense.common.client.model.EnvironmentJso;
import nl.sense_os.commonsense.common.client.model.EnvironmentModel;

import com.google.gwt.core.client.JavaScriptObject;

public class CreateEnvironmentResponseJso extends JavaScriptObject {

    protected CreateEnvironmentResponseJso() {
        // empty protected constructor
    }

    public final native EnvironmentJso getRawEnvironment() /*-{
		return this.environment;
    }-*/;

    public final EnvironmentModel getEnvironment() {

        EnvironmentJso environment = getRawEnvironment();
        if (null != environment) {
            return new EnvironmentModel(environment);
        } else {
            return null;
        }
    }
}
