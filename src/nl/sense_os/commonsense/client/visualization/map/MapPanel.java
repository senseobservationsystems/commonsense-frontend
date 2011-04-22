package nl.sense_os.commonsense.client.visualization.map;

import java.util.List;

import nl.sense_os.commonsense.client.data.DataEvents;
import nl.sense_os.commonsense.client.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.json.overlays.FloatDataPoint;
import nl.sense_os.commonsense.client.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizPanel;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;

public class MapPanel extends LayoutContainer implements VizPanel {

    private static final String TAG = "MapPanel";
    private MapWidget map;
    private DateSlider startSlider;
    private DateSlider endSlider;
    private Timeseries latTimeseries;
    private Timeseries lonTimeseries;
    private Marker startMarker;
    private Marker endMarker;
    private Polyline trace;
    private int traceStartIndex;
    private int traceEndIndex;

    public MapPanel() {
        this.setLayout(new BorderLayout());
        this.setStyleAttribute("background", "rgba(0, 0, 0, 0)");
        this.setId("viz-map");

        initSliders();
        initMapWidget();
    }

    public MapPanel(List<SensorModel> sensors, long startTime, long endTime) {
        this();

        AppEvent dataRequest = new AppEvent(DataEvents.DataRequest);
        dataRequest.setData("sensors", sensors);
        dataRequest.setData("startTime", startTime);
        dataRequest.setData("endTime", endTime);
        dataRequest.setData("vizPanel", this);
        Dispatcher.forwardEvent(dataRequest);
    }

    @Override
    public void addData(JsArray<Timeseries> data) {

        // sort lat/lon data
        Timeseries ts;
        for (int i = 0; i < data.length(); i++) {
            ts = data.get(i);
            if (ts.getLabel().endsWith("latitude")) {
                this.latTimeseries = ts;
            } else if (ts.getLabel().endsWith("longitude")) {
                this.lonTimeseries = ts;
            }
        }
        visualize();
    }

    /**
     * Calculates the slider parameters for a set of sensor values.
     * 
     * @param data
     *            JsonValueModels with positional sensor data over time
     */
    private void calcSliderRange() {

        JsArray<DataPoint> values = this.latTimeseries.getData();
        DataPoint v = values.get(0);
        int min = (int) (v.getTimestamp().getTime() / 1000l);
        v = values.get(values.length() - 1);
        int max = (int) (v.getTimestamp().getTime() / 1000l);

        int interval = 1;
        if (max - min < 60 * 60) {
            interval = 60 * 5; // 5 minutes
        } else if (max - min < 60 * 60 * 24) {
            interval = 60 * 60; // 1 hour
        } else {
            interval = 60 * 60 * 4; // 4 hours
        }

        startSlider.setMinValue(min);
        startSlider.setMaxValue(max);
        startSlider.setIncrement(interval);
        startSlider.disableEvents(true);
        startSlider.setValue(min);
        startSlider.enableEvents(true);

        endSlider.setMinValue(min);
        endSlider.setMaxValue(max);
        endSlider.setIncrement(interval);
        endSlider.disableEvents(true);
        endSlider.setValue(max);
        endSlider.enableEvents(true);
    }

    private void centerMap() {
        final LatLngBounds bounds = this.trace.getBounds();
        this.map.setCenter(bounds.getCenter());
        this.map.setZoomLevel(this.map.getBoundsZoomLevel(bounds));
    }

