package nl.sense_os.commonsense.client.viz.panels.map;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.common.json.overlays.FloatDataPoint;
import nl.sense_os.commonsense.client.common.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.common.models.SensorModel;
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

    private static final Logger logger = Logger.getLogger("MapPanel");
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

        this.setHeading("Map: " + title);
        this.setLayout(new BorderLayout());
        this.setId("viz-map-" + title);

        initSliders();
        initMapWidget();

        visualize(sensors, start, end);
    }

    @Override
    public void addData(JsArray<Timeseries> data) {

        // sort lat/lng data
        Timeseries ts;
        for (int i = 0; i < data.length(); i++) {
            ts = data.get(i);
            if (ts.getLabel().endsWith("latitude")) {
                this.latTimeseries = ts;
            } else if (ts.getLabel().endsWith("longitude")) {
                this.lngTimeseries = ts;
            }
        }

        if (this.latTimeseries != null && this.lngTimeseries != null) {
            calcSliderRange();
            drawTrace();
            centerMap();
        }
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
        JsArray<DataPoint> latValues = this.latTimeseries.getData();
        JsArray<DataPoint> lngValues = this.lngTimeseries.getData();

        // logger.fine( "Number of points: " + latValues.length());

        // Draw the filtered points.
        if (latValues.length() > 0 && maxTime > minTime) {
            LatLng[] points = new LatLng[latValues.length()];

            this.traceStartIndex = -1;
            this.traceEndIndex = -1;
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
                    if (-1 == this.traceStartIndex) {
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
            logger.warning("No position values in selected time range");
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

        // logger.fine( "updateTrace ");

        if (null == trace || false == trace.isVisible()) {
            logger.fine("updateTrace skipped: trace is not shown yet");
            return;
        }

        // get the sensor values
        JsArray<DataPoint> latValues = this.latTimeseries.getData();
        JsArray<DataPoint> lonValues = this.lngTimeseries.getData();

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

        // logger.fine( "old start: " + traceStartIndex + ", old end: " + traceEndIndex);
        // logger.fine( "new start: " + newTraceStartIndex + ", new end: " + newTraceEndIndex);

        if (newTraceStartIndex > newTraceEndIndex) {
            logger.warning("Start index of trace is larger than end index?!");
        }

        // add vertices at START of trace if newTraceStart < traceStartIndex
        if (newTraceStartIndex < traceStartIndex) {
            // logger.fine( "Add " + (traceStartIndex - newTraceStartIndex) + " vertices at start");
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
            // logger.fine( "Delete " + (newTraceStartIndex - traceStartIndex) +
            // " vertices at start");
            for (int i = this.traceStartIndex; i < newTraceStartIndex; i++) {
                this.trace.deleteVertex(0);
            }
        }

        // update start marker
        FloatDataPoint startLat = latValues.get(newTraceStartIndex).cast();
        FloatDataPoint startLon = lonValues.get(newTraceStartIndex).cast();
        LatLng startCoordinate = LatLng.newInstance(startLat.getValue(), startLon.getValue());
        this.startMarker.setLatLng(startCoordinate);

        // add vertices at END of trace if newTraceEnd > traceEndIndex
        if (newTraceEndIndex > traceEndIndex) {
            // logger.fine( "Add " + (newTraceEndIndex - traceEndIndex) + " vertices at end");
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
            // logger.fine( "Delete " + (traceEndIndex - newTraceEndIndex) + " vertices at end");
            for (int i = this.traceEndIndex; i > newTraceEndIndex; i--) {
                this.trace.deleteVertex(this.trace.getVertexCount() - 1);
            }
        }

        // update end marker
        FloatDataPoint endLat = latValues.get(newTraceEndIndex).cast();
        FloatDataPoint endLon = lonValues.get(newTraceEndIndex).cast();
        LatLng endCoordinate = LatLng.newInstance(endLat.getValue(), endLon.getValue());
        this.endMarker.setLatLng(endCoordinate);

        // update trace indexes
        this.traceStartIndex = newTraceStartIndex;
        this.traceEndIndex = newTraceEndIndex;
    }
}
