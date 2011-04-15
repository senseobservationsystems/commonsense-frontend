package nl.sense_os.commonsense.client.json.overlays;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class SensorDataResponse extends JavaScriptObject {

    protected SensorDataResponse() {
        // empty protected constructor
    }

    public final native JsArray<DataPoint> getData() /*-{
		return this.data;
    }-*/;

    public final native int getTotal() /*-{
		return this.total;
    }-*/;
}
