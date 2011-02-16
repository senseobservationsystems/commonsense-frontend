package nl.sense_os.commonsense.client.visualization.map.components;

import java.util.Date;
import java.util.Map;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class MapPanel extends ContentPanel {

	private static final String TAG = "MapPanel";
	private MapWidget map;
	private Slider slider;
	private SensorValueModel[] sensorData;
	// 24 hs -> 86400 secs
	private int timeGranularity = 86400;
	//private static final int timeGranularity = 720;	// in seconds
	private DockLayoutPanel dock;
	
	
	public MapPanel() {
		// Panel settings. 
		setLayout(new FitLayout());
		setHeaderVisible(false);
		setBodyBorder(false);
		setScrollMode(Scroll.NONE);
		setId("viz-map");

		dock = new DockLayoutPanel(Unit.PX);
		
		createMap();		
		createSlider();
		
		// Add the dock to the panel.
		this.add(dock);		
	}

	/**
	 * Create a google map and add it to the north of the dock.
	 */
	private void createMap() {
		// Create the map.
		map = new MapWidget();
		map.setSize("100%", "100%");

		// Add some controls for the zoom level
		map.addControl(new LargeMapControl());
		map.setScrollWheelZoomEnabled(true);

		// Add the map to the dock.
		dock.addNorth(map, 500);
	}

	/**
	 * Create a slider on the bottom, to filter the points to draw according to
	 * a time specified with the slider.
	 */
	private void createSlider() {
		// Slider added below the map.		
		slider = new Slider();
		slider.setWidth(500);
		slider.setHeight(50);
		slider.setMinValue(0);
		slider.setMaxValue(100);
		slider.setIncrement(1);
		slider.setMessage("{0} days ago"); 
		slider.setId("viz-map-slider");
		
		// Add the map to the dock.		
		dock.addSouth(slider, 30);

		// Listener to filter the points to draw on the map.
		slider.addListener(Events.Change, new Listener<SliderEvent>() {
			@Override
			public void handleEvent(SliderEvent be) {
				int newValue = be.getNewValue();
				updateMap(newValue);
			}
		});		
	}
	
	/**
	 * This method is called when it used the slider below the map.
	 * It filters the points to draw a line depending on the time.
	 * 
	 * @param time
	 */
	private void updateMap(int time) {
		// Clean the map.
		map.clearOverlays();

		// Draw the filtered points.
		if (sensorData.length > 0) {
			LatLng[] points = new LatLng[sensorData.length];

			long minTimeFilter = 0;

			// The last drawn point's timestamp is used to set the min time filter.
			if (time != 0) {
				JsonValueModel v = (JsonValueModel) sensorData[sensorData.length - 1];
				Date t = v.getTimestamp();
				
				// time filter in secs
				minTimeFilter = t.getTime() / 1000;
				minTimeFilter -= (timeGranularity * time);				
			}

			int lastPoint = 0;

			// It will be drawn all the points greater than the minTimeFilter.
			for (int i = 0, j = 0; i < sensorData.length; i++) {
				JsonValueModel value = (JsonValueModel) sensorData[i];
				Map<String, Object> fields = value.getFields();

				double latitude = (Double) fields.get("latitude");
				double longitude = (Double) fields.get("longitude");
				
				// timestamp in secs
				long timestamp = value.getTimestamp().getTime() / 1000;
				
				//Log.d(TAG, "minTimeFilter: "+minTimeFilter);
				//Log.d(TAG, "timestamp: "+timestamp);
				
				if (timestamp > minTimeFilter) {
					lastPoint = j;
					points[j++] = LatLng.newInstance(latitude, longitude);
				}
			}

			// Add the first marker
			Marker startMarker = new Marker(points[0]);
			map.addOverlay(startMarker);

			// Add the last marker
			Marker endMarker = new Marker(points[lastPoint]);
			map.addOverlay(endMarker);

			// Draw a track line
			Polyline trace = new Polyline(points);
			map.addOverlay(trace);

			// Center the map
			map.setCenter(endMarker.getLatLng());
			map.setZoomLevel(13);
		}		
	}

	/**
	 * Display the markers and draw a trace line on the map.
	 * 
	 * @param sensor
	 * @param data
	 */
	public void addData(TreeModel sensor, SensorValueModel[] data) {
		// Store the sensor data to be used from other methods.
		sensorData = data;

		// Set the range of values for the slider according to the 
		// difference between the first marker's time and the last one.
		JsonValueModel v = (JsonValueModel) data[0];	
		int min = (int) v.getTimestamp().getTime() / 1000 / 60 / 24; // to days
		v = (JsonValueModel) data[data.length - 1];
		int max = (int) v.getTimestamp().getTime() / 1000 / 60 / 24; // to days
		int days = max - min;
		Log.d(TAG, "days: " + days);		
		slider.setMaxValue(days);

		// If the difference between the 1st point and the last one is greater
		// than 31 days, the time range is changed to an hour (3600 secs).
		if (days < 31) {			
			timeGranularity = 3600;
			slider.setMessage("{0} hours ago");
			slider.setMaxValue(days * 24);
		}
		
		// Draw markers and a trace line on the map.
		if (data.length > 0) {
			LatLng[] points = new LatLng[data.length];
			
			// Store the points in an array.
			for (int i = 0; i < data.length; i++) {
				JsonValueModel value = (JsonValueModel) data[i];
				Map<String, Object> fields = value.getFields();

				double latitude = (Double) fields.get("latitude");
				double longitude = (Double) fields.get("longitude");

				// timestamp in secs
				long timestamp = value.getTimestamp().getTime() / 1000;				
				//Log.d(TAG, "timestamp: " + timestamp);				
				
				points[i] = LatLng.newInstance(latitude, longitude);
			}

			// Add the first marker
			Marker startMarker = new Marker(points[0]);
			map.addOverlay(startMarker);

			// Add the last marker
			Marker endMarker = new Marker(points[points.length - 1]);
			map.addOverlay(endMarker);
			
			// Draw a trace line through the points.
			Polyline trace = new Polyline(points);
			map.addOverlay(trace);

			// Center based on both markers
			/*
			 * LatLngBounds bounds = map.getBounds();
			 * bounds.extend(startMarker.getLatLng());
			 * bounds.extend(endMarker.getLatLng()); 
			 * 
			 * int zoomLevel = map.getBoundsZoomLevel(bounds); 
			 * Log.d(TAG, "zoom level: " + zoomLevel);
			 */

			// Adjust the zoom level according to the bounds.			
			/* 
			 * if (zoomLevel < 9)
			 * 	map.setZoomLevel(zoomLevel); 
			 * else
			 * 	map.setZoomLevel(12);
			 *  
			 * map.setCenter(bounds.getCenter(),
			 * map.getBoundsZoomLevel(bounds));
			 */

			map.setCenter(endMarker.getLatLng());
			map.setZoomLevel(13);

		} else {
			Log.d(TAG, "no data.");
		}
	}

	/**
	 * Should display something to show the user that we are done loading data.
	 */
	public void finishLoading() {
		Log.d(TAG, "Finished loading!");
	}

}
