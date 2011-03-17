package nl.sense_os.commonsense.client.visualization.map.components;

import java.util.Map;

import nl.sense_os.commonsense.client.common.DateSlider;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;

public class MapPanel extends LayoutContainer {

    private static final String TAG = "MapPanel";
    private Text notificationText;
    private MapWidget map;
    private Slider startSlider;
    private Slider endSlider;
    private SensorValueModel[] sensorData = new SensorValueModel[]{};
    private LatLngBounds traceBounds;

    public MapPanel() {

        this.setLayout(new BorderLayout());
        this.setStyleAttribute("background", "rgba(0,0,0,0)");
        this.setId("viz-map");

        initNotificationBar();
        initSliders();
        initMapWidget();

        showNotificationBar();
    }

    /**
     * Display the markers and draw a trace line on the map.
     * 
     * @param sensor
     * @param data
     */
    public void addData(TreeModel sensor, SensorValueModel[] data) {
        // Store the sensor data to be used from other methods.
        this.sensorData = data;

        calcSliderRange(data);
        updateTrace();
        centerMap();
    }

    /**
     * Calculates the slider parameters for a set of sensor values.
     * 
     * @param data
     *            JsonValueModels with positional sensor data over time
     */
    private void calcSliderRange(SensorValueModel[] data) {

        JsonValueModel v = (JsonValueModel) data[0];
        int min = (int) (v.getTimestamp().getTime() / 1000);
        v = (JsonValueModel) data[data.length - 1];
        int max = (int) (v.getTimestamp().getTime() / 1000);

        int interval = 1;
        if (max - min < 60 * 60) {
            interval = 60 * 5; // 5 minutes
        } else if (max - min < 60 * 60 * 24) {
            interval = 60 * 60; // 1 hour
        } else {
            interval = 60 * 60 * 4; // 4 hour
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
        this.map.setCenter(this.traceBounds.getCenter());
        this.map.setZoomLevel(this.map.getBoundsZoomLevel(this.traceBounds));
    }

    /**
     * Hides the "waiting" notification bar
     */
    public void hideNotificationBar() {
        notificationText.hide();
    }

    /**
     * Initializes a Google map.
     */
    private void initMapWidget() {
        // Create the map.
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
     * Initializes the 'waiting for data' notification text, keeping it hidden.
     */
    private void initNotificationBar() {
        this.notificationText = new Text("Waiting for data...");
        this.notificationText.setStyleName("notification-bar");
        this.notificationText.setWidth(150);
        this.notificationText.setVisible(false);

        this.add(this.notificationText, new BorderLayoutData(LayoutRegion.NORTH, 30));
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

    private void showNotificationBar() {
        this.notificationText.setVisible(true);
    }

    /**
     * This method is called when it used the endSlider below the map. It filters the points to draw
     * a line depending on the time.
     * 
     * @param time
     */
    private LatLngBounds updateTrace() {

        int minTime = startSlider.getValue();
        int maxTime = endSlider.getValue();

        // Clean the map.
        this.map.clearOverlays();
        this.traceBounds = null;

        // Draw the filtered points.
        if (sensorData.length > 0 && maxTime > minTime) {
            LatLng[] points = new LatLng[sensorData.length];

            int lastPoint = 0;

            // All the points between the startTime and endTime will be drawn
            for (int i = 0, j = 0; i < sensorData.length; i++) {
                JsonValueModel value = (JsonValueModel) sensorData[i];
                Map<String, Object> fields = value.getFields();

                double latitude = (Double) fields.get("latitude");
                double longitude = (Double) fields.get("longitude");

                // timestamp in secs
                long timestamp = value.getTimestamp().getTime() / 1000;

                if (timestamp > minTime && timestamp < maxTime) {
                    lastPoint = j;
                    LatLng coordinate = LatLng.newInstance(latitude, longitude);
                    points[j++] = coordinate;
                    if (null == this.traceBounds) {
                        this.traceBounds = LatLngBounds.newInstance(coordinate, coordinate);
                    } else {
                        this.traceBounds.extend(coordinate);
                    }
                }
            }

            // Add the first marker
            Marker startMarker = new Marker(points[0]);
            startMarker.setDraggingEnabled(true);
            map.addOverlay(startMarker);

            // Add the last marker
            Marker endMarker = new Marker(points[lastPoint]);
            endMarker.setDraggingEnabled(true);
            map.addOverlay(endMarker);

            // Draw a track line
            PolylineOptions lineOptions = PolylineOptions.newInstance(false, true);
            Polyline trace = new Polyline(points, "#FF7F00", 5, 1, lineOptions);
            map.addOverlay(trace);
        } else {
            Log.w(TAG, "No position values in selected time range");
        }

        return this.traceBounds;
    }
}