    /**
     * This method is called when data is first added to the map. It draws the complete trace on the
     * map, based on the current setting of the sliders.
     */
    private void drawTrace() {

        // clean the map
        this.map.clearOverlays();
        this.trace = null;

        // get the time window for the trace from the sliders
        int minTime = startSlider.getValue();
        int maxTime = endSlider.getValue();

        // get the sensor values
        JsArray<DataPoint> latValues = this.latTimeseries.getData().cast();
        JsArray<DataPoint> lonValues = this.lonTimeseries.getData().cast();

        Log.d(TAG, "Number of points: " + latValues.length());

        // Draw the filtered points.
        if (latValues.length() > 0 && maxTime > minTime) {
            LatLng[] points = new LatLng[latValues.length()];

            this.traceStartIndex = -1;
            this.traceEndIndex = -1;
            int lastPoint = -1;
            FloatDataPoint latitude;
            FloatDataPoint longitude;
            for (int i = 0, j = 0; i < latValues.length(); i++) {
                latitude = latValues.get(i).cast();
                longitude = lonValues.get(i).cast();

                // timestamp in secs
                long timestamp = latitude.getTimestamp().getTime() / 1000;

                if (timestamp > minTime && timestamp < maxTime) {
                    // update indices
                    lastPoint = j;
                    traceEndIndex = i;
                    if (-1 == this.traceStartIndex) {
                        traceStartIndex = i;
                    }
                    // store coordinate
                    Log.d(TAG, "get value...");
                    LatLng coordinate = LatLng.newInstance(latitude.getValue(),
                            longitude.getValue());
                    points[j++] = coordinate;
                    Log.d(TAG, "done.");
                }
            }

            // Add the first marker
            final MarkerOptions markerOptions = MarkerOptions.newInstance();
            this.startMarker = new Marker(points[0], markerOptions);
            this.map.addOverlay(startMarker);

            // Add the last marker
            this.endMarker = new Marker(points[lastPoint], markerOptions);
            this.map.addOverlay(endMarker);

            // Draw a track line
            PolylineOptions lineOptions = PolylineOptions.newInstance(false, true);
            this.trace = new Polyline(points, "#FF7F00", 5, 1, lineOptions);
            this.map.addOverlay(this.trace);
        } else {
            Log.w(TAG, "No position values in selected time range");
        }
    }

    /**
     * Initializes a Google map.
     */
    private void initMapWidget() {

        this.map = new MapWidget();
        this.map.setWidth("100%");

        // Add some controls for the zoom level
        this.map.setUIToDefault();

        // Add the map to the layout
        BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
        layoutData.setMargins(new Margins(5));
        this.add(this.map, layoutData);
    }

