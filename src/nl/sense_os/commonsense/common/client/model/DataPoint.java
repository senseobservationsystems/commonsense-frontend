package nl.sense_os.commonsense.common.client.model;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

public class DataPoint extends JavaScriptObject {

    protected DataPoint() {
        // empty protected constructor
    }

    public final native int getId() /*-{
		return parseInt(this.id);
    }-*/;
    
    public final native void setDate(double date)/*-{
		this.date = date;
 	}-*/;


    public final native void setId(int id)/*-{
		this.id = id;
	}-*/;


    protected final native double getRawDate() /*-{
		return this.date;
    }-*/;

    public final native String getRawValue() /*-{
		return '' + this.value;
    }-*/;
    
    public final native double getOurValue() /*-{
		return this.value;
	}-*/;

    public final Date getTimestamp() {
        return new Date(Math.round(this.getRawDate()));
    }
    
    public final double getTime() {
        return Math.round(this.getRawDate());
    }
    
    public final native void setValue(double value)  /*-{
		this.value = value;
	}-*/;

	
}
