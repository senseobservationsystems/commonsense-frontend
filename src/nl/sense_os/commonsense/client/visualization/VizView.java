package nl.sense_os.commonsense.client.visualization;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.states.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.visualization.components.MapPanel;
import nl.sense_os.commonsense.client.visualization.components.SensorDataGrid;
import nl.sense_os.commonsense.client.visualization.components.TimeLinePanel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.TagModel;

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
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.google.gwt.user.client.ui.Frame;

public class VizView extends View {

    private static final String TAG = "VizView";
    private TabPanel tabPanel;

    public VizView(Controller controller) {
        super(controller);
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
            final long startTime = event.getData("startTime");
            final long endTime = event.getData("endTime");
            showLineChart(sensors, startTime, endTime);

        } else if (type.equals(VizEvents.ShowTable)) {
            // Log.d(TAG, "ShowTable");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            showTable(sensors);

        } else if (type.equals(VizEvents.ShowMap)) {
            // Log.d(TAG, "ShowMap");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long startTime = event.getData("startTime");
            final long endTime = event.getData("endTime");
            showMap(sensors, startTime, endTime);

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
        welcomeItem.setIcon(IconHelper.create(SensorIconProvider.SENSE_ICONS_PATH + "help.png"));
        welcomeItem.setLayout(new FitLayout());
        LayoutData data = new FitData(new Margins(0));
        welcomeItem.add(welcomeFrame, data);
        this.tabPanel.add(welcomeItem);

        // Track trace
        final Frame trackTrace = new Frame("http://almendetracker.appspot.com/?profileURL="
                + "http://demo.almende.com/tracker/ictdelta");
        trackTrace.setStylePrimaryName("senseFrame");
        final TabItem trackTraceItem = new TabItem("Demo: Track & Trace");
        trackTraceItem.setIcon(IconHelper.create(SensorIconProvider.SENSE_ICONS_PATH
                + "sense_orange.gif"));
        trackTraceItem.setLayout(new FitLayout());
        trackTraceItem.add(trackTrace);
        this.tabPanel.add(trackTraceItem);

        // Humidity
        final Frame humid3d = new Frame(
                "http://demo.almende.com/links/storm/day_40_humid_animation.html");
        humid3d.setStylePrimaryName("senseFrame");
        final TabItem humid3dItem = new TabItem("Preview: 3D Chart");
        humid3dItem.setIcon(IconHelper.create(SensorIconProvider.SENSE_ICONS_PATH
                + "sense_orange.gif"));
        humid3dItem.setLayout(new FitLayout());
        humid3dItem.add(humid3d);
        this.tabPanel.add(humid3dItem);
    }

    private void onLoggedOut(AppEvent event) {
        resetTabs();
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

    private void removeFeedback() {
        // TODO this ain't right
        this.tabPanel.remove(this.tabPanel.getSelectedItem());
    }

    private void resetTabs() {
        for (TabItem items : tabPanel.getItems()) {
            if (items.isClosable()) {
                this.tabPanel.remove(items);
            }
        }
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

    private void showFeedback(Component feedbackPanel) {

        // add line chart tab item
        final TabItem item = new TabItem("Feedback");
        item.setIcon(IconHelper.create(SensorIconProvider.SENSE_ICONS_PATH + "sense_magenta.gif"));
        item.setLayout(new FitLayout());
        item.setClosable(true);
        item.add(feedbackPanel);
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
    }

    private String createChartTitle(List<SensorModel> sensors) {
        String title = null;
        for (SensorModel sensor : sensors) {
            title = sensor.<String> get("text") + ", ";
        }

        // remove trailing ", "
        title = title.substring(0, title.length() - 2);

        // trim to max length
        if (title.length() > 18) {
            title = title.substring(0, 15) + "...";
        }
        return title;
    }

    private void showLineChart(List<SensorModel> sensors, long startTime, long endTime) {

        // add line chart tab item
        final TabItem item = new TabItem(createChartTitle(sensors));
        item.setIcon(IconHelper.create(SensorIconProvider.SENSE_ICONS_PATH + "chart.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        final TimeLinePanel charts = new TimeLinePanel(sensors, startTime, endTime);
        item.add(charts);

        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
    }

    private void showMap(List<SensorModel> sensors, long startTime, long endTime) {

        // add map tab item
        final TabItem item = new TabItem(createChartTitle(sensors));
        item.setIcon(IconHelper.create(SensorIconProvider.SENSE_ICONS_PATH + "map.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        MapPanel map = new MapPanel(sensors, startTime, endTime);
        item.add(map);

        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(this.tabPanel);
            parent.layout();
        } else {
            Log.e(TAG, "Failed to show visualization panel: parent=null");
        }
    }

    private void showTable(List<SensorModel> sensors) {

        // add table tab item
        final TabItem item = new TabItem(createChartTitle(sensors));
        item.setIcon(IconHelper.create(SensorIconProvider.SENSE_ICONS_PATH + "table.png"));
        item.setClosable(true);
        item.setScrollMode(Scroll.AUTO);
        item.setLayout(new FitLayout());
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);

        // add sensor data grid
        item.add(new SensorDataGrid(sensors), new FitData());
        item.layout();
    }
}
