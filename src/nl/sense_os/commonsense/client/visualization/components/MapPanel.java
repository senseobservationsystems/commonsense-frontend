package nl.sense_os.commonsense.client.visualization.components;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.sense_os.commonsense.client.common.DateSlider;
import nl.sense_os.commonsense.client.data.DataEvents;
import nl.sense_os.commonsense.client.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.json.overlays.FloatDataPoint;
import nl.sense_os.commonsense.client.json.overlays.JsonDataPoint;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.JavaScriptObject;
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
    private Slider startSlider;
    private Slider endSlider;
    private DataPoint[] sensorData = new DataPoint[]{};
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
    public void addData(Map<SensorModel, DataPoint[]> data) {

        for (Entry<SensorModel, DataPoint[]> entry : data.entrySet()) {
            addData(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addData(SensorModel sensor, DataPoint[] values) {
        // Store the sensor data to be used from other methods.
        this.sensorData = values;

        calcSliderRange(values);
        drawTrace();
        centerMap();
    }

    /**
     * Calculates the slider parameters for a set of sensor values.
     * 
     * @param data
     *            JsonValueModels with positional sensor data over time
     */
    private void calcSliderRange(DataPoint[] data) {

        JsonDataPoint v = (JsonDataPoint) data[0];
        int min = (int) (v.getTimestamp().getTime() / 1000);
        v = (JsonDataPoint) data[data.length - 1];
        int max = (int) (v.getTimestamp().getTime() / 1000);

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
        startSlider.setValue(min);

        endSlider.setMinValue(min);
        endSlider.setMaxValue(max);
        endSlider.setIncrement(interval);
        endSlider.setValue(max);
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

        // Draw the filtered points.
        if (sensorData.length > 0 && maxTime > minTime) {
            LatLng[] points = new LatLng[sensorData.length];

            this.traceStartIndex = -1;
            this.traceEndIndex = -1;
            int lastPoint = -1;

            // find the start index of the trace
            JsonDataPoint value = null;
            Map<String, DataPoint> fields = null;
            for (int i = 0, j = 0; i < sensorData.length; i++) {
                value = (JsonDataPoint) sensorData[i];
                fields = value.getFields();

                FloatDataPoint latitude = (FloatDataPoint) fields.get("latitude");
                FloatDataPoint longitude = (FloatDataPoint) fields.get("longitude");

                // timestamp in secs
                long timestamp = value.getTimestamp().getTime() / 1000;

                if (timestamp > minTime && timestamp < maxTime) {
                    // update indices
                    lastPoint = j;
                    traceEndIndex = i;
                    if (-1 == this.traceStartIndex) {
                        traceStartIndex = i;
                    }
                    // store coordinate
                    LatLng coordinate = LatLng.newInstance(latitude.getFloatValue(),
                            longitude.getFloatValue());
                    points[j++] = coordinate;
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

        int minTime = startSlider.getValue();
        int maxTime = endSlider.getValue();

        // find the start end end indices of the trace in the sensor data array
        int newTraceStartIndex = -1, newTraceEndIndex = -1;
        JsonDataPoint value = null;
        long timestamp;
        for (int i = 0; i < sensorData.length; i++) {
            // get timestamp
            value = (JsonDataPoint) sensorData[i];
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
                Map<String, DataPoint> fields = null;
                for (int i = this.traceStartIndex - 1; i >= newTraceStartIndex; i--) {
                    value = (JsonDataPoint) this.sensorData[i];
                    fields = value.getFields();
                    FloatDataPoint lat = (FloatDataPoint) fields.get("latitude");
                    FloatDataPoint lon = (FloatDataPoint) fields.get("longitude");
                    LatLng coordinate = LatLng
                            .newInstance(lat.getFloatValue(), lon.getFloatValue());

                    this.trace.insertVertex(0, coordinate);
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
            JsonDataPoint startValue = (JsonDataPoint) this.sensorData[newTraceStartIndex];
            Map<String, DataPoint> fields = startValue.getFields();
            FloatDataPoint lat = (FloatDataPoint) fields.get("latitude");
            FloatDataPoint lon = (FloatDataPoint) fields.get("longitude");
            LatLng coordinate = LatLng.newInstance(lat.getFloatValue(), lon.getFloatValue());
            this.startMarker.setLatLng(coordinate);

        } else {
            newTraceStartIndex = this.traceStartIndex;
        }

        // change end of trace
        if (newTraceEndIndex != -1 && newTraceEndIndex > newTraceStartIndex) {
            // add vertices at END of trace if newTraceEnd > traceEndIndex
            if (newTraceEndIndex > traceEndIndex) {
                Log.d(TAG, "Add " + (newTraceEndIndex - traceEndIndex) + " vertices at end");
                Map<String, DataPoint> fields = null;
                for (int i = this.traceEndIndex + 1; i <= newTraceEndIndex; i++) {
                    value = (JsonDataPoint) this.sensorData[i];
                    fields = value.getFields();
                    FloatDataPoint lat = (FloatDataPoint) fields.get("latitude");
                    FloatDataPoint lon = (FloatDataPoint) fields.get("longitude");
                    LatLng coordinate = LatLng
                            .newInstance(lat.getFloatValue(), lon.getFloatValue());

                    this.trace.insertVertex(this.trace.getVertexCount(), coordinate);
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
            JsonDataPoint endValue = (JsonDataPoint) this.sensorData[newTraceEndIndex];
            Map<String, DataPoint> fields = endValue.getFields();
            FloatDataPoint lat = (FloatDataPoint) fields.get("latitude");
            FloatDataPoint lon = (FloatDataPoint) fields.get("longitude");
            LatLng coordinate = LatLng.newInstance(lat.getFloatValue(), lon.getFloatValue());
            this.endMarker.setLatLng(coordinate);

        } else {
            newTraceEndIndex = this.traceEndIndex;
        }

        // update trace indexes
        this.traceStartIndex = newTraceStartIndex;
        this.traceEndIndex = newTraceEndIndex;
    }

    @Override
    public void addData(JavaScriptObject data) {
        // TODO Auto-generated method stub

    }
}
