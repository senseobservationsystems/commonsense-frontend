package nl.sense_os.commonsense.main.client.visualization.component.map;

import nl.sense_os.commonsense.common.client.model.Timeseries;

public class LocationData {

	private Timeseries latitude;
	private Timeseries longitude;
	private LocationTrace trace;

	public LocationData(Timeseries latitude, Timeseries longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @return the latitude
	 */
	public Timeseries getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public Timeseries getLongitude() {
		return longitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(Timeseries latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(Timeseries longitude) {
		this.longitude = longitude;
	}

	public LocationTrace getTrace() {
		return trace;
	}

	public void setTrace(LocationTrace trace) {
		this.trace = trace;
	}
}
