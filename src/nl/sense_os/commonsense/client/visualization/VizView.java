package nl.sense_os.commonsense.client.visualization;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.states.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.components.SensorDataGrid;
import nl.sense_os.commonsense.client.visualization.components.TimeLineCharts;
import nl.sense_os.commonsense.client.visualization.components.VisualizationTab;
import nl.sense_os.commonsense.client.visualization.map.MapEvents;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.google.gwt.user.client.ui.Frame;

public class VizView extends View {

    private static final String TAG = "VizView";
    private TabPanel tabPanel;
    private List<SensorModel> outstandingReqs;
    private TabItem unfinishedTab;
    private int reqFailCount;
    private long startTime;
    private long endTime;

    public VizView(Controller controller) {
        super(controller);
    }

    /**
     * Requests the sensor values for a given tagged sensor type.
     * 
     * @param tag
     *            the tag to request data for
     * @see #startRequests(TagModel[])
     */
    private void getSensorData(TreeModel tag) {
        Log.d(TAG, "Request sensor data: " + tag.<String> get("id"));

        AppEvent requestEvent = new AppEvent(VizEvents.DataRequested);
        requestEvent.setData("sensor", tag);
        requestEvent.setData("startDate", (this.startTime / 1000d));
        requestEvent.setData("endDate", (this.endTime / 1000d));
        Dispatcher.forwardEvent(requestEvent);
    }

