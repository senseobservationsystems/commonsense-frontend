package nl.sense_os.commonsense.client.map;

import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class MapView extends View {

    private static final String TAG = "MapView";
    private TreeModel[] sensors;

    public MapView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType evtType = event.getType();

        if (evtType.equals(MapEvents.CreateMap)) {
            // Log.d(TAG, "onCreateMap");
            onCreateMap(event);

        } else {
            Log.e(TAG, "Unexpected event received!");
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    private void onCreateMap(AppEvent event) {
        this.sensors = event.<TreeModel[]> getData("sensors");

        if (this.sensors != null) {
            Log.d(TAG, "sensors length: " + this.sensors.length);
        }

        TreeModel[] sensorList = getSensors();

        if (sensorList != null)
            Log.d(TAG, "sensorList: " + sensorList.toString());

        MapPanel mapPanel = new MapPanel();
        createMap(mapPanel);

        // The created panel is sent to
        Dispatcher.forwardEvent(MapEvents.MapReady, mapPanel);
    }

    public TreeModel[] getSensors() {
        return this.sensors;
    }

    private void createMap(MapPanel panel) {
        // Open a map centered on Cawker City, KS USA
        // LatLng cawkerCity = LatLng.newInstance(39.509, -98.434);

        // final MapWidget map = new MapWidget(cawkerCity, 2);
        final MapWidget map = new MapWidget();
        map.setSize("100%", "100%");
        // Add some controls for the zoom level
        map.addControl(new LargeMapControl());

        // Add a marker
        // map.addOverlay(new Marker(cawkerCity));

        // Add an info window to highlight a point of interest
        // map.getInfoWindow().open(map.getCenter(),
        // new InfoWindowContent("World's Largest Ball of Sisal Twine"));

        final DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
        dock.addNorth(map, 500);

        // Add the map to the HTML host page
        panel.add(dock);
    }

}
