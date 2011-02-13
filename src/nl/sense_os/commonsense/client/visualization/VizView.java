package nl.sense_os.commonsense.client.visualization;

import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.map.MapEvents;
import nl.sense_os.commonsense.client.states.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.components.Visualization;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

public class VizView extends View {

    private static final String TAG = "VizView";
    private Visualization vizPanel;

    public VizView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(VizEvents.Show)) {
            // Log.d(TAG, "Show");
            onShow(event);

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);

        } else if (type.equals(VizEvents.DataNotReceived)) {
            Log.w(TAG, "DataNotReceived");
            onDataNotReceived(event);

        } else if (type.equals(VizEvents.DataReceived)) {
            Log.d(TAG, "DataReceived");
            onDataReceived(event);

        } else if (type.equals(StateEvents.FeedbackComplete)
                || type.equals(StateEvents.FeedbackCancelled)) {
            // Log.d(TAG, "FeedbackComplete");

            this.vizPanel.removeFeedback();

        } else if (type.equals(StateEvents.FeedbackReady)) {
            Log.d(TAG, "FeedbackReady");
            ContentPanel feedbackPanel = event.getData();
            this.vizPanel.showFeedback(feedbackPanel);

        } else if (type.equals(VizEvents.ShowLineChart)) {
            Log.d(TAG, "ShowLineChart");
            TreeModel[] sensors = event.<TreeModel[]> getData("sensors");
            long startTime = event.<Long> getData("startTime");
            long endTime = event.<Long> getData("endTime");
            this.vizPanel.showLineChart(sensors, startTime, endTime);

        } else if (type.equals(VizEvents.ShowTable)) {
            Log.d(TAG, "ShowTable");
            TreeModel[] sensors = event.<TreeModel[]> getData("sensors");
            this.vizPanel.showTable(sensors);

        } else if (type.equals(MapEvents.MapReady)) {
            Log.d(TAG, "MapReady");
            ContentPanel mapPanel = event.getData();
            this.vizPanel.showMap(mapPanel);

        } else if (type.equals(VizEvents.ShowNetwork)) {
            Log.w(TAG, "ShowNetwork not implemented");

        } else {
            Log.e(TAG, "Unexpected event type: " + type);
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

    private void onLoggedOut(AppEvent event) {
        this.vizPanel.resetTabs();
    }

    private void onShow(AppEvent event) {
        this.vizPanel.setId("visualization");

        LayoutContainer parent = event.<LayoutContainer> getData();
        parent.removeAll();
        parent.add(this.vizPanel);
        parent.layout();
    }
}
