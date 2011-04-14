package nl.sense_os.commonsense.client.json.overlays;

import java.util.Date;

public interface AbstractDataPoint {

    /**
     * @return The sensor value, stripped of any slashes.
     */
    public String getCleanValue();

    /**
     * @return The sample time of this data point, in Unix-time seconds (floating point), formatted
     *         as String.
     * @see #getTimestamp()
     */
    public String getDate();

    /**
     * @return The ID of this data point.
     */
    public String getId();

    /**
     * @return The month of the sample time.
     * @see #getTimestamp()
     */
    public String getMonth();

    /**
     * @return The raw sensor value, including any escape characters.
     * @see #getCleanValue()
     */
    public String getRawValue();

    /**
     * @return The ID of the sensor that generated this data point.
     */
    public String getSensorId();

    /**
     * @return The time at which this data point was sampled.
     */
    public Date getTimestamp();

    /**
     * @return The week of the sample time.
     * @see #getTimestamp()
     */
    public String getWeek();

    /**
     * @return The year of the sample time.
     * @see #getTimestamp()
     */
    public String getYear();
}
