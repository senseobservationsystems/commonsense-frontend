package nl.sense_os.commonsense.main.client.visualization.component.map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polyline;

public class LocationTrace {

	private Polyline polyline;
	private Marker startMarker;
	private Marker endMarker;
	private Marker animationMarker;
	private int startIndex;
	private int endIndex;

	public LocationTrace() {
		this.polyline = new Polyline(new LatLng[] { LatLng.newInstance(90, 0) });
		this.startMarker = new Marker(LatLng.newInstance(90, 0));
		this.endMarker = new Marker(LatLng.newInstance(90, 0));
		this.animationMarker = new Marker(LatLng.newInstance(90, 0));
		this.startIndex = 0;
		this.endIndex = 0;
	}

	/**
	 * @return the endIndex
	 */
	public int getEndIndex() {
		return endIndex;
	}

	/**
	 * @param endIndex
	 *            the endIndex to set
	 */
	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	/**
	 * @param startIndex
	 *            the startIndex to set
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * @return the trace
	 */
	public Polyline getPolyline() {
		return polyline;
	}

	/**
	 * @param trace
	 *            the trace to set
	 */
	public void setPolyline(Polyline trace) {
		this.polyline = trace;
	}

	/**
	 * @return the traceStartMarker
	 */
	public Marker getStartMarker() {
		return startMarker;
	}

	/**
	 * @param startMarker
	 *            the traceStartMarker to set
	 */
	public void setStartMarker(Marker startMarker) {
		this.startMarker = startMarker;
	}

	/**
	 * @return the traceEndMarker
	 */
	public Marker getEndMarker() {
		return endMarker;
	}

	/**
	 * @param endMarker
	 *            the traceEndMarker to set
	 */
	public void setEndMarker(Marker endMarker) {
		this.endMarker = endMarker;
	}

	public Marker getAnimationMarker() {
		return animationMarker;
	}

	public void setAnimationMarker(Marker animationMarker) {
		this.animationMarker = animationMarker;
	}

	public int getStartIndex() {
		return startIndex;
	}
}
