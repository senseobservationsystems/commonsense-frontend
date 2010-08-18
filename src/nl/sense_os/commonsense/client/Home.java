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
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.MotionChart;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.widgets.GroupSelection;
import nl.sense_os.commonsense.client.widgets.TimeLineCharts;
import nl.sense_os.commonsense.client.widgets.WelcomeTab;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;
import nl.sense_os.commonsense.dto.UserModel;

public class Home extends LayoutContainer {

    private static final String TAG = "Home";
    GroupSelection groupSelection = new GroupSelection();
    private final AsyncCallback<Void> mainCallback;
    private RadioGroup timeSelector;
    private final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);
    private TabPanel tabPanel;
    private TreePanel<TagModel> tagTree;
    private final UserModel user;
    private TagModel[] outstandingReqs;
    private List<TaggedDataModel> receivedData;
    private MessageBox progressBox;

    public Home(UserModel user, AsyncCallback<Void> callback) {
        this.mainCallback = callback;
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

        // west panel with controls
        final ContentPanel west = createWestPanel();
        final BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST, 225, 200, 300);
        westLayout.setMargins(new Margins(5));
        westLayout.setSplit(true);

        // center panel with content
        final TabPanel center = createCenterPanel();
        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(5));

        // main content panel containing the west and center panels
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeading("CommonSense Web Application");
        contentPanel.setHeaderVisible(true);
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setFrame(true);
        contentPanel.setCollapsible(false);

        contentPanel.add(west, westLayout);
        contentPanel.add(center, centerLayout);

        this.setLayout(new FitLayout());
        this.add(contentPanel);

        setupDragDrop();
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
        welcome.add(new WelcomeTab(this.user.getName()));
        welcome.setClosable(false);
        welcome.setScrollMode(Scroll.AUTO);

        // Tabs
        this.tabPanel = new TabPanel();
        this.tabPanel.setSize("100%", "100%");
        this.tabPanel.setPlain(true);
        this.tabPanel.add(welcome);

        return this.tabPanel;
    }

    public RadioGroup createTimeSelector() {

        final Radio radio1d = new Radio();
        radio1d.setId("1d");
        radio1d.setBoxLabel("1d");

        final Radio radio7d = new Radio();
        radio7d.setId("7d");
        radio7d.setBoxLabel("7d");
        radio7d.setValue(true);

        final Radio radio1m = new Radio();
        radio1m.setId("1m");
        radio1m.setBoxLabel("1m");

        final Radio radio3m = new Radio();
        radio3m.setId("3m");
        radio3m.setBoxLabel("3m");

        RadioGroup result = new RadioGroup();
        result.add(radio1d);
        result.add(radio7d);
        result.add(radio1m);
        result.add(radio3m);
        result.setOriginalValue(radio7d);

        return result;
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
                    TagModel tag = (TagModel) loadConfig;
                    service.getTags(tag.getPath(), callback);
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

        ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeading("Tag tree");
        panel.setCollapsible(true);
        panel.add(this.tagTree);

        return panel;
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
        ContentPanel tagPanel = createTagPanel();

        // Log out button with flexible white space above it
        final Button logoutBtn = new Button("Log out");
        logoutBtn.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent be) {
                Home.this.service.logout(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        Home.this.mainCallback.onFailure(ex);
                    }

                    @Override
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
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setScrollMode(Scroll.AUTOY);
        panel.add(logoContainer, new RowData(-1, -1, new Margins(10, 0, 0, 0)));
        panel.add(tagPanel, new RowData(1, 1, new Margins(10, 0, 0, 0)));
        panel.add(timeRangePanel, new RowData(1, -1, new Margins(10, 0, 0, 0)));
        panel.add(logoutBtn, new RowData(1, -1, new Margins(5, 5, 5, 5)));

        return panel;
    }

    private long[] getTimeRange() {

        // constants
        final long day = 1 * 24 * 60 * 60 * 1000;
        final long week = 7 * day;
        final long month = 31 * day;
        final long quarter = 3 * month;

        // read off selected time range
        final long end = System.currentTimeMillis();
        long start = 0;
        final String radioId = this.timeSelector.getValue().getId();
        if (radioId.equals("1d")) {
            start = end - day;
        } else if (radioId.equals("7d")) {
            start = end - week;
        } else if (radioId.equals("1m")) {
            start = end - month;
        } else if (radioId.equals("3m")) {
            start = end - quarter;
        } else {
            Log.w(TAG, "Unexpected time range: " + radioId);
        }

        return new long[] { start, end };
    }

    private void onSensorValuesReceived(TaggedDataModel data) {
        Log.d(TAG, "Received response from service!");        
        
        // remove the tag from outstandingReqs
        TagModel[] temp = new TagModel[this.outstandingReqs.length - 1];
        System.arraycopy(outstandingReqs, 1, temp, 0, temp.length);
        this.outstandingReqs = temp;        

        if ((null != data) && (data.getData().length > 0)) {
            this.receivedData.add(data);
            Log.d(TAG, "Added received data to the list");
        }

        if (this.outstandingReqs.length == 0) {
            if (this.progressBox.isVisible()) {
                this.progressBox.close();
            }

            if (this.receivedData.size() > 0) {
                Log.d(TAG, "Creating tab item");
                TabItem item = new TabItem("Time line");
                item.setLayout(new FitLayout());
                item.setClosable(true);
                item.add(new TimeLineCharts(this.receivedData));
                this.tabPanel.add(item);
                this.tabPanel.setSelection(item);
            } else {
                MessageBox.alert("CommonSense Web Application", "No data received from database",
                        null);
            }
        } else {
            Log.d(TAG, "New request for data: " + this.outstandingReqs[0].get("text"));
            
            AsyncCallback<TaggedDataModel> callback = new AsyncCallback<TaggedDataModel>() {

                @Override
                public void onFailure(Throwable caught) {                    
                    onSensorValuesReceived(null);
                }

                @Override
                public void onSuccess(TaggedDataModel data) {
                    onSensorValuesReceived(data);
                }
            };

            final long[] range = getTimeRange();
            final Timestamp start = new Timestamp(range[0]);
            final Timestamp end = new Timestamp(range[1]);
            this.service.getSensorValues(this.outstandingReqs[0], start, end, callback);        
        }
    }

    private void onTagsDropped(ArrayList<TreeStoreModel> treeStoreModel) {

        // create array to send as parameter in RPC
        TagModel[] tags = new TagModel[0];
        for (int i = 0; i < treeStoreModel.size(); i++) {
            TagModel tag = (TagModel) treeStoreModel.get(i).getModel();
            if (tag.getType() == TagModel.TYPE_SENSOR) {
                TagModel[] temp = new TagModel[tags.length + 1];
                System.arraycopy(tags, 0, temp, 0, tags.length);
                temp[temp.length-1] = tag;
                tags = temp;
            }
        }
        
        if (tags.length == 0) {
            MessageBox.info("CommonSense Web Application", "No sensor types selected, nothing to display.", null);
            return;
        }

        // select the Welcome tab
        this.tabPanel.setSelection(this.tabPanel.getItem(0));
        
        this.progressBox = MessageBox.progress("Please wait", "Requesting data...", "");
        this.progressBox.getProgressBar().auto();
        this.progressBox.show();

        AsyncCallback<TaggedDataModel> callback = new AsyncCallback<TaggedDataModel>() {

            @Override
            public void onFailure(Throwable caught) {                
                onSensorValuesReceived(null);
            }

            @Override
            public void onSuccess(TaggedDataModel data) {
                onSensorValuesReceived(data);
            }
        };

        final long[] range = getTimeRange();
        final Timestamp start = new Timestamp(range[0]);
        final Timestamp end = new Timestamp(range[1]);
        this.outstandingReqs = tags;
        this.receivedData = new ArrayList<TaggedDataModel>();        
        this.service.getSensorValues(tags[0], start, end, callback);        
    }

    private void setupDragDrop() {

        new TreePanelDragSource(this.tagTree);

        DropTarget dropTarget = new DropTarget(this.tabPanel);
        dropTarget.setOperation(Operation.COPY);
        dropTarget.addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                super.dragDrop(e);

                ArrayList<TreeStoreModel> data = e.<ArrayList<TreeStoreModel>> getData();
                onTagsDropped(data);
            }
        });
    }
}
