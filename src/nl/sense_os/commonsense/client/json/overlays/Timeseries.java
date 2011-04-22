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

    public final long getEnd() {
        return Math.round(getRawEnd());
    }

    public final native String getId() /*-{
        return this.id;
    }-*/;

    public final native String getLabel() /*-{
        return this.label;
    }-*/;

    private final native double getRawEnd() /*-{
        return this.end;
    }-*/;

    private final native double getRawStart() /*-{
        return this.start;
    }-*/;

    public final long getStart() {
        return Math.round(getRawStart());
    }

    public final native String getType() /*-{
        return this.type;
    }-*/;
}
