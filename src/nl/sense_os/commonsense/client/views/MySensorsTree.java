package nl.sense_os.commonsense.client.views;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.events.MySensorsEvents;
import nl.sense_os.commonsense.client.events.VizEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.utility.SensorKeyProvider;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MySensorsTree extends View {

    private static final String TAG = "MySensorsTree";
    private ContentPanel panel;
    private TreeStore<TreeModel> store;
    private ToolButton refreshButton;
    private Button shareButton;
    private Button eventsButton;
    private Button vizButton;
    private TreePanel<TreeModel> tree;
    private BaseTreeLoader<TreeModel> loader;

    public MySensorsTree(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MySensorsEvents.ShowTree)) {
            // Log.d(TAG, "Show");
            onShow(event);
        } else if (type.equals(MySensorsEvents.Done)) {
            // Log.d(TAG, "Done");
            setBusy(false);
        } else if (type.equals(MySensorsEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusy(true);
        } else if (type.equals(MainEvents.ShowVisualization)) {
            // Log.d(TAG, "ShowVisualization");
            refreshLoader();
        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + type);
        }
    }

    private void initHeaderTool() {
        this.refreshButton = new ToolButton("x-tool-refresh");
        this.refreshButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                refreshLoader();
            }
        });
        this.panel.getHeader().addTool(this.refreshButton);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("My personal sensors");

        initTree();
        initHeaderTool();
        initToolBar();

        setupDragDrop();
    }

    private void initToolBar() {

        // listen to toolbar button clicks
        final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(vizButton)) {
                    onVizClick();
                } else if (source.equals(shareButton)) {
                    List<TreeModel> selection = tree.getSelectionModel().getSelection();
                    Dispatcher.forwardEvent(MySensorsEvents.ShowShareDialog, selection);
                } else if (source.equals(eventsButton)) {
                    onEventsClick();
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };

        // initialize the buttons
        this.vizButton = new Button("Visualize", l);
        this.vizButton.disable();

        this.shareButton = new Button("Sharing", l);
        this.shareButton.disable();

        this.eventsButton = new Button("Events", l);
        this.eventsButton.disable();
        this.eventsButton.hide();

        // listen to selection of tree items to enable/disable buttons
        TreePanelSelectionModel<TreeModel> selectionModel = new TreePanelSelectionModel<TreeModel>();
        selectionModel.setSelectionMode(SelectionMode.MULTI);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                List<TreeModel> selection = se.getSelection();
                if (selection.size() > 0) {
                    vizButton.enable();
                    shareButton.enable();
                    // eventsButton.enable();
                } else {
                    vizButton.disable();
                    shareButton.disable();
                    eventsButton.disable();
                }
            }
        });
        this.tree.setSelectionModel(selectionModel);

        // create tool bar
        final ToolBar toolBar = new ToolBar();
        toolBar.add(this.vizButton);
        toolBar.add(this.shareButton);
        toolBar.add(this.eventsButton);

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    private void initTree() {
        // tree store
        @SuppressWarnings({"unchecked", "rawtypes"})
        DataProxy proxy = new DataProxy() {

            @Override
            public void load(DataReader reader, Object loadConfig, AsyncCallback callback) {
                if (null == loadConfig) {
                    Dispatcher.forwardEvent(MySensorsEvents.ListRequested, callback);
                } else if (loadConfig instanceof TreeModel) {
                    List<ModelData> childrenModels = ((TreeModel) loadConfig).getChildren();
                    callback.onSuccess(childrenModels);
                } else {
                    callback.onSuccess(new ArrayList<TreeModel>());
                }
            }
        };
        this.loader = new BaseTreeLoader<TreeModel>(proxy);
        this.store = new TreeStore<TreeModel>(loader);
        this.store.setKeyProvider(new SensorKeyProvider());
        this.store.setStoreSorter(new StoreSorter<TreeModel>(new SensorComparator()));

        this.tree = new TreePanel<TreeModel>(store);
        this.tree.setBorders(false);
        this.tree.setStateful(true);
        this.tree.setId("mySensorsTree");
        this.tree.setDisplayProperty("text");
        this.tree.setIconProvider(new SensorIconProvider());

        // toolbar with filter field
        ToolBar filterBar = new ToolBar();
        filterBar.add(new LabelToolItem("Filter: "));
        StoreFilterField<TreeModel> filter = new StoreFilterField<TreeModel>() {

            @Override
            protected boolean doSelect(Store<TreeModel> store, TreeModel parent, TreeModel record,
                    String property, String filter) {
                // only match leaf nodes
                if (record.getChildCount() > 0) {
                    return false;
                }
                String name = record.get("text");
                name = name.toLowerCase();
                if (name.startsWith(filter.toLowerCase())) {
                    return true;
                }
                return false;
            }

        };
        filter.bind(store);
        filterBar.add(filter);

        ContentPanel content = new ContentPanel(new FitLayout());
        content.setBodyBorder(false);
        content.setHeaderVisible(false);
        content.setTopComponent(filterBar);
        content.add(this.tree);

        this.panel.add(content);
    }

    private void onEventsClick() {
        // TODO Auto-generated method stub
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onShow(AppEvent event) {
        ContentPanel parent = event.<ContentPanel> getData();
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
        } else {
            Log.e(TAG, "Failed to show my sensors panel: parent=null");
        }
    }

    private void onVizClick() {
        List<TreeModel> selection = tree.getSelectionModel().getSelection();
        // TODO get child sensors of selected users, groups and devices
        Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, selection);
    }

    private void refreshLoader() {
        loader.load();
    }

    private void setBusy(boolean busy) {
        String icon = busy ? "gxt/images/gxt/icons/loading.gif" : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }

    /**
     * Sets up the tag tree panel for drag and drop of the tags.
     */
    private void setupDragDrop() {
        TreePanelDragSource source = new TreePanelDragSource(this.tree);
        source.setTreeStoreState(true);
    }
}
