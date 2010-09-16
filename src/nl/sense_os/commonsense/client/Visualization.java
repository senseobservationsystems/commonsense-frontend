package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
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
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.MotionChart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.widgets.GoogleStreetView;
import nl.sense_os.commonsense.client.widgets.GridTab;
import nl.sense_os.commonsense.client.widgets.TimeLineCharts;
import nl.sense_os.commonsense.client.widgets.VisualizationTab;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.dto.exceptions.DbConnectionException;
import nl.sense_os.commonsense.dto.exceptions.TooMuchDataException;
import nl.sense_os.commonsense.dto.exceptions.WrongResponseException;

public class Visualization extends LayoutContainer {

    private static final String TAG = "Home";
    private TagModel[] outstandingReqs;
    private int rxDbConnectionExceptions;
    private int rxTooMuchDataExceptions;
    private int rxWrongDataExceptions;
    private final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);
    private Date startTime;
    private TabPanel tabPanel;
    private TreePanel<TagModel> tagTree;
    private RadioGroup timeSelector;
    private TabItem unfinishedTab;
    private final UserModel user;

    public Visualization(UserModel user) {
        this.user = user;

        // Load the visualization API, passing the onLoadCallback to be called when loading is done.
        final Runnable vizCallback = new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Visualization loaded...");
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback, AnnotatedTimeLine.PACKAGE,
                MotionChart.PACKAGE);

        // layouts for the different panels 
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST, 225);
        westLayout.setMargins(new Margins(5));
        westLayout.setSplit(false);
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));
        
        this.setLayout(new BorderLayout());
        this.add(createWestPanel(), westLayout);
        this.add(createCenterPanel(), centerLayout);
        this.setStyleAttribute("backgroundColor", "rgba(0,0,0,0)");
        
        setupDragDrop();
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
        welcomeItem.setStyleAttribute("backgroundColor", "transparent");
        welcomeItem.add(welcomeFrame);
        
        // Tabs
        this.tabPanel = new TabPanel();
        this.tabPanel.setSize("100%", "100%");
        this.tabPanel.setPlain(true);
        this.tabPanel.addStyleName("transparent");
        this.tabPanel.add(welcomeItem);
        
        return tabPanel;
    }

    private Dialog createTabTypeDialog(final TagModel[] tags) {
        final Dialog d = new Dialog();
        d.setHeading("CommonSense Web Application");
        d.setButtons("");
        d.setWidth(350);

        final ContentPanel panel = new ContentPanel();
        panel.setHeaderVisible(false);
        panel.setSize(340, 100);
        panel.setBorders(false);
        panel.add(new Text("Please select the desired visualization type."), new FlowData(10));

        final ButtonBar buttons = new ButtonBar();
        buttons.setAlignment(HorizontalAlignment.CENTER);
        buttons.setMinButtonWidth(75);
        buttons.add(new Button("Line chart", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                d.hide();

                // add line chart tab item
                final TabItem item = new TabItem("Time line");
                item.setLayout(new FitLayout());
                item.setClosable(true);
                item.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.7)");
                final VisualizationTab charts = new TimeLineCharts();
                charts.setWaitingText(true);
                item.add(charts);
                Visualization.this.tabPanel.add(item);
                Visualization.this.tabPanel.setSelection(item);
                Visualization.this.unfinishedTab = item;

                startRequests(tags);
            }
        }));
        buttons.add(new Button("Table", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                d.hide();

                // add table tab item
                final TabItem item = new TabItem("Table");
                item.setLayout(new FitLayout());
                item.setClosable(true);
                item.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.7)");
                final VisualizationTab charts = new GridTab();
                charts.setWaitingText(true);
                item.add(charts);
                Visualization.this.tabPanel.add(item);
                Visualization.this.tabPanel.setSelection(item);
                Visualization.this.unfinishedTab = item;

                startRequests(tags);
            }
        }));
        buttons.add(new Button("Street view", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                d.hide();

                deviceLocationView(tags);
            }
        }));
        buttons.add(new Button("Speed test", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                d.hide();

                Visualization.this.speedTestCounter = 0;
                testSpeed();
            }
        }));

        panel.setBottomComponent(buttons);

        d.add(panel);
        return d;
    }

    /**
     * Creates an tree of PhoneModels and SensorModels, which are fetched asynchronously.
     * 
     * @return the tree
     */
    private ContentPanel createTagPanel() {

        final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);

        // data proxy
        final RpcProxy<List<TagModel>> proxy = new RpcProxy<List<TagModel>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<TagModel>> callback) {

                if (loadConfig == null) {
                    service.getTags(null, callback);
                } else if (loadConfig instanceof TagModel) {
                    final TagModel tag = (TagModel) loadConfig;
                    service.getTags(tag, callback);
                } else {
                    Log.e("RpcProxy", "loadConfig unexpected type");
                }
            }
        };

        // tree loader
        final TreeLoader<TagModel> loader = new BaseTreeLoader<TagModel>(proxy) {
            @Override
            public boolean hasChildren(TagModel parent) {
                return (parent.getType() != TagModel.TYPE_SENSOR);
            }
        };

        // trees store
        final TreeStore<TagModel> store = new TreeStore<TagModel>(loader);
        store.setKeyProvider(new ModelKeyProvider<TagModel>() {
            @Override
            public String getKey(TagModel tag) {
                return tag.getPath();
            }
        });

        this.tagTree = new TreePanel<TagModel>(store);
        this.tagTree.setBorders(false);
        this.tagTree.setStateful(true);
        this.tagTree.setId("idNecessaryForStatefulSetting");
        this.tagTree.setDisplayProperty("text");
        this.tagTree.getStyle().setLeafIcon(IconHelper.create("gxt/images/default/tree/leaf.gif"));

        final ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeading("Tag tree");
        panel.setBodyStyle("backgroundColor: transparent");
        panel.setCollapsible(true);
        panel.add(this.tagTree);

        return panel;
    }

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
        final LayoutContainer logoContainer = new LayoutContainer(new CenterLayout());
        logoContainer.setHeight(68);
        logoContainer.add(logo);

        // Content panel with the tree of tags
        final ContentPanel tagPanel = createTagPanel();
        
        // Log out button with flexible white space above it
        final Button trackTraceBtn = new Button("Track & Trace");
        trackTraceBtn.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent be) {
                final TabItem trackTraceItem = new TabItem("Track & Trace");
                trackTraceItem.setLayout(new FitLayout());
                trackTraceItem.setClosable(true);
                trackTraceItem.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.7)");
                final Frame welcomeFrame = new Frame("http://almendetracker.appspot.com/?profileURL=http://demo.almende.com/tracker/ictdelta");
                welcomeFrame.setStylePrimaryName("senseFrame");
                trackTraceItem.add(welcomeFrame);
                tabPanel.add(trackTraceItem);
                tabPanel.setSelection(trackTraceItem);
            }
        });

        this.timeSelector = createTimeSelector();
        final ContentPanel timeRangePanel = new ContentPanel();
        timeRangePanel.setBodyStyle("backgroundColor: transparent");
        timeRangePanel.setHeading("Time range");
        timeRangePanel.setCollapsible(true);
        timeRangePanel.add(this.timeSelector, new FlowData(0, 0, 0, 5));

        final LayoutContainer translucentPanel = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
        translucentPanel.setScrollMode(Scroll.AUTOY);
        translucentPanel.add(logoContainer, new RowData(-1, -1, new Margins(10, 0, 0, 0)));
        translucentPanel.add(tagPanel, new RowData(1, 1, new Margins(10, 0, 0, 0)));
        translucentPanel.add(timeRangePanel, new RowData(1, -1, new Margins(10, 0, 0, 0)));        
        translucentPanel.add(trackTraceBtn, new RowData(1, -1, new Margins(5, 5, 5, 5)));
        translucentPanel.setStyleAttribute("backgroundColor", "rgba(255,0,255,0)");
        translucentPanel.setBorders(false);
        
        return translucentPanel;
    }

    private void deviceLocationView(TagModel[] tags) {
        // for (int i = 0; i < tags.length; i++) {
        // TagModel tagModel = tags[i];
        final TagModel tagModel = tags[0];

        final TabItem item = new TabItem("Street View");
        item.setLayout(new FitLayout());
        item.setClosable(true);
        item.add(new GoogleStreetView(tagModel.getParentId(), this.user.getName(), this.user
                .getPassword()));
        item.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.7)");
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);
        // }
    }

    private long[] getTimeRange() {

        // constants
        final long hour = 1000 * 60 * 60;
        final long day = 24 * hour;
        final long week = 7 * day;

        // read off selected time range
        final long end = System.currentTimeMillis();
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

    private void onSensorValuesReceived(TaggedDataModel data) {

        // remove the tag from outstandingReqs
        final TagModel[] temp = new TagModel[this.outstandingReqs.length - 1];
        System.arraycopy(this.outstandingReqs, 1, temp, 0, temp.length);
        this.outstandingReqs = temp;

        if (null != data) {
            Log.d(TAG, "Received sensor data from service!");

            final VisualizationTab charts = (VisualizationTab) this.unfinishedTab.getItem(0);
            charts.addData(data);
        }

        // show the results or request more data if there are still tags left
        if (this.outstandingReqs.length > 0) {
            requestSensorValues(this.outstandingReqs[0]);
        } else {

            final VisualizationTab charts = (VisualizationTab) this.unfinishedTab.getItem(0);
            charts.setWaitingText(false);

            String errorMsg = "Not all data was received.\n";
            if (this.rxDbConnectionExceptions > 0) {
                errorMsg += "Connection to the database failed for "
                        + this.rxDbConnectionExceptions + " tag(s).\n";
            }
            if (this.rxTooMuchDataExceptions > 0) {
                errorMsg += "Too much data requested for " + this.rxTooMuchDataExceptions
                        + " tag(s).\n";
            }
            if (this.rxWrongDataExceptions > 0) {
                errorMsg += "Unexpected data received from the database for "
                        + this.rxWrongDataExceptions + " tag(s).";
            }
            if (errorMsg.length() > 30) {
                MessageBox.alert("CommonSense Web Application", errorMsg, null);
            }

        }
    }

    private void onTagsDropped(ArrayList<TreeStoreModel> treeStoreModel) {

        // create array to send as parameter in RPC
        TagModel[] tags = new TagModel[0];
        for (int i = 0; i < treeStoreModel.size(); i++) {
            final TagModel tag = (TagModel) treeStoreModel.get(i).getModel();
            if (tag.getType() == TagModel.TYPE_SENSOR) {
                final TagModel[] temp = new TagModel[tags.length + 1];
                System.arraycopy(tags, 0, temp, 0, tags.length);
                temp[temp.length - 1] = tag;
                tags = temp;
            }
        }

        // check whether there are any tags at all
        if (tags.length == 0) {
            MessageBox.info("CommonSense Web Application",
                    "No sensor types or devices selected, nothing to display.", null);
            return;
        }

        final Dialog d = createTabTypeDialog(tags);
        d.show();
    }

    private int speedTestCounter;

    private void requestSensorValues(TagModel tag) {
        Log.d(TAG, "New request for data: " + tag.get("text"));

        final AsyncCallback<TaggedDataModel> callback = new AsyncCallback<TaggedDataModel>() {

            @Override
            public void onFailure(Throwable caught) {

                if (caught instanceof TooMuchDataException) {
                    Visualization.this.rxTooMuchDataExceptions++;
                    Log.d(TAG, "Too much data requested: " + caught.getMessage());
                } else if (caught instanceof WrongResponseException) {
                    Visualization.this.rxWrongDataExceptions++;
                    Log.d(TAG, "Problem with received response: " + caught.getMessage());
                } else if (caught instanceof DbConnectionException) {
                    Visualization.this.rxDbConnectionExceptions++;
                    Log.d(TAG, "Error in connection to database: " + caught.getMessage());
                } else {
                    Log.d(TAG, "Generic exception: " + caught.getMessage());
                }

                onSensorValuesReceived(null);
            }

            @Override
            public void onSuccess(TaggedDataModel data) {

                onSensorValuesReceived(data);
            }
        };

        // reset error counters
        this.rxDbConnectionExceptions = 0;
        this.rxTooMuchDataExceptions = 0;
        this.rxWrongDataExceptions = 0;

        final long[] range = getTimeRange();
        final Date start = new Date(range[0]);
        final Date end = new Date(range[1]);
        this.service.getSensorValues(tag, start, end, callback);
    }

    private void setupDragDrop() {

        new TreePanelDragSource(this.tagTree);

        final DropTarget dropTarget = new DropTarget(this.tabPanel);
        dropTarget.setOperation(Operation.COPY);
        dropTarget.addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                super.dragDrop(e);

                final ArrayList<TreeStoreModel> data = e.<ArrayList<TreeStoreModel>> getData();
                onTagsDropped(data);
            }
        });
    }

    private void startRequests(TagModel[] tags) {
        // start requesting data for the list of tags
        this.outstandingReqs = tags;
        this.unfinishedTab = this.tabPanel.getSelectedItem();
        this.rxWrongDataExceptions = 0;
        this.rxDbConnectionExceptions = 0;
        this.rxTooMuchDataExceptions = 0;

        if (tags.length > 0) {
            requestSensorValues(tags[0]);
        }
    }
    private long speedNivo;
    private long speedIvo;

    private void testSpeed() {
        final AsyncCallback<TaggedDataModel> callback = new AsyncCallback<TaggedDataModel>() {

            @Override
            public void onFailure(Throwable caught) {

                if (caught instanceof TooMuchDataException) {
                    Visualization.this.rxTooMuchDataExceptions++;
                    Log.d(TAG, "Too much data requested: " + caught.getMessage());
                } else if (caught instanceof WrongResponseException) {
                    Visualization.this.rxWrongDataExceptions++;
                    Log.d(TAG, "Problem with received response: " + caught.getMessage());
                } else if (caught instanceof DbConnectionException) {
                    Visualization.this.rxDbConnectionExceptions++;
                    Log.d(TAG, "Error in connection to database: " + caught.getMessage());
                } else {
                    Log.d(TAG, "Generic exception: " + caught.getMessage());
                }

                // for speed testing:
                Date delay = new Date(new Date().getTime() - Visualization.this.startTime.getTime());
                Info.display("Speed data", "Request failed after "
                        + DateTimeFormat.getFormat("m:ss.SSS").format(delay) + " secs");
                
                if(Visualization.this.speedTestCounter <= 10) {
                    Visualization.this.speedNivo += delay.getTime();
                } else {
                    Visualization.this.speedIvo += delay.getTime();
                }
                testSpeed();
            }

            @Override
            public void onSuccess(TaggedDataModel data) {

                // for speed testing:
                Date delay = new Date(new Date().getTime() - Visualization.this.startTime.getTime());
                Info.display("Speed data", "Request took "
                        + DateTimeFormat.getFormat("m:ss.SSS").format(delay) + " secs");

                if(Visualization.this.speedTestCounter <= 10) {
                    Visualization.this.speedNivo += delay.getTime();
                } else {
                    Visualization.this.speedIvo += delay.getTime();
                }
                
                testSpeed();
            }
        };

        // reset error counters
        this.rxDbConnectionExceptions = 0;
        this.rxTooMuchDataExceptions = 0;
        this.rxWrongDataExceptions = 0;

        final Date start = new Date(1280620800000L); // new Date(range[0]);
        final Date end = new Date(1283299200000L); // new Date(range[1]);
        TagModel tag = new TagModel("/78/Nexus One/noise_sensor/", 1, 12, SensorValueModel.FLOAT);

        if (this.speedTestCounter < 10) {
            this.startTime = new Date();
            this.service.getSensorValues(tag, start, end, callback);
            this.speedTestCounter++;
        } else if (this.speedTestCounter < 20){
            this.startTime = new Date();
            this.service.getIvoSensorValues(tag, start, end, callback);
            this.speedTestCounter++;
        } else {
            Date nivo = new Date(this.speedNivo / 10);
            Date ivo = new Date(this.speedIvo / 10);
            DateTimeFormat dtf = DateTimeFormat.getFormat("s.SSS");
            String msg = "non-IVO avg: " + dtf.format(nivo) + " sec. \n\nIVO avg: " + dtf.format(ivo) + " sec.";
            MessageBox.info("IVO vs. non-IVO", msg, null);
            this.speedTestCounter = 0;
        }
    }
}