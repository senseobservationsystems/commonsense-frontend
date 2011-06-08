package nl.sense_os.commonsense.client.viz.data.timeseries;

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

    public final native int getId() /*-{
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

    public final native void append(Timeseries toAppend) /*-{

        // if there is no data yet, the solution is easy
        if (undefined == this.data || this.data.length == 0) {
            this.data = toAppend.data;
            this.start = toAppend.start;
            this.end = toAppend.end;
            return;
        }

        // else, append the new data
        for ( var i = 0; i < toAppend.data.length; i++) {
            this.data.push(toAppend.data[i]);
        }

        this.end = toAppend.end;

        console.log(toAppend.data.length + " points appended. Total length: " + this.data.length);

    }-*/;
}
