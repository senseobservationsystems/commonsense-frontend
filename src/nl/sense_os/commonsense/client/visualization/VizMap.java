package nl.sense_os.commonsense.client.visualization;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class VizMap extends View {

    private static final String TAG = "VizMap";
    private TreeModel[] sensors;

	public VizMap(Controller c) {
		super(c);
	}

	@Override
	protected void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type.equals(VizEvents.ShowMap)) {
			// Log.d(TAG, "Show");
			onShow(event);
		} else {
			Log.e(TAG, "Unexpected event type received!");
		}
	}

    @Override
    protected void initialize() {
        super.initialize();
    }

	private void onShow(AppEvent event) {
		this.sensors = event.<TreeModel[]> getData("sensors");

		if (this.sensors != null) {
			Log.d(TAG, "sensors length: " + this.sensors.length);
		}

		// Asynchronously loads the Maps API.
        Maps.loadMapsApi(Constants.MAPS_API_KEY, "2", false, new Runnable() {
            public void run() {
                TreeModel[] sensorList = getSensors();

                if (sensorList != null)
                    Log.d(TAG, "sensorList: " + sensorList.toString());

                ContentPanel panel = new ContentPanel(new FitLayout());
                panel.setHeaderVisible(false);
                panel.setBodyBorder(false);
                panel.setScrollMode(Scroll.NONE);
                panel.setId("viz-map");

                createMap(panel);

                AppEvent response = new AppEvent(VizEvents.MapReady);
                response.setData(panel);
                Dispatcher.forwardEvent(response);
            }
        });
    }

    public TreeModel[] getSensors() {
        return this.sensors;
    }

    private void createMap(ContentPanel panel) {
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
