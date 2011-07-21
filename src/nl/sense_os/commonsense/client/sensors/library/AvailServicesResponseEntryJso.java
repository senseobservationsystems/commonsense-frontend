package nl.sense_os.commonsense.client.sensors.library;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.models.ServiceJso;
import nl.sense_os.commonsense.client.common.models.ServiceModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class AvailServicesResponseEntryJso extends JavaScriptObject {

    protected AvailServicesResponseEntryJso() {
        // empty protected constructor
    }

    public native final int getSensorId() /*-{
        return parseInt(this.sensor_id);
    }-*/;

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
