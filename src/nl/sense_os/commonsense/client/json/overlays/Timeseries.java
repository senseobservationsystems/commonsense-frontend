package nl.sense_os.commonsense.client.json.overlays;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class Timeseries extends JavaScriptObject {

    protected Timeseries() {
        // empty protected constructor
    }

    public final native JsArray<DataPoint> getData() /*-{
		return this.data;
    }-*/;

    public final native String getId() /*-{
		return this.id;
    }-*/;

    public final native String getLabel() /*-{
		return this.label;
    }-*/;

    public final native String getType() /*-{
		return this.type;
    }-*/;
}
