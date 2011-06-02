package nl.sense_os.commonsense.client.viz.data.timeseries;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

public class DataPoint extends JavaScriptObject {

    protected DataPoint() {
        // empty protected constructor
    }

    protected final native double getRawDate() /*-{
        return this.date;
    }-*/;

    public final native String getRawValue() /*-{
        return '' + this.value;
    }-*/;

    public final Date getTimestamp() {
        return new Date(Math.round(this.getRawDate()));
    }
}
