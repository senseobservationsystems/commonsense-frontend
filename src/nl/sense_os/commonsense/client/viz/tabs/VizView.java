package nl.sense_os.commonsense.client.viz.tabs;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.states.feedback.FeedbackEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.viz.panels.map.MapPanel;
import nl.sense_os.commonsense.client.viz.panels.table.SensorDataGrid;
import nl.sense_os.commonsense.client.viz.panels.timeline.TimeLinePanel;
import nl.sense_os.commonsense.shared.SensorModel;

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
import com.extjs.gxt.ui.client.widget.ContentPanel;
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

    private String createChartTitle(List<SensorModel> sensors) {
        String title = null;
        for (SensorModel sensor : sensors) {
            title = sensor.getDisplayName() + ", ";
        }

        // remove trailing ", "
        title = title.substring(0, title.length() - 2);

        // // trim to max length
        // if (title.length() > 18) {
        // title = title.substring(0, 15) + "...";
        // }
        return title;
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

        } else if (type.equals(FeedbackEvents.ShowFeedback)) {
            Log.d(TAG, "ShowFeedback");
            final ContentPanel panel = event.<ContentPanel> getData("panel");
            final String title = event.<String> getData("title");
            showFeedback(panel, title);

        } else if (type.equals(VizEvents.ShowTimeLine)) {
            // Log.d(TAG, "ShowTimeLine");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long startTime = event.getData("startTime");
            final long endTime = event.getData("endTime");
            showTimeLine(sensors, startTime, endTime);

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
        this.tabPanel.setMinTabWidth(120);
        this.tabPanel.setResizeTabs(true);
        this.tabPanel.addStyleName("transparent");

        // Welcome tab item
        final Frame welcomeFrame = new Frame("http://welcome.sense-os.nl/node/9");
        welcomeFrame.setStylePrimaryName("senseFrame");
        final TabItem welcomeItem = new TabItem("Welcome");
        welcomeItem.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "help.png"));
        welcomeItem.setLayout(new FitLayout());
        LayoutData data = new FitData(new Margins(0));
        welcomeItem.add(welcomeFrame, data);
        this.tabPanel.add(welcomeItem);

        // Track trace
        final Frame trackTrace = new Frame("http://almendetracker.appspot.com/?profileURL="
                + "http://demo.almende.com/tracker/ictdelta");
        trackTrace.setStylePrimaryName("senseFrame");
        final TabItem trackTraceItem = new TabItem("Demo: Track & Trace");
        trackTraceItem.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH
                + "sense_orange.gif"));
        trackTraceItem.setLayout(new FitLayout());
        trackTraceItem.add(trackTrace);
        this.tabPanel.add(trackTraceItem);

        // Humidity
        final Frame humid3d = new Frame(
                "http://demo.almende.com/links/storm/day_40_humid_animation.html");
        humid3d.setStylePrimaryName("senseFrame");
        final TabItem humid3dItem = new TabItem("Preview: 3D Chart");
        humid3dItem.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH
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
        List<SensorModel> sensors = new ArrayList<SensorModel>();
        for (TreeStoreModel tsm : treeStoreModels) {
            final TreeModel tag = (TreeModel) tsm.getModel();
            if (false == sensors.contains(tag)) {

                if (tag instanceof SensorModel) {
                    sensors.add((SensorModel) tag);
                } else {
                    // add any children
                    for (ModelData model : tsm.getChildren()) {
                        TreeStoreModel tm = (TreeStoreModel) model;
                        TreeModel child = (TreeModel) tm.getModel();
                        if (false == sensors.contains(child)) {
                            if (child instanceof SensorModel) {
                                sensors.add((SensorModel) child);
                            }
                        }
                    }
                }
            }
        }

        showTypeChoice(sensors);
    }

    private void showTypeChoice(List<SensorModel> sensors) {
        Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, sensors);
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
                Object data = e.getData();
                if (data instanceof List) {
                    Object listEntry = ((List<?>) data).get(0);
                    if (listEntry instanceof TreeStoreModel) {
                        @SuppressWarnings("unchecked")
                        List<TreeStoreModel> list = (List<TreeStoreModel>) data;
                        onTagsDropped(list);
                    } else if (listEntry instanceof SensorModel) {
                        @SuppressWarnings("unchecked")
                        List<SensorModel> list = (List<SensorModel>) data;
                        showTypeChoice(list);
                    } else {
                        Log.d(TAG, "Unknown list type: " + listEntry);
                    }
                } else {
                    Log.w(TAG, "Cannot handle dropped data: " + data);
                }
            }
        });
    }

    private void showFeedback(ContentPanel panel, String title) {

        // add feedback tab item
        final TabItem item = new TabItem(title);
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "setting_tools.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        item.add(panel);

        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
    }

    private void showMap(List<SensorModel> sensors, long startTime, long endTime) {

        // add map tab item
        String title = createChartTitle(sensors);
        final TabItem item = new TabItem(title);
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "map.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        MapPanel map = new MapPanel(sensors, startTime, endTime, title);
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
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "table.png"));
        item.setClosable(true);
        item.setScrollMode(Scroll.AUTO);
        item.setLayout(new FitLayout());
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);

        // add sensor data grid
        item.add(new SensorDataGrid(sensors), new FitData());
        item.layout();
    }

    private void showTimeLine(List<SensorModel> sensors, long startTime, long endTime) {

        // add line chart tab item
        String title = createChartTitle(sensors);
        final TabItem item = new TabItem(title);
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "chart.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        final TimeLinePanel chart = new TimeLinePanel(sensors, startTime, endTime, title);
        item.add(chart);

        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
    }
}