    /**
     * Create a set of sliders on the bottom, to filter the points to draw according to a time
     * specified with the sliders.
     */
    private void initSliders() {

        FormPanel slidersForm = new FormPanel();
        slidersForm.setHeaderVisible(false);
        slidersForm.setBorders(false);
        slidersForm.setBodyBorder(false);
        slidersForm.setPadding(0);

        Listener<SliderEvent> slideListener = new Listener<SliderEvent>() {

            @Override
            public void handleEvent(SliderEvent be) {
                updateTrace();
            }
        };

        // create start time slider
        this.startSlider = new DateSlider();
        this.startSlider.setMessage("{0}");
        this.startSlider.setId("viz-map-startSlider");
        this.startSlider.addListener(Events.Change, slideListener);

        SliderField startField = new SliderField(this.startSlider);
        startField.setFieldLabel("Trace start");

        this.endSlider = new DateSlider();
        this.endSlider.setMessage("{0}");
        this.endSlider.setValue(this.endSlider.getMaxValue());
        this.endSlider.setId("viz-map-endSlider");
        this.endSlider.addListener(Events.Change, slideListener);

        SliderField endField = new SliderField(this.endSlider);
        endField.setFieldLabel("Trace end");

        slidersForm.add(startField, new FormData("-5"));
        slidersForm.add(endField, new FormData("-5"));

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 50);
        data.setMargins(new Margins(0, 5, 5, 5));
        this.add(slidersForm, data);
    }

    private void updateTrace() {

        // Log.d(TAG, "updateTrace ");

        if (null == trace || false == trace.isVisible()) {
            Log.d(TAG, "updateTrace skipped");
            return;
        }

        // get the sensor values
        JsArray<FloatDataPoint> latValues = this.latTimeseries.getData().cast();
        JsArray<FloatDataPoint> lonValues = this.lonTimeseries.getData().cast();

        int minTime = startSlider.getValue();
        int maxTime = endSlider.getValue();

        // find the start end end indices of the trace in the sensor data array
        int newTraceStartIndex = -1, newTraceEndIndex = -1;
        long timestamp;
        for (int i = 0; i < latValues.length(); i++) {
            // get timestamp
            DataPoint value = latValues.get(i);
            timestamp = value.getTimestamp().getTime() / 1000;

            if (timestamp > minTime && newTraceStartIndex == -1) {
                // this is the first index with start of visible range
                newTraceStartIndex = i;
            }
            if (timestamp > maxTime && newTraceEndIndex == -1) {
                // this is the first index after the end of visible range
                newTraceEndIndex = i - 1;
                break;
            }
        }

        // Log.d(TAG, "old start: " + traceStartIndex + ", old end: " + traceEndIndex);
        // Log.d(TAG, "new start: " + newTraceStartIndex + ", new end: " + newTraceEndIndex);

        // change start of trace
        if (newTraceStartIndex != -1 && newTraceEndIndex > newTraceStartIndex) {
            // add vertices at START of trace if newTraceStart < traceStartIndex
            if (newTraceStartIndex < traceStartIndex) {
                Log.d(TAG, "Add " + (traceStartIndex - newTraceStartIndex) + " vertices at start");
                FloatDataPoint lat;
                FloatDataPoint lon;
                for (int i = this.traceStartIndex - 1; i >= newTraceStartIndex; i--) {
                    lat = latValues.get(i).cast();
                    lon = lonValues.get(i).cast();
                    this.trace.insertVertex(0, LatLng.newInstance(lat.getValue(), lon.getValue()));
                }
            }

            // delete vertices at START of trace if newTraceStart > traceStartIndex
            if (newTraceStartIndex > traceStartIndex) {
                Log.d(TAG, "Delete " + (newTraceStartIndex - traceStartIndex)
                        + " vertices at start");
                for (int i = this.traceStartIndex; i < newTraceStartIndex; i++) {
                    this.trace.deleteVertex(0);
                }
            }

            // update end marker
            FloatDataPoint startLat = latValues.get(newTraceStartIndex).cast();
            FloatDataPoint startLon = lonValues.get(newTraceStartIndex).cast();
            LatLng coordinate = LatLng.newInstance(startLat.getValue(), startLon.getValue());
            this.startMarker.setLatLng(coordinate);

        } else {
            newTraceStartIndex = this.traceStartIndex;
        }

        // change end of trace
        if (newTraceEndIndex != -1 && newTraceEndIndex > newTraceStartIndex) {
            // add vertices at END of trace if newTraceEnd > traceEndIndex
            if (newTraceEndIndex > traceEndIndex) {
                Log.d(TAG, "Add " + (newTraceEndIndex - traceEndIndex) + " vertices at end");
                FloatDataPoint lat;
                FloatDataPoint lon;
                for (int i = this.traceEndIndex + 1; i <= newTraceEndIndex; i++) {
                    lat = latValues.get(i).cast();
                    lon = lonValues.get(i).cast();
                    this.trace.insertVertex(this.trace.getVertexCount(),
                            LatLng.newInstance(lat.getValue(), lon.getValue()));
                }
            }

            // delete vertices at END of trace if newTraceEnd < traceEndIndex
            if (newTraceEndIndex < traceEndIndex) {
                Log.d(TAG, "Delete " + (traceEndIndex - newTraceEndIndex) + " vertices at end");
                for (int i = this.traceEndIndex; i > newTraceEndIndex; i--) {
                    this.trace.deleteVertex(this.trace.getVertexCount() - 1);
                }
            }

            // update end marker
            FloatDataPoint endLat = latValues.get(newTraceEndIndex).cast();
            FloatDataPoint endLon = lonValues.get(newTraceEndIndex).cast();
            LatLng coordinate = LatLng.newInstance(endLat.getValue(), endLon.getValue());
            this.endMarker.setLatLng(coordinate);

        } else {
            newTraceEndIndex = this.traceEndIndex;
        }

        // update trace indexes
        this.traceStartIndex = newTraceStartIndex;
        this.traceEndIndex = newTraceEndIndex;
    }

    private void visualize() {
        if (this.latTimeseries != null && this.lonTimeseries != null) {
            calcSliderRange();
            drawTrace();
            centerMap();
        }
    }
}
