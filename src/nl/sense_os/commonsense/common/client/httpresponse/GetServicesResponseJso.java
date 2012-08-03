package nl.sense_os.commonsense.common.client.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.common.client.model.ServiceJso;
import nl.sense_os.commonsense.common.client.model.ServiceModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GetServicesResponseJso extends JavaScriptObject {

    protected GetServicesResponseJso() {
        // empty protected constructor
    }

    public final native JsArray<ServiceJso> getRawServices() /*-{
		return this.services;
    }-*/;

    public final List<ServiceModel> getServices() {
        List<ServiceModel> list = new ArrayList<ServiceModel>();

        JsArray<ServiceJso> services = getRawServices();
        if (null != services) {
            for (int i = 0; i < services.length(); i++) {
                list.add(new ServiceModel(services.get(i)));
            }
        }
        return list;
    }
}