    private void initTabPanel() {
        // Tabs panel
        this.tabPanel = new TabPanel();
        this.tabPanel.setId("tab-panel");
        this.tabPanel.setSize("100%", "100%");
        this.tabPanel.setPlain(true);
        this.tabPanel.addStyleName("transparent");

        // Welcome tab item
        final Frame welcomeFrame = new Frame("http://welcome.sense-os.nl/node/9");
        welcomeFrame.setStylePrimaryName("senseFrame");
        final TabItem welcomeItem = new TabItem("Welcome");
        welcomeItem.setLayout(new FitLayout());
        LayoutData data = new FitData(new Margins(-150, 0, 0, 0));
        welcomeItem.add(welcomeFrame, data);
        this.tabPanel.add(welcomeItem);

        // Track trace
        final Frame trackTrace = new Frame("http://almendetracker.appspot.com/?profileURL="
                + "http://demo.almende.com/tracker/ictdelta");
        trackTrace.setStylePrimaryName("senseFrame");
        final TabItem trackTraceItem = new TabItem("Track & Trace demo");
        trackTraceItem.setLayout(new FitLayout());
        trackTraceItem.add(trackTrace);
        this.tabPanel.add(trackTraceItem);

        // Humidity
        final Frame humid3d = new Frame(
                "http://demo.almende.com/links/storm/day_40_humid_animation.html");
        humid3d.setStylePrimaryName("senseFrame");
        final TabItem humid3dItem = new TabItem("3D Humidity");
        humid3dItem.setLayout(new FitLayout());
        humid3dItem.add(humid3d);
        this.tabPanel.add(humid3dItem);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(VizEvents.Show)) {
            // Log.d(TAG, "Show");
            final LayoutContainer parent = event.<LayoutContainer> getData("parent");
            showPanel(parent);

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);

        } else if (type.equals(VizEvents.DataNotReceived)) {
            Log.w(TAG, "DataNotReceived");
            onRequestFailed();

        } else if (type.equals(VizEvents.DataReceived)) {
            // Log.d(TAG, "DataReceived");
            final TaggedDataModel data = event.<TaggedDataModel> getData();
            onSensorValuesReceived(data);

        } else if (type.equals(StateEvents.FeedbackComplete)
                || type.equals(StateEvents.FeedbackCancelled)) {
            // Log.d(TAG, "FeedbackComplete");
            removeFeedback();

        } else if (type.equals(StateEvents.FeedbackReady)) {
            // Log.d(TAG, "FeedbackReady");
            final Component feedbackPanel = event.getData();
            showFeedback(feedbackPanel);

        } else if (type.equals(VizEvents.ShowLineChart)) {
            // Log.d(TAG, "ShowLineChart");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long startTime = event.<Long> getData("startTime");
            final long endTime = event.<Long> getData("endTime");
            showLineChart(sensors, startTime, endTime);

        } else if (type.equals(VizEvents.ShowTable)) {
            // Log.d(TAG, "ShowTable");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            showTable(sensors);

        } else if (type.equals(MapEvents.MapReady)) {
            // Log.d(TAG, "MapReady");
            final Component mapPanel = event.getData();
            showMap(mapPanel);

        } else if (type.equals(VizEvents.ShowNetwork)) {
            Log.w(TAG, "ShowNetwork not implemented");

        } else {
            Log.e(TAG, "Unexpected event type: " + type);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        initTabPanel();
        setupDragDrop();
    }

    /**
     * Handles failed requests for sensor data. Retries the request 3 times, and then passes null to
     * {@link #onSensorValuesReceived(TaggedDataModel)} to indicate definite failure.
     */
    public void onRequestFailed() {
        Log.w(TAG, "Request failed");

        onSensorValuesReceived(null);
    }

    private void onLoggedOut(AppEvent event) {
        resetTabs();
    }

    private void resetTabs() {
        for (TabItem items : tabPanel.getItems()) {
            if (items.isClosable()) {
                this.tabPanel.remove(items);
            }
        }
    }

    /**
     * Handles a visualization request by displaying a dialog for the preferred action to take.
     * 
     * @param treeStoreModels
     *            list of dropped tags
     * @see #setupDragDrop()
     */
    private void onTagsDropped(List<TreeStoreModel> treeStoreModels) {

        // get the children of node tags
        List<TreeModel> tags = new ArrayList<TreeModel>();
        for (TreeStoreModel tsm : treeStoreModels) {
            final TreeModel tag = (TreeModel) tsm.getModel();
            if (false == tags.contains(tag)) {
                int tagType = tag.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_SENSOR) {
                    tags.add(tag);
                } else {
                    // add any children
                    for (ModelData model : tsm.getChildren()) {
                        TreeStoreModel tm = (TreeStoreModel) model;
                        TreeModel child = (TreeModel) tm.getModel();
                        if (false == tags.contains(child)) {
                            tags.add(child);
                        }
                    }
                }
            }
        }

        Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, tags);
    }

    /**
     * Handles the callback from the sensor data RPC request. Adds the received data to the open
     * visualization tab. Requests data for the next tagged sensor, if there are still outstanding
     * requests. Otherwise removes the "waiting for data" label from the tab and displays any errors
     * that might have occurred during the series of requests.
     * 
     * @param data
     *            the received TaggedDataModel
     */
    public void onSensorValuesReceived(TaggedDataModel data) {

        // remove the tag from outstandingReqs
        if (this.outstandingReqs == null || this.outstandingReqs.size() == 0) {
            return;
        }
        this.outstandingReqs.remove(0);

        if (null != data) {
            Log.d(TAG, "Received sensor data from service!");

            final VisualizationTab charts = (VisualizationTab) this.unfinishedTab.getItem(0);
            charts.addData(data);
        } else {
            this.reqFailCount++;
        }

        // show the results or request more data if there are still tags left
        if (this.outstandingReqs.size() > 0) {
            getSensorData(this.outstandingReqs.get(0));
        } else {
            // Log.d(TAG, "Finalizing visualization tab...");
            final VisualizationTab charts = (VisualizationTab) this.unfinishedTab.getItem(0);
            charts.setWaitingText(false);

            if (this.reqFailCount > 0) {
                String msg = "There was a problem getting some of the sensor data. Please try again.";
                MessageBox.alert("CommonSense", msg, null);
            }
        }
    }

    public void removeFeedback() {
        // TODO this ain't right
        this.tabPanel.remove(this.tabPanel.getSelectedItem());
    }

    /**
     * Sets up the tab panel for drag and drop of the tags.
     * 
     * @see #onTagsDropped(ArrayList)
     */
    private void setupDragDrop() {
        final DropTarget dropTarget = new DropTarget(this.tabPanel);
        dropTarget.setOperation(Operation.COPY);
        dropTarget.addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                final ArrayList<TreeStoreModel> data = e.<ArrayList<TreeStoreModel>> getData();
                onTagsDropped(data);
            }
        });
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(this.tabPanel);
            parent.layout();
        } else {
            Log.e(TAG, "Failed to show visualization panel: parent=null");
        }
    }

    public void showFeedback(Component feedbackPanel) {

        // add line chart tab item
        final TabItem item = new TabItem("Feedback");
        item.setLayout(new FitLayout());
        item.setClosable(true);
        item.add(feedbackPanel);
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
        this.unfinishedTab = item;
    }

    public void showLineChart(List<SensorModel> sensors, long startTime, long endTime) {

        // add line chart tab item
        final TabItem item = new TabItem("Line chart");
        item.setLayout(new FitLayout());
        item.setClosable(true);
        final VisualizationTab charts = new TimeLineCharts();
        charts.setWaitingText(true);
        item.add(charts);
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
        this.unfinishedTab = item;

        startRequests(sensors, startTime, endTime);
    }

    public void showMap(Component mapPanel) {
        // add map tab item
        final TabItem item = new TabItem("Map");
        item.setLayout(new FitLayout());
        item.setClosable(true);
        item.add(mapPanel);
        item.layout();

        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
        this.unfinishedTab = item;
    }

    public void showTable(List<SensorModel> sensors) {

        // add table tab item
        final TabItem item = new TabItem("Table");
        item.setClosable(true);
        item.setScrollMode(Scroll.AUTO);
        item.setLayout(new FitLayout());
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);

        // add sensor data grid
        item.add(new SensorDataGrid(sensors), new FitData());
        item.layout();
    }

    /**
     * Prepares for a series of RPC requests for data from a list of tags. Initializes some
     * constants and starts the first request with <code>requestSensorValues</code>.
     * 
     * @param tags
     *            the list of tagged sensors
     */
    private void startRequests(List<SensorModel> sensors, long startTime, long endTime) {

        // start requesting data for the list of tags
        this.outstandingReqs = sensors;
        this.unfinishedTab = this.tabPanel.getSelectedItem();
        this.reqFailCount = 0;
        this.startTime = startTime;
        this.endTime = endTime;

        if (sensors.size() > 0) {
            getSensorData(sensors.get(0));
        }
    }
}
