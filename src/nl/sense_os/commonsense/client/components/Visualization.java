package nl.sense_os.commonsense.client.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import nl.sense_os.commonsense.client.components.grids.SensorDataGrid;
import nl.sense_os.commonsense.client.mvc.events.VizEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.UserModel;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.VisualizationUtils;

/**
 * Component with the visualization part of the web application.
 */
public class Visualization extends LayoutContainer {

    private static final String TAG = "Visualization";

    private TreeModel[] outstandingReqs;
    private int reqFailCount;
    public TreeStore<TreeModel> store = new TreeStore<TreeModel>();
    private TabPanel tabPanel;
    private TreePanel<TreeModel> tagTree;
    private RadioGroup timeSelector;
    private TabItem unfinishedTab;

    public Visualization() {

        // Load the visualization API, passing the onLoadCallback to be called when loading is done.
        final Runnable vizCallback = new Runnable() {

            @Override
            public void run() {
                onVisualizationLoad();
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback);

        this.setStyleAttribute("backgroundColor", "transparent");
    }

    /**
     * Creates the big "center" panel of the main BorderLayout. Contains only the tabPanel for the
     * sensor values.
     * 
     * @return the panel's LayoutContainer.
     */
    private Widget createCenterPanel() {

        // Welcome tab item
        final Frame welcomeFrame = new Frame("http://welcome.sense-os.nl/node/9");
        welcomeFrame.setStylePrimaryName("senseFrame");
        final TabItem welcomeItem = new TabItem("Welcome");
        welcomeItem.setLayout(new FitLayout());
        welcomeItem.add(welcomeFrame);

        // Tabs
        this.tabPanel = new TabPanel();
        this.tabPanel.setSize("100%", "100%");
        this.tabPanel.setPlain(true);
        this.tabPanel.addStyleName("transparent");
        this.tabPanel.add(welcomeItem);

        // Track trace
        UserModel user = Registry.get(Constants.REG_USER);
        if (user != null && user.getId() != 142) {
            final TabItem trackTraceItem = new TabItem("Track & Trace demo");
            trackTraceItem.setLayout(new FitLayout());
            trackTraceItem.setClosable(true);
            final Frame trackTrace = new Frame(
                    "http://almendetracker.appspot.com/?profileURL=http://demo.almende.com/tracker/ictdelta");
            trackTrace.setStylePrimaryName("senseFrame");
            trackTraceItem.add(trackTrace);
            this.tabPanel.add(trackTraceItem);
        }

        // add greenhouse building chart to please Freek
        if (user != null && user.getId() == 142) {
            final TabItem greenhouseItem = new TabItem("Greenhouse nodes");
            greenhouseItem.setLayout(new FitLayout());
            greenhouseItem.setClosable(true);
            final Image greenhouse = new Image("img/storm/storm_building.png");
            greenhouse.setPixelSize(1122, 793);
            greenhouseItem.add(greenhouse);
            this.tabPanel.add(greenhouseItem);
        }

        return tabPanel;
    }

    /**
     * Creates a dialog which asks for the desired action to take after the user drag and dropped
     * one or more tags from the tag tree. The dialog calls through to the proper follow-up method.
     * 
     * @param tags
     *            the tags that were dropped
     * @return the dialog
     * @see #onTagsDropped(ArrayList)
     */
    private Dialog createTabTypeDialog(final TreeModel[] tags) {
        final Dialog d = new Dialog();
        d.setHeading("CommonSense Web Application");
        d.setButtons("");
        d.setWidth(350);

        final ContentPanel panel = new ContentPanel();
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setHeaderVisible(false);
        panel.setSize(340, 100);
        panel.setBorders(false);
        panel.add(new Text("Please select the desired visualization type."), new FlowData(10));

        final Button lineChart = new Button("Line chart", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                d.hide();

                // add line chart tab item
                final TabItem item = new TabItem("Time line");
                item.setLayout(new FitLayout());
                item.setClosable(true);
                final VisualizationTab charts = new TimeLineCharts();
                charts.setWaitingText(true);
                item.add(charts);
                Visualization.this.tabPanel.add(item);
                Visualization.this.tabPanel.setSelection(item);
                Visualization.this.unfinishedTab = item;

                startRequests(tags);
            }
        });
        final Button table = new Button("Table", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                d.hide();

                // add table tab item
                final TabItem item = new TabItem("Table");
                item.setClosable(true);
                item.setScrollMode(Scroll.AUTO);
                item.setLayout(new FitLayout());
                Visualization.this.tabPanel.add(item);
                tabPanel.setSelection(item);

                // add sensor data grid
                item.add(new SensorDataGrid(tags), new FitData());
                item.layout();
            }
        });
        // final Button streetView = new Button("Street view", new SelectionListener<ButtonEvent>()
        // {
        //
        // @Override
        // public void componentSelected(ButtonEvent ce) {
        // d.hide();
        //
        // deviceLocationView(tags);
        // }
        // });
        // streetView.setEnabled(false);

        final ButtonBar buttons = new ButtonBar();
        buttons.setAlignment(HorizontalAlignment.CENTER);
        buttons.setMinButtonWidth(75);
        buttons.add(lineChart);
        buttons.add(table);
        // buttons.add(streetView);
        panel.setBottomComponent(buttons);

        d.add(panel);
        return d;
    }

    /**
     * Creates a tree of TagModels, which are fetched asynchronously. The TagModels represent users,
     * devices or sensor types.
     * 
     * @return the tree
     */
    private ContentPanel createTagPanel() {

        // trees store
        store.setKeyProvider(new ModelKeyProvider<TreeModel>() {

            @Override
            public String getKey(TreeModel model) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_GROUP) {
                    return "group " + model.<String> get("name");
                } else if (tagType == TagModel.TYPE_DEVICE) {
                    return "device " + model.<String> get("uuid");
                } else if (tagType == TagModel.TYPE_SENSOR) {
                    return "sensor " + model.<String> get("id");
                } else {
                    Log.e(TAG, "unexpected tag type in ModelKeyProvider");
                    return model.toString();
                }
            }
        });
        Comparator<Object> comparator = new Comparator<Object>() {

            @Override
            public int compare(Object obj1, Object obj2) {
                try {
                    TreeModel o1 = (TreeModel) obj1;
                    TreeModel o2 = (TreeModel) obj2;
                    int type1 = o1.<Integer> get("tagType");
                    int type2 = o2.<Integer> get("tagType");
                    if (type1 == type2 && type1 == TagModel.TYPE_SENSOR) {
                        String name1 = o1.<String> get("name");
                        String name2 = o2.<String> get("name");
                        return name1.compareToIgnoreCase(name2);
                    }
                    return 0;
                } catch (ClassCastException e) {
                    return 0;
                }
            }
        };
        StoreSorter<TreeModel> sorter = new StoreSorter<TreeModel>(comparator);
        store.setStoreSorter(sorter);

        this.tagTree = new TreePanel<TreeModel>(store);
        this.tagTree.setBorders(false);
        this.tagTree.setStateful(true);
        this.tagTree.setId("idNecessaryForStatefulSetting");
        this.tagTree.setLabelProvider(new ModelStringProvider<TreeModel>() {

            @Override
            public String getStringValue(TreeModel model, String property) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_GROUP) {
                    return model.<String> get("name");
                } else if (tagType == TagModel.TYPE_DEVICE) {
                    return model.<String> get("type");
                } else if (tagType == TagModel.TYPE_SENSOR) {
                    String name = model.<String> get("name");
                    String deviceType = model.<String> get("device_type");
                    if (name.equals(deviceType)) {
                        return name;
                    }
                    return name + " (" + deviceType + ")";
                } else {
                    Log.e(TAG, "unexpected tag type in ModelStringProvider");
                    return model.toString();
                }
            }
        });
        this.tagTree.setIconProvider(new ModelIconProvider<TreeModel>() {

            @Override
            public AbstractImagePrototype getIcon(TreeModel model) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_GROUP) {
                    return IconHelper.create("gxt/images/gxt/icons/folder.gif");
                } else if (tagType == TagModel.TYPE_DEVICE) {
                    return IconHelper.create("gxt/images/gxt/icons/folder.gif");
                } else if (tagType == TagModel.TYPE_SENSOR) {
                    return IconHelper.create("gxt/images/gxt/icons/tabs.gif");
                } else {
                    Log.e(TAG, "unexpected tag type in ModelIconProvider");
                    return IconHelper.create("gxt/images/gxt/icons/done.gif");
                }
            }
        });

        final ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeading("Devices and sensors");
        panel.setCollapsible(true);
        panel.add(this.tagTree);

        return panel;
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
    private LayoutContainer createWestPanel() {

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

        // Content panel with the tree of tags
        final ContentPanel tagPanel = createTagPanel();

        this.timeSelector = createTimeSelector();
        final ContentPanel timeRangePanel = new ContentPanel();
        timeRangePanel.setHeading("Time range");
        timeRangePanel.setCollapsible(true);
        timeRangePanel.add(this.timeSelector, new FlowData(0, 0, 0, 5));

        final LayoutContainer translucentPanel = new LayoutContainer(new RowLayout(
                Orientation.VERTICAL));
        translucentPanel.setScrollMode(Scroll.AUTOY);
        translucentPanel.add(logoContainer, new RowData(-1, -1, new Margins(10, 0, 0, 0)));
        translucentPanel.add(tagPanel, new RowData(1, 1, new Margins(10, 0, 0, 0)));
        translucentPanel.add(timeRangePanel, new RowData(1, -1, new Margins(10, 0, 0, 0)));
        translucentPanel.setBorders(false);

        return translucentPanel;
    }

    /**
     * Opens a Google street view tab.
     * 
     * @param tags
     *            the tags that were dropped. NB: only the first tag in the array is actually used.
     * @see GoogleStreetView
     */
    @SuppressWarnings("unused")
    private void deviceLocationView(TreeModel[] tags) {

        final UserModel user = Registry.get(Constants.REG_USER);
        if (null == user) {
            Log.e(TAG, "No user object in Registry");
            return;
        }

        final TreeModel tagModel = tags[0];

        final TabItem item = new TabItem("Street View");
        item.setLayout(new FitLayout());
        item.setClosable(true);
        // TODO fix streetview
        item.add(new GoogleStreetView(tagModel.<String> get("id"), user.getName(), null));
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
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
        requestEvent.setData("tag", tag);
        final long[] range = getTimeRange();
        requestEvent.setData("startDate", (range[0] / 1000d));
        requestEvent.setData("endDate", (range[1] / 1000d));
        Dispatcher.forwardEvent(requestEvent);
    }

    /**
     * Gets the time range from the radio buttons in the west panel.
     * 
     * @return array with start and end time in milliseconds.
     * @see #createTimeSelector()
     */
    private long[] getTimeRange() {

        // constants
        final long hour = 1000 * 60 * 60;
        final long day = 24 * hour;
        final long week = 7 * day;

        // read off selected time range
        long end = System.currentTimeMillis();
        UserModel user = Registry.get(Constants.REG_USER);
        if (null != user && user.getId() == 134) {
            Log.d(TAG, "delfgauw time hack");
            end = 1283603962000l; // 4 september, 14:39.220 CEST
        } else if (null != user && user.getId() == 142) {
            Log.d(TAG, "greenhouse time hack");
            end = 1288609200000l; // 2 november, 12:00 CET
        }
        long start = 0;
        final String radioId = this.timeSelector.getValue().getId();
        if (radioId.equals("1hr")) {
            start = end - hour;
        } else if (radioId.equals("6hr")) {
            start = end - (6 * hour);
        } else if (radioId.equals("24hr")) {
            start = end - day;
        } else if (radioId.equals("1wk")) {
            start = end - week;
        } else if (radioId.equals("4wk")) {
            start = end - (4 * week);
        } else {
            Log.w(TAG, "Unexpected time range: " + radioId);
        }

        return new long[] { start, end };
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
     * Handles a drag-drop event by displaying a dialog for the preferred action to take.
     * 
     * @param treeStoreModels
     *            list of dropped tags
     * @see #setupDragDrop()
     */
    private void onTagsDropped(ArrayList<TreeStoreModel> treeStoreModels) {

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

        // create array to send as parameter in RPC
        TreeModel[] tagsArray = new TreeModel[0];
        for (TreeModel tag : tags) {
            // final TagModel tag = (TagModel) tsm.getModel();
            int tagType = tag.<Integer> get("tagType");
            if (tagType == TagModel.TYPE_SENSOR) {
                final TreeModel[] temp = new TreeModel[tagsArray.length + 1];
                System.arraycopy(tagsArray, 0, temp, 0, tagsArray.length);
                temp[temp.length - 1] = tag;
                tagsArray = temp;
            } else {
                // do nothing
            }
        }

        // check whether there are any tags at all
        if (tagsArray.length == 0) {
            MessageBox.info("CommonSense Web Application",
                    "No sensor types or devices selected, nothing to display.", null);
            return;
        }

        final Dialog d = createTabTypeDialog(tagsArray);
        d.show();
    }

    /**
     * Shows the final layout after the Google Visualization API has been loaded.
     */
    private void onVisualizationLoad() {
        // layouts for the different panels
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST, 225);
        westLayout.setMargins(new Margins(5));
        westLayout.setSplit(false);
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));

        this.setLayout(new BorderLayout());
        this.add(createWestPanel(), westLayout);
        this.add(createCenterPanel(), centerLayout);

        setupDragDrop();

        layout();
    }

    /**
     * Sets up the tag tree panel and the tab panel for drag and drop of the tags.
     * 
     * @see #onTagsDropped(ArrayList)
     */
    private void setupDragDrop() {

        TreePanelDragSource source = new TreePanelDragSource(this.tagTree);
        source.setTreeStoreState(true);
        source.addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                final ArrayList<TreeStoreModel> data = e.<ArrayList<TreeStoreModel>> getData();
                onTagsDropped(data);
            }
        });
        final DropTarget dropTarget = new DropTarget(this.tabPanel);
        dropTarget.setOperation(Operation.COPY);
    }

    /**
     * Prepares for a series of RPC requests for data from a list of tags. Initializes some
     * constants and starts the first request with <code>requestSensorValues</code>.
     * 
     * @param tags
     *            the list of tagged sensors
     */
    private void startRequests(TreeModel[] tags) {
        // start requesting data for the list of tags
        this.outstandingReqs = tags;
        this.unfinishedTab = this.tabPanel.getSelectedItem();
        this.reqFailCount = 0;

        if (tags.length > 0) {
            getSensorData(tags[0]);
        }
    }
}