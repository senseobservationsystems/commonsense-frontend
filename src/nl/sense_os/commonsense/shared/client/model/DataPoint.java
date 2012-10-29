package nl.sense_os.commonsense.shared.client.model;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

public class DataPoint extends JavaScriptObject {

    protected DataPoint() {
        // empty protected constructor
    }

    /**
     * @return The timestamp, as Date object
     */
    public final Date getDate() {
        return new Date(getTimestamp());
    }

    /**
     * @return The ID of this data point
     */
    public final native String getId() /*-{
		return this.id;
    }-*/;

    /**
     * @return The timestamp (in milliseconds!)
     */
    protected final native double getRawTimestamp() /*-{
		return this.date;
    }-*/;

    /**
     * @return The 'raw' (String) value
     */
    public final native String getRawValue() /*-{
		return '' + this.value;
    }-*/;

    /**
     * @return The timestamp (in milliseconds!)
     */
    public final long getTimestamp() {
        return Math.round(getRawTimestamp());
    }

    public final native void setId(int id)/*-{
		this.id = id;
    }-*/;

    public final native void setTimestamp(double date)/*-{
		this.date = date;
    }-*/;

    public final native void setValue(String value) /*-{
		this.value = value;
    }-*/;
}