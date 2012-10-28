package nl.sense_os.commonsense.main.client.visualization.component.map;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.FloatDataPoint;
import nl.sense_os.commonsense.main.client.visualization.component.map.resource.MapResources;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;

public class MapPanel extends Composite {

	private static final Logger LOG = Logger.getLogger(MapPanel.class.getName());
	private static final List<String> COLOURS = Arrays.asList("#FF7F00", "#E9967A");

	/**
	 * Main Google Maps widget
	 */
	private MapWidget mapWidget;

	/**
	 * Set of all position data, including the corresponding polylines and markers
	 */
    private Map<String, LocationData> dataset;

	private long traceStartTime = Long.MIN_VALUE;
	private long traceEndTime = Long.MAX_VALUE;
	private long animationTime;

	private boolean animationMode = true;

	public MapPanel() {

		mapWidget = new MapWidget();
		mapWidget.setWidth("100%");
		mapWidget.setUIToDefault();

		initWidget(mapWidget);
	}

	private void centerMap() {

		// find the extremes of every trace

		double newLat_sw = 90;
		double newLon_sw = 180;

		double newLat_ne = -90;
		double newLon_ne = -180;

		for (LocationData locationData : dataset.values()) {
			Polyline trace = locationData.getTrace().getPolyline();
			LatLngBounds bounds = trace.getBounds();

			LatLng sw = bounds.getSouthWest();
			double lat_sw = sw.getLatitude();
			double lon_sw = sw.getLongitude();
			if (lat_sw < newLat_sw) {
				newLat_sw = lat_sw;
			}
			if (lon_sw < newLon_sw) {
				newLon_sw = lon_sw;
			}

			LatLng ne = bounds.getNorthEast();
			double lat_ne = ne.getLatitude();
			double lon_ne = ne.getLongitude();
			if (lat_ne > newLat_ne) {
				newLat_ne = lat_ne;
			}
			if (lon_ne > newLon_ne) {
				newLon_ne = lon_ne;
			}

		}

		// make the new Bounds according to the extremes

		LatLng new_sw = LatLng.newInstance(newLat_sw, newLon_sw);
		LatLng new_ne = LatLng.newInstance(newLat_ne, newLon_ne);
		LatLngBounds newBounds = LatLngBounds.newInstance(new_sw, new_ne);

		mapWidget.setCenter(newBounds.getCenter());
		mapWidget.setZoomLevel(mapWidget.getBoundsZoomLevel(newBounds));
	}

	public void setAnimationMode(boolean enable) {
		LOG.fine((enable ? "Enable" : "Disable") + " animation mode");
		if (enable) {
			// remove start/end markers for each trace
			for (LocationData locationData : dataset.values()) {
				LocationTrace trace = locationData.getTrace();
				mapWidget.removeOverlay(trace.getStartMarker());
				mapWidget.removeOverlay(trace.getEndMarker());
				mapWidget.addOverlay(trace.getAnimationMarker());
			}
		} else {

			for (LocationData locationData : dataset.values()) {
				LocationTrace trace = locationData.getTrace();
				mapWidget.addOverlay(trace.getStartMarker());
				mapWidget.addOverlay(trace.getEndMarker());
				mapWidget.removeOverlay(trace.getAnimationMarker());
			}
		}

		animationMode = enable;
	}

	public void setAnimationTime(long timestamp) {
		animationTime = timestamp;
		updateAnimationMarkers();
	}

	public void setDisplayEnd(long timestamp) {
		traceEndTime = timestamp;
		updateTraces();
	}

	public void setDisplayStart(long timestamp) {
		traceStartTime = timestamp;
		updateTraces();
	}

    public void setLocationDataSet(Map<String, LocationData> dataset) {
		this.dataset = dataset;

		showTraces();
		centerMap();

		if (animationMode) {
			showAnimationMarkers();
		}
	}

