package nl.sense_os.commonsense.client.visualization.components;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.environments.BuildingEvents;
import nl.sense_os.commonsense.client.groups.GroupEvents;
import nl.sense_os.commonsense.client.sensors.GroupSensorsEvents;
import nl.sense_os.commonsense.client.sensors.MySensorsEvents;
import nl.sense_os.commonsense.client.states.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.UserModel;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;

/**
 * Component with the visualization part of the web application.
 */
public class Visualization extends LayoutContainer {

    private static final String TAG = "Visualization";

    private TreeModel[] outstandingReqs;
    private int reqFailCount;
    private TabPanel tabPanel;
    private TabItem unfinishedTab;
    private long startTime;
    private long endTime;

    /**
     * Creates the big "center" panel of the main BorderLayout. Contains only the tabPanel for the
     * sensor values.
     */
    private void createCenterPanel() {

        // Tabs panel
        this.tabPanel = new TabPanel();
        this.tabPanel.setSize("100%", "100%");
        this.tabPanel.setPlain(true);
        this.tabPanel.addStyleName("transparent");

        // Welcome tab item
        final Frame welcomeFrame = new Frame("http://welcome.sense-os.nl/node/9");
        welcomeFrame.setStylePrimaryName("senseFrame");
        final TabItem welcomeItem = new TabItem("Welcome");
        welcomeItem.setLayout(new FitLayout());
        welcomeItem.add(welcomeFrame);
        this.tabPanel.add(welcomeItem);

        // Track trace
        UserModel user = Registry.get(Constants.REG_USER);
        if (user != null && user.getId() != 142) {
            final Frame trackTrace = new Frame("http://almendetracker.appspot.com/?profileURL="
                    + "http://demo.almende.com/tracker/ictdelta");
            trackTrace.setStylePrimaryName("senseFrame");
            final TabItem trackTraceItem = new TabItem("Track & Trace demo");
            trackTraceItem.setLayout(new FitLayout());
            trackTraceItem.setClosable(true);
            trackTraceItem.add(trackTrace);
            this.tabPanel.add(trackTraceItem);

            final Frame humid3d = new Frame(
                    "http://demo.almende.com/links/storm/day_40_humid_animation.html");
            humid3d.setStylePrimaryName("senseFrame");
            final TabItem humid3dItem = new TabItem("3D Humidity");
            humid3dItem.setLayout(new FitLayout());
            humid3dItem.setClosable(true);
            humid3dItem.add(humid3d);
            this.tabPanel.add(humid3dItem);
        }

        // add greenhouse building chart to please Freek
        if (user != null && user.getId() == 142) {
            final Image greenhouse = new Image("img/storm/storm_building.png");
            greenhouse.setPixelSize(1122, 793);
            final TabItem greenhouseItem = new TabItem("Greenhouse nodes");
            greenhouseItem.setLayout(new FitLayout());
            greenhouseItem.setClosable(true);
            greenhouseItem.add(greenhouse);
            this.tabPanel.add(greenhouseItem);
        }

        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));
        add(this.tabPanel, centerLayout);
    }

    /**
     * Creates a content panel for the time range selection, containing only a radio group and a
     * header.
     * 
     * @return the content panel
     * @see #getTimeRange()
     */
    public RadioGroup createTimeSelector() {

        final RadioGroup result = new RadioGroup();

        final Radio radio1Hr = new Radio();
        radio1Hr.setId("1hr");
        radio1Hr.setBoxLabel("1hr");

        final Radio radioDay = new Radio();
        radioDay.setId("24hr");
        radioDay.setBoxLabel("24hr");
        radioDay.setValue(true);

        final Radio radioWeek = new Radio();
        radioWeek.setId("1wk");
        radioWeek.setBoxLabel("1wk");

        final Radio radioMonth = new Radio();
        radioMonth.setId("4wk");
        radioMonth.setBoxLabel("4wk");

        result.add(radio1Hr);
        result.add(radioDay);
        result.add(radioWeek);
        result.add(radioMonth);
        result.setOriginalValue(radioDay);

        return result;
    }

    /**
     * Creates the "west" panel of the main BorderLayout. Contains the TreePanel with phones and
     * sensor, and the logout button.
     * 
     * @return the panel's LayoutContainer
     */
    private void createWestPanel() {

        final Image logo = new Image("/img/logo_sense-150.png");
        logo.setPixelSize(131, 68);
        logo.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                Log.d(TAG, "relative x: " + event.getRelativeX(logo.getElement()));
                Log.d(TAG, "relative y: " + event.getRelativeY(logo.getElement()));
            }
        });
        final LayoutContainer logoContainer = new LayoutContainer(new CenterLayout());
        logoContainer.setHeight(68);
        logoContainer.add(logo);

        final ContentPanel accordion = new ContentPanel(new AccordionLayout());
        accordion.setHeaderVisible(false);
        accordion.setBodyBorder(false);

        final LayoutContainer westPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
        westPanel.setScrollMode(Scroll.AUTOY);
        westPanel.setBorders(false);
        westPanel.add(logoContainer, new RowData(-1, -1, new Margins(10, 0, 0, 0)));
        westPanel.add(accordion, new RowData(1, 1, new Margins(10, 0, 0, 0)));

        final BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 275);
        westData.setMargins(new Margins(5));
        westData.setSplit(false);
        add(westPanel, westData);

        Dispatcher.forwardEvent(MySensorsEvents.ShowTree, accordion);
        Dispatcher.forwardEvent(GroupSensorsEvents.ShowTree, accordion);
        Dispatcher.forwardEvent(GroupEvents.ShowGrid, accordion);
        Dispatcher.forwardEvent(StateEvents.ShowGrid, accordion);
        Dispatcher.forwardEvent(BuildingEvents.ShowGrid, accordion);
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

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        this.setStyleAttribute("backgroundColor", "transparent");
        this.setLayout(new BorderLayout());

        createWestPanel();
        createCenterPanel();

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
        final TreeModel[] temp = new TreeModel[this.outstandingReqs.length - 1];
        System.arraycopy(this.outstandingReqs, 1, temp, 0, temp.length);
        this.outstandingReqs = temp;

        if (null != data) {
            Log.d(TAG, "Received sensor data from service!");

            final VisualizationTab charts = (VisualizationTab) this.unfinishedTab.getItem(0);
            charts.addData(data);
        } else {
            this.reqFailCount++;
        }

        // show the results or request more data if there are still tags left
        if (this.outstandingReqs.length > 0) {
            getSensorData(this.outstandingReqs[0]);
        } else {
            Log.d(TAG, "Finalizing visualization tab...");
            final VisualizationTab charts = (VisualizationTab) this.unfinishedTab.getItem(0);
            charts.setWaitingText(false);

            if (this.reqFailCount > 0) {
                String msg = "There was a problem getting some of the sensor data. Please try again.";
                MessageBox.alert("CommonSense", msg, null);
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

    public void resetTabs() {
        int tabCount = this.tabPanel.getItemCount();
        for (int i = tabCount; i > 3; i--) {
            this.tabPanel.remove(this.tabPanel.getItem(i - 1));
        }
        layout();
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

    public void showFeedback(ContentPanel feedbackPanel) {

        // add line chart tab item
        final TabItem item = new TabItem("Feedback");
        item.setLayout(new FitLayout());
        item.setClosable(true);
        item.add(feedbackPanel);
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
        this.unfinishedTab = item;
    }

    public void showLineChart(TreeModel[] sensors, long startTime, long endTime) {

        // add line chart tab item
        final TabItem item = new TabItem("Time line");
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

    public void showTable(TreeModel[] sensors) {

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
    private void startRequests(TreeModel[] tags, long startTime, long endTime) {

        // start requesting data for the list of tags
        this.outstandingReqs = tags;
        this.unfinishedTab = this.tabPanel.getSelectedItem();
        this.reqFailCount = 0;
        this.startTime = startTime;
        this.endTime = endTime;

        if (tags.length > 0) {
            getSensorData(tags[0]);
        }
    }

    public void removeFeedback() {
        this.tabPanel.remove(this.tabPanel.getSelectedItem());
    }

    public void showMap(ContentPanel mapPanel) {
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
}