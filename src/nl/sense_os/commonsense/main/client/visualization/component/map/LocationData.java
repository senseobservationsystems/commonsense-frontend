package nl.sense_os.commonsense.main.client.visualization.component.map;

import nl.sense_os.commonsense.common.client.model.Timeseries;

public class LocationData {

    private Timeseries latitudes;
    private Timeseries longitudes;
    private LocationTrace trace;

    public LocationData(Timeseries latitude, Timeseries longitude) {
        this.latitudes = latitude;
        this.longitudes = longitude;
    }

    /**
     * @return the latitude data
     */
    public Timeseries getLatitudes() {
        return latitudes;
    }

    /**
     * @return the longitude data
     */
    public Timeseries getLongitudes() {
        return longitudes;
    }

    public LocationTrace getTrace() {
        return trace;
    }

    /**
     * @param latitudes
     *            the latitude to set
     */
    public void setLatitudes(Timeseries latitudes) {
        this.latitudes = latitudes;
    }

    /**
     * @param longitudes
     *            the longitude to set
     */
    public void setLongitudes(Timeseries longitudes) {
        this.longitudes = longitudes;
    }

    public void setTrace(LocationTrace trace) {
        this.trace = trace;
    }
}