	/**
	 * Draws a dot on the map for animation
	 */
	private void showAnimationMarkers() {
		LOG.fine("Show animation markers");

		// make an icon used to animate the trace
		ImageResource iconResource = MapResources.INSTANCE.markerBlueDot();
		Icon icon = Icon.newInstance(iconResource.getSafeUri().asString());
		icon.setIconSize(Size.newInstance(18, 18));
		icon.setIconAnchor(Point.newInstance(9, 9));
		icon.setInfoWindowAnchor(Point.newInstance(5, 1));

		MarkerOptions options = MarkerOptions.newInstance();
		options.setIcon(icon);

		// create marker for each location trace
		for (LocationData locationData : dataset.values()) {
			JsArray<FloatDataPoint> latData = locationData.getLatitudes().getData().cast();
			JsArray<FloatDataPoint> lonData = locationData.getLongitudes().getData().cast();

			LatLng markerLatLng = LatLng.newInstance(latData.get(0).getValue(), lonData.get(0)
					.getValue());
			Marker animationMarker = new Marker(markerLatLng, options);

			locationData.getTrace().setAnimationMarker(animationMarker);
			mapWidget.addOverlay(animationMarker);
		}
	}

	/**
	 * This method is called when data is first added to the map. It draws the complete trace on the
	 * map, based on the current setting of the sliders.
	 */
	private void showTraces() {
		LOG.fine("Show traces");

		int colourCount = 0;
		for (LocationData locationData : dataset.values()) {

			String traceColour = COLOURS.get(colourCount % COLOURS.size());
			colourCount++;

			// get the sensor values
			JsArray<FloatDataPoint> latValues = locationData.getLatitudes().getData().cast();
			JsArray<FloatDataPoint> lonValues = locationData.getLongitudes().getData().cast();

			LOG.finest("Number of points to draw: " + latValues.length());

			// Draw the filtered points.
			if (latValues.length() > 0) {
				LatLng[] points = new LatLng[latValues.length()];
				LocationTrace trace = new LocationTrace();

				int traceStartIndex = -1;
				int traceEndIndex = -1;
				int lastPoint = -1;
				double lat;
				double lng;

				for (int i = 0, j = 0; i < latValues.length(); i++) {

					lat = latValues.get(i).getValue();
					lng = lonValues.get(i).getValue();

                    // timestamp in ms
                    long timestamp = latValues.get(i).getTimestamp();
					// LOG.fine ("The timestamp for point " + i + " is " + timestamp);

					if (timestamp != 0) {
						// update indices
						lastPoint = j;
						traceEndIndex = i;
						if (-1 == traceStartIndex) {
							traceStartIndex = i;
						}
						// store coordinate
						LatLng coordinate = LatLng.newInstance(lat, lng);
						points[j] = coordinate;
						j++;
					}
				}

				LOG.finest("Trace start index=" + traceStartIndex + ", end index=" + traceEndIndex);

				// Add the first marker
				final MarkerOptions markerOptions = MarkerOptions.newInstance();
				Marker startMarker = new Marker(points[0], markerOptions);
				if (!animationMode) {
					mapWidget.addOverlay(startMarker);
				}

				// Add the last marker
				Marker endMarker = new Marker(points[lastPoint], markerOptions);
				if (!animationMode) {
					mapWidget.addOverlay(endMarker);
				}

				// Draw a track line
				PolylineOptions lineOptions = PolylineOptions.newInstance(false, true);
				Polyline polyline = new Polyline(points, traceColour, 5, 1, lineOptions);
				mapWidget.addOverlay(polyline);
				LOG.finest("trace vertex count is " + polyline.getVertexCount());

				// save the trace details with the location data
				trace.setStartMarker(startMarker);
				trace.setStartIndex(0);
				trace.setEndMarker(endMarker);
				trace.setEndIndex(points.length - 1);
				trace.setPolyline(polyline);
				locationData.setTrace(trace);

			} else {
				LOG.warning("No position values in selected time range");
			}
		}
	}

	private void updateAnimationMarkers() {
		LOG.finer("Update animation markers");

		for (LocationData locationData : dataset.values()) {

			LocationTrace trace = locationData.getTrace();
			Marker animationMarker = trace.getAnimationMarker();

			JsArray<FloatDataPoint> latData = locationData.getLatitudes().getData().cast();
			JsArray<FloatDataPoint> lonData = locationData.getLongitudes().getData().cast();
			LatLng newLocation = LatLng.newInstance(latData.get(0).getValue(), lonData.get(0)
					.getValue());
			for (int i = 0; i < latData.length(); i++) {
                if (latData.get(i).getTimestamp() >= animationTime) {
					newLocation = LatLng.newInstance(latData.get(i).getValue(), lonData.get(i)
							.getValue());
					break;
				}
			}
			animationMarker.setLatLng(newLocation);

			LOG.finest("Set animation marker at " + newLocation.toUrlValue());
		}
	}

