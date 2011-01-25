package nl.sense_os.commonsense.client.mvc.views;

import java.util.List;

import nl.sense_os.commonsense.client.components.Visualization;
import nl.sense_os.commonsense.client.mvc.events.GroupEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.MySensorsEvents;
import nl.sense_os.commonsense.client.mvc.events.VizEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;

public class VizView extends View {

    private static final String TAG = "VizView";
    private Visualization vizPanel;

    public VizView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType eventType = event.getType();

        if (eventType.equals(MainEvents.ShowVisualization)) {
            // Log.d(TAG, "Show");
            onShow(event);
        } else if (eventType.equals(LoginEvents.LoggedIn)) {
            // Log.d(TAG, "LoggedIn");
            onLoggedIn(event);
        } else if (eventType.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else if (eventType.equals(VizEvents.DataNotReceived)) {
            Log.w(TAG, "DataNotReceived");
            onDataNotReceived(event);
        } else if (eventType.equals(VizEvents.DataReceived)) {
            Log.d(TAG, "DataReceived");
            onDataReceived(event);
        } else if (eventType.equals(VizEvents.VizRequested)) {
            Log.d(TAG, "VizRequested");
            onVizRequested(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + eventType);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.vizPanel = new Visualization();
    }

    private void onDataNotReceived(AppEvent event) {
        this.vizPanel.onRequestFailed();
    }

    private void onDataReceived(AppEvent event) {
        TaggedDataModel data = event.<TaggedDataModel> getData();
        this.vizPanel.onSensorValuesReceived(data);
    }

    private void onLoggedIn(AppEvent event) {
        // Dispatcher.forwardEvent(TagsEvents.TagsRequested);
        // Dispatcher.forwardEvent(GroupsEvents.GroupsRequested);
    }

    private void onLoggedOut(AppEvent event) {
        
    }

    private void onShow(AppEvent event) {
        this.vizPanel.setId("visualization");
        
        ContentPanel center = event.<ContentPanel> getData();
        center.removeAll();
        center.add(this.vizPanel);
        center.layout();

        Dispatcher.forwardEvent(MySensorsEvents.MySensorsRequested);
        Dispatcher.forwardEvent(GroupEvents.GroupListRequested);
    }

    private void onVizRequested(AppEvent event) {
        List<TreeModel> selection = event.<List<TreeModel>> getData();
        this.vizPanel.onVizRequested(selection);
    }
}
