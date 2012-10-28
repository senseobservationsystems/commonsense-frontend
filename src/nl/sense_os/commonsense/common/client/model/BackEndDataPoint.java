package nl.sense_os.commonsense.common.client.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Class to represent the "raw" data points that are in the data that CommonSense back end sends us.
 */
public class BackEndDataPoint extends JavaScriptObject {

    protected BackEndDataPoint() {
        // empty protected constructor
    }

    public final native String getId() /*-{
		return this.id;
    }-*/;

    public final native int getSensorId() /*-{
		return this.sensor_id;
    }-*/;

    /**
     * @return the date (in seconds!), formatted as String by CommonSense back end
     */
    public final native String getDate() /*-{
		return '' + this.date;
    }-*/;

    public final native String getValue() /*-{
		return this.value;
    }-*/;

    public final native int getWeek() /*-{
		return this.week;
    }-*/;

    public final native int getMonth() /*-{
		return this.month;
    }-*/;

    public final native int getYear() /*-{
		return this.year;
    }-*/;

}
