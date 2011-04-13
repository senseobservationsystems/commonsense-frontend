package nl.sense_os.commonsense.client.json.overlays;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

public class DataPoint extends JavaScriptObject {

    protected DataPoint() {
        // empty protected constructor
    }

    /**
     * @return The sensor value, stripped of all slashes.
     */
    public final String getCleanValue() {
        return getRawValue().replaceAll("//", "");
    }

    /**
     * @return The sample time of this data point, in Unix-time seconds (floating point), formatted
     *         as String.
     * @see #getTimestamp()
     */
    public final native String getDate() /*-{
		return this.date;
    }-*/;

    /**
     * @return The ID of this data point.
     */
    public final native String getId() /*-{
		return this.id;
    }-*/;

    /**
     * @return The month of the sample time.
     * @see #getTimestamp()
     */
    public final native String getMonth() /*-{
		return this.month;
    }-*/;

    /**
     * @return The raw sensor value, including all escape characters.
     * @see #getCleanValue()
     */
    public final native String getRawValue() /*-{
		return this.value;
    }-*/;

    /**
     * @return The ID of the sensor that generated this data point.
     */
    public final native String getSensorId() /*-{
		return this.sensor_id;
    }-*/;

    /**
     * @return The time at which this data point was sampled.
     */
    public final Date getTimestamp() {
        final double decimalTime = Double.parseDouble(this.getDate());
        return new Date((long) (decimalTime * 1000));
    }

    /**
     * @return The week of the sample time.
     * @see #getTimestamp()
     */
    public final native String getWeek() /*-{
		return this.week;
    }-*/;

    /**
     * @return The year of the sample time.
     * @see #getTimestamp()
     */
    public final native String getYear() /*-{
		return this.year;
    }-*/;
}
