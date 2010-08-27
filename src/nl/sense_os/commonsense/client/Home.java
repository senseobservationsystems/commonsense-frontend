package nl.sense_os.commonsense.client;

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
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
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
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.MotionChart;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.widgets.GoogleStreetView;
import nl.sense_os.commonsense.client.widgets.NoorderzonChart;
import nl.sense_os.commonsense.client.widgets.TimeLineCharts;
import nl.sense_os.commonsense.client.widgets.WelcomeTab;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.dto.exceptions.DbConnectionException;
import nl.sense_os.commonsense.dto.exceptions.TooMuchDataException;
import nl.sense_os.commonsense.dto.exceptions.WrongResponseException;
import nl.sense_os.commonsense.server.data.User;

public class Home extends LayoutContainer {

    private static boolean NOORDERZONMODE;
    private static final String TAG = "Home";
    private final AsyncCallback<Void> mainCallback;
    private TagModel[] outstandingReqs;
    private MessageBox progressBox;
    private List<TaggedDataModel> rxData;
    private int rxDbConnectionExceptions;
    private int rxTooMuchDataExceptions;
    private int rxWrongDataExceptions;
    private final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);
    private TabPanel tabPanel;
    private TreePanel<TagModel> tagTree;
    private RadioGroup timeSelector;

    public Home(UserModel user, AsyncCallback<Void> callback) {
        this.mainCallback = callback;
        NOORDERZONMODE = (user.getId() == 341);

        // Load the visualization API, passing the onLoadCallback to be called when loading is done.
        final Runnable vizCallback = new Runnable() {

            public void run() {
                Log.d(TAG, "Visualization loaded...");
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback, AnnotatedTimeLine.PACKAGE,
                MotionChart.PACKAGE);

        // west panel with controls
        final ContentPanel west = new ContentPanel(new FitLayout());
        west.setHeaderVisible(false);
        west.setBodyStyle("background:url('img/bg/left_bot_corner.png') no-repeat bottom left;");
        west.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.7)");
        west.add(createWestPanel(), new FitData(5));
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST, 225);
        westLayout.setMargins(new Margins(0));
        westLayout.setSplit(false);

        // center panel with content
        final ContentPanel center = new ContentPanel(new FitLayout());
        center.setHeaderVisible(false);
        center.setBodyStyle("background:url('img/bg/right_top_pre.png') no-repeat top right;");
        final ContentPanel center2 = new ContentPanel(new FitLayout());
        center2.setHeaderVisible(false);
        center2.setBodyStyle("background:url('img/bg/left_bot.png') no-repeat bottom left;");
        center2.add(createCenterPanel(), new FitData(5));
        center.add(center2, new FitData(0));
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(0));

        // main content panel containing the west and center panels
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeading("CommonSense Web Application");
        contentPanel.setHeaderVisible(false);
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setFrame(true);
        contentPanel.setCollapsible(false);

        contentPanel.add(west, westLayout);
        contentPanel.add(center, centerLayout);

        // contentPanel.setStyleAttribute("backgroundColor", "white");
        setLayout(new FitLayout());
        this.add(contentPanel);

        setupDragDrop();
    }

    private boolean addChartTab() {

        Log.d(TAG, "Creating tab item");
        final TabItem item = new TabItem("Time line");
        item.setLayout(new FitLayout());
        item.setClosable(true);
        if (NOORDERZONMODE) {
            item.add(new NoorderzonChart(this.rxData));
        } else {
            item.add(new TimeLineCharts(this.rxData));
        }
        item.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.7)");
        this.tabPanel.add(item);
        this.tabPanel.setSelection(item);

        return true;
    }

    /**
     * Creates the "center" panel of the main BorderLayout. Contains only the tabPanel for the
     * sensor values.
     * 
     * @return the panel's LayoutContainer.
     */
    private TabPanel createCenterPanel() {

        // Welcome tab item
        final TabItem welcome = new TabItem("Welcome");
        welcome.setLayout(new FitLayout());
        welcome.add(new WelcomeTab());
        welcome.setClosable(false);
        welcome.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.7)");

        // Tabs
        this.tabPanel = new TabPanel();
        this.tabPanel.setSize("100%", "100%");
        this.tabPanel.setPlain(true);
        this.tabPanel.addStyleName("transparent");
        this.tabPanel.add(welcome);

        return this.tabPanel;
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
        panel.setCollapsible(true);
        panel.add(this.tagTree);

        return panel;
    }

    public RadioGroup createTimeSelector() {

        final RadioGroup result = new RadioGroup();

        if (NOORDERZONMODE) {
            final Radio radio1Hr = new Radio();
            radio1Hr.setId("1hr");
            radio1Hr.setBoxLabel("1hr");
            radio1Hr.setValue(true);

            final Radio radio6Hr = new Radio();
            radio6Hr.setId("6hr");
            radio6Hr.setBoxLabel("6hr");

            final Radio radioDay = new Radio();
            radioDay.setId("24hr");
            radioDay.setBoxLabel("24hr");

            final Radio radioWeek = new Radio();
            radioWeek.setId("1wk");
            radioWeek.setBoxLabel("week");

            result.add(radio1Hr);
            result.add(radio6Hr);
            result.add(radioDay);
            result.add(radioWeek);
            result.setOriginalValue(radio1Hr);
        } else {
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
        }

        return result;
    }

    /**
     * Creates the "west" panel of the main BorderLayout. Contains the TreePanel with phones and
     * sensor, and the logout button.
     * 
     * @return the panel's LayoutContainer
     */
    private ContentPanel createWestPanel() {

        final Image logo = new Image("/img/logo_sense-150.png");
        // logo.setSize("131", "68");
        logo.setPixelSize(131, 68);
        final ContentPanel logoContainer = new ContentPanel(new CenterLayout());
        logoContainer.setHeaderVisible(false);
        logoContainer.setHeight(68);
        logoContainer.add(logo);

        // Content panel with the tree of tags
        final ContentPanel tagPanel = createTagPanel();

        // Log out button with flexible white space above it
        final Button logoutBtn = new Button("Log out");
        logoutBtn.addListener(Events.Select, new Listener<ButtonEvent>() {
            public void handleEvent(ButtonEvent be) {
                Home.this.service.logout(new AsyncCallback<Void>() {
                    public void onFailure(Throwable ex) {
                        Home.this.mainCallback.onFailure(ex);
                    }

                    public void onSuccess(Void result) {
                        Home.this.mainCallback.onSuccess(null);
                    }
                });
            }
        });

        this.timeSelector = createTimeSelector();
        final ContentPanel timeRangePanel = new ContentPanel();
        timeRangePanel.setHeading("Time range");
        timeRangePanel.setCollapsible(true);
        timeRangePanel.add(this.timeSelector, new FlowData(0, 0, 0, 5));

        final ContentPanel panel = new ContentPanel(new RowLayout(Orientation.VERTICAL));
        panel.setHeaderVisible(false);
        panel.setBorders(true);
        panel.setScrollMode(Scroll.AUTOY);
        panel.add(logoContainer, new RowData(-1, -1, new Margins(10, 0, 0, 0)));
        panel.add(tagPanel, new RowData(1, 1, new Margins(10, 0, 0, 0)));
        panel.add(timeRangePanel, new RowData(1, -1, new Margins(10, 0, 0, 0)));
        panel.add(logoutBtn, new RowData(1, -1, new Margins(5, 5, 5, 5)));
        panel.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.7)");

        return panel;
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
            this.rxData.add(data);
        }

        // show the results or request more data if there are still tags left
        if (this.outstandingReqs.length == 0) {

            if (this.progressBox.isVisible()) {
                this.progressBox.close();
            }

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

            if (this.rxData.size() > 0) {
                addChartTab();
            }
        } else {
            requestSensorValues(this.outstandingReqs[0]);
        }
    }

    private void onTagsDropped(ArrayList<TreeStoreModel> treeStoreModel) {

        // create array to send as parameter in RPC
        TagModel[] tags = new TagModel[0];
        TagModel[] deviceTags = new TagModel[0];
        for (int i = 0; i < treeStoreModel.size(); i++) {
            final TagModel tag = (TagModel) treeStoreModel.get(i).getModel();
            if (tag.getType() == TagModel.TYPE_SENSOR) {
                final TagModel[] temp = new TagModel[tags.length + 1];
                System.arraycopy(tags, 0, temp, 0, tags.length);
                temp[temp.length - 1] = tag;
                tags = temp;
            }
            if (tag.getType() == TagModel.TYPE_DEVICE) {
                final TagModel[] temp = new TagModel[tags.length + 1];
                System.arraycopy(tags, 0, temp, 0, tags.length);
                temp[temp.length - 1] = tag;
                deviceTags = temp;
            }
        }

        // check whether there are any tags at all
        if (tags.length == 0 && deviceTags.length == 0) {
            MessageBox.info("CommonSense Web Application",
                    "No sensor types or devices selected, nothing to display.", null);
            return;
        }

        // select the Welcome tab
        this.tabPanel.setSelection(this.tabPanel.getItem(0));

        // show message to indicate progress
        this.progressBox = MessageBox.progress("Please wait", "Requesting data...", "");
        this.progressBox.getProgressBar().auto();
        this.progressBox.show();

        // start requesting data for the list of tags
        this.outstandingReqs = tags;
        this.rxData = new ArrayList<TaggedDataModel>();
        this.rxWrongDataExceptions = 0; 
        
        if(deviceTags.length > 0)
        	deviceLocationView(deviceTags);
        if(tags.length > 0)
        	requestSensorValues(tags[0]);
    }

    private void deviceLocationView(TagModel[] tags)
    {    	  	
    	for (int i = 0; i < tags.length; i++) {
			TagModel tagModel = tags[i];				
		
    	  if (this.progressBox.isVisible())
              this.progressBox.close();
    	  
    	  Log.d(TAG, "Creating tab item");
          final TabItem item = new TabItem("Google Street View");
          item.setLayout(new FitLayout());
          item.setClosable(true);
          item.add(new GoogleStreetView(tagModel.getTaggedId(), Cookies.getCookie("user_name"), Cookies.getCookie("user_pass")));          
          item.setStyleAttribute("backgroundColor", "rgba(255,255,255,0.7)");
          this.tabPanel.add(item);
          this.tabPanel.setSelection(item);
    	}
          
    }
    private void requestSensorValues(TagModel tag) {
        Log.d(TAG, "New request for data: " + tag.get("text"));

        final AsyncCallback<TaggedDataModel> callback = new AsyncCallback<TaggedDataModel>() {

            public void onFailure(Throwable caught) {

                if (caught instanceof TooMuchDataException) {
                    Home.this.rxTooMuchDataExceptions++;
                    Log.d(TAG, "Too much data requested: " + caught.getMessage());
                } else if (caught instanceof WrongResponseException) {
                    Home.this.rxWrongDataExceptions++;
                    Log.d(TAG, "Problem with received response: " + caught.getMessage());
                } else if (caught instanceof DbConnectionException) {
                    Home.this.rxDbConnectionExceptions++;
                    Log.d(TAG, "Error in connection to database: " + caught.getMessage());
                } else {
                    Log.d(TAG, "Generic exception: " + caught.getMessage());
                }

                onSensorValuesReceived(null);
            }

            public void onSuccess(TaggedDataModel data) {
                onSensorValuesReceived(data);
            }
        };

        final long[] range = getTimeRange();
        final Timestamp start = new Timestamp(range[0]);
        final Timestamp end = new Timestamp(range[1]);
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
}