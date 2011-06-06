package nl.sense_os.commonsense.client.viz.panels.map;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.FloatDataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
import nl.sense_os.commonsense.client.viz.panels.VizPanel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.util.Margins;
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

public class MapPanel extends VizPanel {

    private static final Logger LOG = Logger.getLogger(MapPanel.class.getName());
    private MapWidget map;
    private DateSlider startSlider;
    private DateSlider endSlider;
    private Timeseries latTimeseries;
    private Timeseries lngTimeseries;
    private Marker startMarker;
    private Marker endMarker;
    private Polyline trace;
    private int traceStartIndex;
    private int traceEndIndex;

    public MapPanel(List<SensorModel> sensors, long start, long end, String title) {
        super();

        setHeading("Map: " + title);
        setLayout(new BorderLayout());
        setId("viz-map-" + title);

        initSliders();
        initMapWidget();

        visualize(sensors, start, end);
    }

    /**
     * Calculates the slider parameters for a set of sensor values.
     * 
     * @param data
     *            JsonValueModels with positional sensor data over time
     */
    private void calcSliderRange() {

        JsArray<DataPoint> values = latTimeseries.getData();
        DataPoint v = values.get(0);
        int min = (int) Math.floor(v.getTimestamp().getTime() / 1000l);
        v = values.get(values.length() - 1);
        int max = (int) Math.ceil(v.getTimestamp().getTime() / 1000l);

        int interval = (max - min) / 25;
        max = min + 25 * interval;

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
        final LatLngBounds bounds = trace.getBounds();
        map.setCenter(bounds.getCenter());
        map.setZoomLevel(map.getBoundsZoomLevel(bounds));
    }

    /**
     * This method is called when data is first added to the map. It draws the complete trace on the
     * map, based on the current setting of the sliders.
     */
    private void drawTrace() {

        // clean the map
        map.clearOverlays();
        trace = null;

        // get the time window for the trace from the sliders
        int minTime = startSlider.getValue();
        int maxTime = endSlider.getValue();

        // get the sensor values
        JsArray<DataPoint> latValues = latTimeseries.getData();
        JsArray<DataPoint> lngValues = lngTimeseries.getData();

        // LOG.fine( "Number of points: " + latValues.length());

        // Draw the filtered points.
        if (latValues.length() > 0 && maxTime > minTime) {
            LatLng[] points = new LatLng[latValues.length()];

            traceStartIndex = -1;
            traceEndIndex = -1;
            int lastPoint = -1;
            FloatDataPoint lat;
            FloatDataPoint lng;
            for (int i = 0, j = 0; i < latValues.length(); i++) {
                lat = latValues.get(i).cast();
                lng = lngValues.get(i).cast();

                // timestamp in secs
                long timestamp = lat.getTimestamp().getTime() / 1000;

                if (timestamp > minTime && timestamp < maxTime) {
                    // update indices
                    lastPoint = j;
                    traceEndIndex = i;
                    if (-1 == traceStartIndex) {
                        traceStartIndex = i;
                    }
                    // store coordinate
                    LatLng coordinate = LatLng.newInstance(lat.getValue(), lng.getValue());
                    points[j] = coordinate;
                    j++;
                }
            }

            // Add the first marker
            final MarkerOptions markerOptions = MarkerOptions.newInstance();
            startMarker = new Marker(points[0], markerOptions);
            map.addOverlay(startMarker);

            // Add the last marker
            endMarker = new Marker(points[lastPoint], markerOptions);
            map.addOverlay(endMarker);

            // Draw a track line
            PolylineOptions lineOptions = PolylineOptions.newInstance(false, true);
            trace = new Polyline(points, "#FF7F00", 5, 1, lineOptions);
            map.addOverlay(trace);

        } else {
            LOG.warning("No position values in selected time range");
        }
    }

