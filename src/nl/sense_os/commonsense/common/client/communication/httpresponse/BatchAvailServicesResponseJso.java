package nl.sense_os.commonsense.common.client.communication.httpresponse;

import java.util.List;

import nl.sense_os.commonsense.common.client.model.ServiceModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class BatchAvailServicesResponseJso extends JavaScriptObject {

    protected BatchAvailServicesResponseJso() {
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
