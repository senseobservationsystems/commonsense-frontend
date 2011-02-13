package nl.sense_os.commonsense.client.visualization.map.components;

import java.util.Map;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.widget.ContentPanel;
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

    public MapPanel() {
        setLayout(new FitLayout());
        setHeaderVisible(false);
        setBodyBorder(false);
        setScrollMode(Scroll.NONE);
        setId("viz-map");

        initMap();
    }

    private void initMap() {
        // Open a map centered on Cawker City, KS USA
        // LatLng cawkerCity = LatLng.newInstance(39.509, -98.434);

        // final MapWidget map = new MapWidget(cawkerCity, 2);
        this.map = new MapWidget();
        this.map.setSize("100%", "100%");
        // Add some controls for the zoom level
        this.map.addControl(new LargeMapControl());

        // Add a marker
        // map.addOverlay(new Marker(cawkerCity));

        // Add an info window to highlight a point of interest
        // map.getInfoWindow().open(map.getCenter(),
        // new InfoWindowContent("World's Largest Ball of Sisal Twine"));

        final DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
        dock.addNorth(map, 500);

        // Add the map to the HTML host page
        this.add(dock);
    }

    public void addData(TreeModel sensor, SensorValueModel[] data) {

        if (data.length > 0) {

            // add line between each data point
            LatLng[] points = new LatLng[data.length];
            for (int i = 0; i < data.length; i++) {
                JsonValueModel value = (JsonValueModel) data[i];
                Map<String, Object> fields = value.getFields();
                double latitude = (Double) fields.get("latitude");
                double longitude = (Double) fields.get("longitude");
                points[i] = LatLng.newInstance(latitude, longitude);
                // Log.d(TAG, "Point: (" + latitude + ", " + longitude + ")");
            }
            Polyline trace = new Polyline(points);
            map.addOverlay(trace);

            // center on last marker
            Marker marker = new Marker(points[points.length - 1]);
            map.addOverlay(marker);
            map.setCenter(marker.getLatLng());
            map.setZoomLevel(12);
        }
    }

    /**
     * Should display something to show the user that we are done loading data.
     */
    public void finishLoading() {
        Log.d(TAG, "Finished loading!");
    }
}
