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
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
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
import nl.sense_os.commonsense.client.widgets.LineChartTab;
import nl.sense_os.commonsense.client.widgets.WelcomeTab;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.UserModel;

public class Home extends LayoutContainer {

    private static final String TAG = "Home";
    GroupSelection groupSelection = new GroupSelection();
    private final AsyncCallback<Void> mainCallback;
    private final DataServiceAsync service;
    private TabPanel tabPanel;
    private TreePanel<TagModel> tagTree;
    private final UserModel user;

    public Home(UserModel user, AsyncCallback<Void> callback) {
        this.service = (DataServiceAsync) GWT.create(DataService.class);
        this.mainCallback = callback;
        this.user = user;

        // Load the visualization api, passing the onLoadCallback to be called when loading is done.
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
//        logo.setSize("131", "68");
        logo.setPixelSize(131, 68);
        final ContentPanel logoContainer = new ContentPanel();
        logoContainer.setHeaderVisible(false);
        logoContainer.setLayout(new CenterLayout());
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

        final ContentPanel panel = new ContentPanel(new RowLayout(Orientation.VERTICAL));
        panel.setHeaderVisible(false);
        panel.setBorders(true);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setScrollMode(Scroll.AUTOY);
        panel.add(logoContainer, new RowData(-1, -1, new Margins(10,0,0,0)));
        panel.add(tagPanel, new RowData(1, 1, new Margins(10,0,0,0)));
        panel.add(logoutBtn, new RowData(1, -1, new Margins(5,5,5,5)));

        return panel;
    }
    
    private void onSensorValuesReceived(List<SensorValueModel> values) {
        Log.d(TAG, "Received " + values.size() + " sensor values");
        
        if (values.size() > 0) {
            SensorValueModel s = values.get(0);
            
            switch (s.getType()) {
            case SensorValueModel.BOOL:
                MessageBox.info("CommonSense Web Application", "Sorry, no visualization available for this data type (yet).", null);
                break;
            case SensorValueModel.FLOAT:
                TabItem item = new TabItem(s.getName());
                item.setLayout(new CenterLayout());
                item.setClosable(true);
                item.add(new LineChartTab(values));
                this.tabPanel.add(item);
                this.tabPanel.setSelection(item);
                break;
            case SensorValueModel.JSON:
                MessageBox.info("CommonSense Web Application", "Sorry, no visualization available for this data type (yet).", null);
                break;
            case SensorValueModel.STRING:
                MessageBox.info("CommonSense Web Application", "Sorry, no visualization available for this data type (yet).", null);
                break;
            }
        }
    }
    
    private void onTagDrop(TagModel tag) {

        final MessageBox progress = MessageBox.progress("Please wait", "Requesting data...", "");
        progress.getProgressBar().auto();
        progress.show();
        
        AsyncCallback<List<SensorValueModel>> callback = new AsyncCallback<List<SensorValueModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                MessageBox.alert("CommonSense Web Application", "Failed getting sensor values from the database", null);
            }

            @Override
            public void onSuccess(List<SensorValueModel> result) {
                progress.close();
                onSensorValuesReceived(result);                            
            }
            
        };
        service.getSensorValues(tag, new Timestamp(0L), new Timestamp(System.currentTimeMillis()), callback);
    }
    
    private void setupDragDrop() {
        
        new TreePanelDragSource(this.tagTree);
        
        DropTarget dropTarget = new DropTarget(this.tabPanel);
        dropTarget.setOperation(Operation.COPY);
        dropTarget.addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                super.dragDrop(e);
                
                ArrayList<TreeStoreModel> data = e.<ArrayList<TreeStoreModel>>getData();
                for (TreeStoreModel tsm : data) {
                    TagModel tag = (TagModel) tsm.getModel();
                    
                    onTagDrop(tag);
                }                
            }
        });
    }
}
