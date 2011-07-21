package nl.sense_os.commonsense.client.sensors.library;

import java.util.List;

import nl.sense_os.commonsense.client.common.models.ServiceModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class AvailServicesResponseJso extends JavaScriptObject {

    protected AvailServicesResponseJso() {
        // empty protected constructor
    }

    public final native JsArray<AvailServicesResponseEntryJso> getEntries() /*-{
        return this.available_services;
    }-*/;

    public final native int getTotal() /*-{
        return this.total;
    }-*/;

    public final int getSensorId(int index) {
        JsArray<AvailServicesResponseEntryJso> entries = getEntries();
        return entries.get(index).getSensorId();
    }

    public final List<ServiceModel> getServices(int index) {
        JsArray<AvailServicesResponseEntryJso> entries = getEntries();
        return entries.get(index).getServices();
    }
}