	private void updateTraces() {
		LOG.fine("Update traces");

		// TODO determine which side of the time range has changed

		LOG.fine("Start time: " + traceStartTime + ", end time: " + traceEndTime);

		for (LocationData locationData : dataset.values()) {

			LocationTrace trace = locationData.getTrace();

			Polyline polyline = trace.getPolyline();
			Marker startMarker = trace.getStartMarker();
			Marker endMarker = trace.getEndMarker();
			int traceStartIndex = trace.getStartIndex();
			int traceEndIndex = trace.getEndIndex();

			if (null == polyline || false == polyline.isVisible()) {
				LOG.finest("Trace update skipped: trace is not shown yet");
				return;
			}

			// get the sensor values
			JsArray<FloatDataPoint> latData = locationData.getLatitudes().getData().cast();
			JsArray<FloatDataPoint> lonData = locationData.getLongitudes().getData().cast();

			// find the start and end indices of the trace in the sensor data array
			int newTraceStartIndex = 0;
			int newTraceEndIndex = 0;
			for (int i = 0; i < latData.length(); i++) {
                if (latData.get(i).getTimestamp() < traceStartTime) {
					newTraceStartIndex = i;
                } else if (latData.get(i).getTimestamp() > traceEndTime) {
					newTraceEndIndex = i;
					break;
				} else {
					newTraceEndIndex = i;
				}
			}

			LOG.fine("Old start: " + traceStartIndex + ", new start: " + newTraceStartIndex
					+ ", old end: " + traceEndIndex + ", new end: " + newTraceEndIndex
					+ ", total trace length: " + latData.length());

			// add vertices at START of trace if newTraceStart < traceStartIndex
			if (newTraceStartIndex < traceStartIndex) {
				double lat;
				double lon;
				LOG.fine("Add " + (traceStartIndex - newTraceStartIndex) + " vertices at start");
				for (int i = traceStartIndex - 1; i >= newTraceStartIndex; i--) {
					lat = latData.get(i).getValue();
					lon = lonData.get(i).getValue();
					polyline.insertVertex(0, LatLng.newInstance(lat, lon));
				}
			}

			// delete vertices at START of trace if newTraceStart > traceStartIndex
			if (newTraceStartIndex > traceStartIndex) {
				LOG.fine("Delete " + (newTraceStartIndex - traceStartIndex) + " vertices at start");
				for (int i = traceStartIndex; i < newTraceStartIndex; i++) {
					polyline.deleteVertex(0);
				}
			}

			// update start marker
			double startLat = latData.get(newTraceStartIndex).getValue();
			double startLon = lonData.get(newTraceStartIndex).getValue();
			LatLng startCoordinate = LatLng.newInstance(startLat, startLon);
			startMarker.setLatLng(startCoordinate);

			// add vertices at END of trace if newTraceEnd > traceEndIndex
			if (newTraceEndIndex > traceEndIndex) {
				LOG.fine("Add " + (newTraceEndIndex - traceEndIndex) + " vertices at end");
				double lat;
				double lon;
				int vertexCount = polyline.getVertexCount();
				for (int i = traceEndIndex + 1; i <= newTraceEndIndex; i++) {
					lat = latData.get(i).getValue();
					lon = lonData.get(i).getValue();
					polyline.insertVertex(vertexCount, LatLng.newInstance(lat, lon));
					vertexCount++;
				}
			}

			// delete vertices at END of trace if newTraceEnd < traceEndIndex
			if (newTraceEndIndex < traceEndIndex) {
				LOG.fine("Delete " + (traceEndIndex - newTraceEndIndex) + " vertices at end");
				int currentCount = polyline.getVertexCount();
				for (int i = traceEndIndex; i > newTraceEndIndex; i--) {
					polyline.deleteVertex(currentCount - 1);
					currentCount--;
				}
			}

			// update end marker
			double endLat = latData.get(newTraceEndIndex).getValue();
			double endLon = lonData.get(newTraceEndIndex).getValue();
			LatLng endCoordinate = LatLng.newInstance(endLat, endLon);
			endMarker.setLatLng(endCoordinate);

			// update trace indexes
			trace.setStartIndex(newTraceStartIndex);
			trace.setEndIndex(newTraceEndIndex);
		}
	}
}
