package nl.sense_os.commonsense.client.json.overlays;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

public class JsoDataPoint extends JavaScriptObject {

    protected JsoDataPoint() {
        // empty protected constructor
    }

    public final String getCleanValue() {
        return getRawValue().replaceAll("//", "");
    }

    public final native String getDate() /*-{
		return this.date;
    }-*/;

    public final native String getId() /*-{
		return this.id;
    }-*/;

    public final native String getMonth() /*-{
		return this.month;
    }-*/;

    public final native String getRawValue() /*-{
		return this.value;
    }-*/;

    public final native String getSensorId() /*-{
		return this.sensor_id;
    }-*/;

    public final Date getTimestamp() {
        final double decimalTime = Double.parseDouble(this.getDate());
        return new Date((long) (decimalTime * 1000));
    }

    public final native String getWeek() /*-{
		return this.week;
    }-*/;

    public final native String getYear() /*-{
		return this.year;
    }-*/;
}
