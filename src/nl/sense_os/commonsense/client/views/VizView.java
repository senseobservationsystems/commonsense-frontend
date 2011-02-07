package nl.sense_os.commonsense.client.views;

import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.events.VizEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.components.Visualization;
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
        } else if (eventType.equals(StateEvents.FeedbackComplete)) {
            Log.d(TAG, "FeedbackComplete");
            this.vizPanel.removeFeedback();
        } else if (eventType.equals(StateEvents.FeedbackReady)) {
            Log.d(TAG, "FeedbackReady");
            ContentPanel feedbackPanel = event.getData();
            this.vizPanel.showFeedback(feedbackPanel);
        } else if (eventType.equals(VizEvents.ShowLineChart)) {
            Log.d(TAG, "ShowLineChart");
            TreeModel[] sensors = event.<TreeModel[]> getData("sensors");
            long startTime = event.<Long> getData("startTime");
            long endTime = event.<Long> getData("endTime");
            this.vizPanel.showLineChart(sensors, startTime, endTime);
        } else if (eventType.equals(VizEvents.ShowTable)) {
            Log.d(TAG, "ShowTable");
            TreeModel[] sensors = event.<TreeModel[]> getData("sensors");
            this.vizPanel.showTable(sensors);
        } else if (eventType.equals(VizEvents.MapReady)) {
            Log.d(TAG, "MapReady");
            ContentPanel mapPanel = event.getData();
            this.vizPanel.showMap(mapPanel);
        } else if (eventType.equals(VizEvents.ShowNetwork)) {
            Log.w(TAG, "ShowNetwork not implemented");
            // TODO
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
        this.vizPanel.resetTabs();
    }

    private void onLoggedOut(AppEvent event) {

    }

    private void onShow(AppEvent event) {
        this.vizPanel.setId("visualization");

        LayoutContainer center = event.<LayoutContainer> getData();
        center.removeAll();
        center.add(this.vizPanel);
        center.layout();
    }
}