    /**
     * Initializes a Google map.
     */
    private void initMapWidget() {

        map = new MapWidget();
        map.setWidth("100%");

        // Add some controls for the zoom level
        map.setUIToDefault();

        // Add the map to the layout
        BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
        layoutData.setMargins(new Margins(5));
        this.add(map, layoutData);
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
        startSlider = new DateSlider();
        startSlider.setMessage("{0}");
        startSlider.setId("viz-map-startSlider");
        startSlider.addListener(Events.Change, slideListener);

        SliderField startField = new SliderField(startSlider);
        startField.setFieldLabel("Trace start");

        endSlider = new DateSlider();
        endSlider.setMessage("{0}");
        endSlider.setValue(endSlider.getMaxValue());
        endSlider.setId("viz-map-endSlider");
        endSlider.addListener(Events.Change, slideListener);

        SliderField endField = new SliderField(endSlider);
        endField.setFieldLabel("Trace end");

        slidersForm.add(startField, new FormData("-5"));
        slidersForm.add(endField, new FormData("-5"));

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 50);
        data.setMargins(new Margins(0, 5, 5, 5));
        this.add(slidersForm, data);
    }

    @Override
    protected void onNewData() {

        // sort lat/lng data
        Timeseries ts;
        for (int i = 0; i < data.length(); i++) {
            ts = data.get(i);
            if (ts.getLabel().endsWith("latitude")) {
                latTimeseries = ts;
            } else if (ts.getLabel().endsWith("longitude")) {
                lngTimeseries = ts;
            }
        }

        if (latTimeseries != null && lngTimeseries != null) {
            calcSliderRange();
            drawTrace();
            centerMap();
        }
    }

    private void updateTrace() {

        // LOG.fine( "updateTrace ");

        if (null == trace || false == trace.isVisible()) {
            LOG.fine("updateTrace skipped: trace is not shown yet");
            return;
        }

        // get the sensor values
        JsArray<DataPoint> latValues = latTimeseries.getData();
        JsArray<DataPoint> lonValues = lngTimeseries.getData();

        int minTime = startSlider.getValue();
        int maxTime = endSlider.getValue();

        // find the start end end indices of the trace in the sensor data array
        int newTraceStartIndex = 0, newTraceEndIndex = latValues.length() - 1;
        long timestamp;
        for (int i = 0; i < latValues.length(); i++) {
            // get timestamp
            timestamp = latValues.get(i).getTimestamp().getTime() / 1000;

            if (timestamp > minTime && newTraceStartIndex == 0) {
                // this is the first index with start of visible range
                newTraceStartIndex = i;
            }
            if (timestamp > maxTime) {
                // this is the first index after the end of visible range
                newTraceEndIndex = i - 1;
                break;
            }
        }

        // LOG.fine( "old start: " + traceStartIndex + ", old end: " + traceEndIndex);
        // LOG.fine( "new start: " + newTraceStartIndex + ", new end: " + newTraceEndIndex);

        if (newTraceStartIndex > newTraceEndIndex) {
            LOG.warning("Start index of trace is larger than end index?!");
        }

        // add vertices at START of trace if newTraceStart < traceStartIndex
        if (newTraceStartIndex < traceStartIndex) {
            // LOG.fine( "Add " + (traceStartIndex - newTraceStartIndex) + " vertices at start");
            FloatDataPoint lat;
            FloatDataPoint lon;
            for (int i = traceStartIndex - 1; i >= newTraceStartIndex; i--) {
                lat = latValues.get(i).cast();
                lon = lonValues.get(i).cast();
                trace.insertVertex(0, LatLng.newInstance(lat.getValue(), lon.getValue()));
            }
        }

        // delete vertices at START of trace if newTraceStart > traceStartIndex
        if (newTraceStartIndex > traceStartIndex) {
            // LOG.fine( "Delete " + (newTraceStartIndex - traceStartIndex) +
            // " vertices at start");
            for (int i = traceStartIndex; i < newTraceStartIndex; i++) {
                trace.deleteVertex(0);
            }
        }

        // update start marker
        FloatDataPoint startLat = latValues.get(newTraceStartIndex).cast();
        FloatDataPoint startLon = lonValues.get(newTraceStartIndex).cast();
        LatLng startCoordinate = LatLng.newInstance(startLat.getValue(), startLon.getValue());
        startMarker.setLatLng(startCoordinate);

        // add vertices at END of trace if newTraceEnd > traceEndIndex
        if (newTraceEndIndex > traceEndIndex) {
            // LOG.fine( "Add " + (newTraceEndIndex - traceEndIndex) + " vertices at end");
            FloatDataPoint lat;
            FloatDataPoint lon;
            for (int i = traceEndIndex + 1; i <= newTraceEndIndex; i++) {
                lat = latValues.get(i).cast();
                lon = lonValues.get(i).cast();
                trace.insertVertex(trace.getVertexCount(),
                        LatLng.newInstance(lat.getValue(), lon.getValue()));
            }
        }

        // delete vertices at END of trace if newTraceEnd < traceEndIndex
        if (newTraceEndIndex < traceEndIndex) {
            // LOG.fine( "Delete " + (traceEndIndex - newTraceEndIndex) + " vertices at end");
            for (int i = traceEndIndex; i > newTraceEndIndex; i--) {
                trace.deleteVertex(trace.getVertexCount() - 1);
            }
        }

        // update end marker
        FloatDataPoint endLat = latValues.get(newTraceEndIndex).cast();
        FloatDataPoint endLon = lonValues.get(newTraceEndIndex).cast();
        LatLng endCoordinate = LatLng.newInstance(endLat.getValue(), endLon.getValue());
        endMarker.setLatLng(endCoordinate);

        // update trace indexes
        traceStartIndex = newTraceStartIndex;
        traceEndIndex = newTraceEndIndex;
    }
}
