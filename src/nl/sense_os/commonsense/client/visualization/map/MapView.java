package nl.sense_os.commonsense.client.visualization.map;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.map.components.MapPanel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

import java.util.List;

public class MapView extends View {

    private static final String TAG = "MapView";

    public MapView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType evtType = event.getType();

        if (evtType.equals(MapEvents.Show)) {
            // Log.d(TAG, "Show");
            onCreateMap(event);

        } else if (evtType.equals(MapEvents.AddData)) {
            // Log.d(TAG, "AddData");
            final MapPanel panel = event.<MapPanel> getData("panel");
            final SensorValueModel[] data = event.<SensorValueModel[]> getData("data");
            final TreeModel sensor = event.<TreeModel> getData("sensor");
            panel.addData(sensor, data);

        } else if (evtType.equals(MapEvents.LoadSuccess)) {
            // Log.d(TAG, "LoadSuccess");
            final MapPanel panel = event.<MapPanel> getData("panel");
            panel.hideNotificationBar();

        } else if (evtType.equals(MapEvents.LoadFailure)) {
            Log.w(TAG, "LoadFailure");
            // TODO handle this
            final MapPanel panel = event.<MapPanel> getData("panel");
            panel.hideNotificationBar();

        } else {
            Log.e(TAG, "Unexpected event received!");
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    private void onCreateMap(AppEvent event) {
        List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
        long startTime = event.getData("startTime");
        long endTime = event.getData("endTime");

        // create map panel
        MapPanel mapPanel = new MapPanel();

        // request sensor data to display on the panel
        AppEvent loadEvent = new AppEvent(MapEvents.LoadData);
        loadEvent.setData("sensor", sensors.get(0));
        loadEvent.setData("startDate", startTime / 1000d);
        loadEvent.setData("endDate", endTime / 1000d);
        loadEvent.setData("panel", mapPanel);
        fireEvent(loadEvent);

        // TODO make map handle more than 1 location sensor

        // The panel is dispatched to a View that can display it
        Dispatcher.forwardEvent(MapEvents.MapReady, mapPanel);
    }

}
