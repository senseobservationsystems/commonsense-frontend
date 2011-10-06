package nl.sense_os.commonsense.client.env.list;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.models.EnvironmentJso;
import nl.sense_os.commonsense.client.common.models.EnvironmentModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GetEnvironmentsResponseJso extends JavaScriptObject {

    protected GetEnvironmentsResponseJso() {
        // empty protected constructor
    }

    public final native JsArray<EnvironmentJso> getRawEnvironments() /*-{
		return this.environments;
    }-*/;

    public final List<EnvironmentModel> getEnvironments() {

        List<EnvironmentModel> list = new ArrayList<EnvironmentModel>();

        JsArray<EnvironmentJso> environments = getRawEnvironments();
        if (null != environments) {
            for (int i = 0; i < environments.length(); i++) {
                list.add(new EnvironmentModel(environments.get(i)));
            }
        }

        return list;
    }
}
