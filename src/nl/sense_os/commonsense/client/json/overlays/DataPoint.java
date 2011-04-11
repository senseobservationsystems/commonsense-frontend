package nl.sense_os.commonsense.client.json.overlays;

import com.google.gwt.core.client.JavaScriptObject;

public class DataPoint extends JavaScriptObject {

    protected DataPoint() {
        // empty protected constructor
    }

    public final native String getId() /*-{
		return this.id;
    }-*/;

    public final native String getSensorId() /*-{
		return this.sensor_id;
    }-*/;

    public final native String getValue() /*-{
		return this.value;
    }-*/;

    public final native String getDate() /*-{
		return this.date;
    }-*/;

    public final native String getWeek() /*-{
		return this.week;
    }-*/;

    public final native String getMonth() /*-{
		return this.month;
    }-*/;

    public final native String getYear() /*-{
		return this.year;
    }-*/;
}
