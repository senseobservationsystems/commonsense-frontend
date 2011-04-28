package nl.sense_os.commonsense.client.sensors.personal;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.utility.SensorKeyProvider;
import nl.sense_os.commonsense.client.visualization.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
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
    private boolean isCollapsed = false;
    private ContentPanel panel;
    private TreeStore<TreeModel> store;
    private ToolButton refreshButton;
    private Button shareButton;
    private Button removeButton;
    private Button vizButton;
    private TreePanel<TreeModel> tree;
    private BaseTreeLoader<TreeModel> loader;
    private boolean isRemoving;

    public MySensorsTree(Controller controller) {
        super(controller);
    }

    private List<SensorModel> getSelectedSensors() {

        final List<TreeModel> selection = this.tree.getSelectionModel().getSelection();

        final List<SensorModel> sensors = new ArrayList<SensorModel>();
        for (TreeModel item : selection) {
            if (item instanceof SensorModel) {
                sensors.add((SensorModel) item);
            } else {
                List<ModelData> children = item.getChildren();
                for (ModelData child : children) {
                    if (child instanceof SensorModel) {
                        sensors.add((SensorModel) child);
                    }
                }
            }
        }
        return sensors;
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(MySensorsEvents.ShowTree)) {
            // Log.d(TAG, "ShowTree");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(MySensorsEvents.DeleteSuccess)) {
            // Log.d(TAG, "DeleteSuccess");
            onRemoveSuccess();

        } else if (type.equals(MySensorsEvents.DeleteFailure)) {
            // Log.d(TAG, "DeleteFailure");
            onRemoveFailure();

        } else if (type.equals(MySensorsEvents.Done)) {
            // Log.d(TAG, "ListUpdated");
            setBusy(false);

        } else if (type.equals(MySensorsEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusy(true);

        } else if (type.equals(VizEvents.Show)) {
            // Log.d(TAG, "Show Visualization");
            refreshLoader(false);

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
                refreshLoader(true);
            }
        });
        this.panel.getHeader().addTool(this.refreshButton);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("My personal sensors");

        // track whether the panel is expanded
        Listener<ComponentEvent> collapseListener = new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                EventType type = be.getType();
                if (type.equals(Events.Expand)) {
                    isCollapsed = false;
                    refreshLoader(false);
                } else if (type.equals(Events.Collapse)) {
                    isCollapsed = true;
                }
            }
        };
        panel.addListener(Events.Expand, collapseListener);
        panel.addListener(Events.Collapse, collapseListener);

        initTree();
        initHeaderTool();
        initToolBar();
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
                    onShareClick();
                } else if (source.equals(removeButton)) {
                    onRemoveClick();
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };

        // initialize the buttons
        this.vizButton = new Button("Visualize", l);
        this.vizButton.disable();

        this.shareButton = new Button("Share", l);
        this.shareButton.disable();

        this.removeButton = new Button("Remove", l);
        this.removeButton.disable();

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
                    removeButton.enable();
                } else {
                    vizButton.disable();
                    shareButton.disable();
                    removeButton.disable();
                }
            }
        });
        this.tree.setSelectionModel(selectionModel);

        // create tool bar
        final ToolBar toolBar = new ToolBar();
        toolBar.add(this.vizButton);
        toolBar.add(this.shareButton);
        toolBar.add(this.removeButton);

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    private void initTree() {
        // tree store
        RpcProxy<List<TreeModel>> proxy = new RpcProxy<List<TreeModel>>() {

            @Override
            public void load(Object loadConfig, AsyncCallback<List<TreeModel>> callback) {
                // only load when the panel is not collapsed
                if (false == isCollapsed) {
                    if (null == loadConfig) {
                        fireEvent(new AppEvent(MySensorsEvents.ListRequested, callback));
                    } else if (loadConfig instanceof TreeModel) {
                        List<ModelData> childrenModels = ((TreeModel) loadConfig).getChildren();
                        List<TreeModel> children = new ArrayList<TreeModel>();
                        for (ModelData model : childrenModels) {
                            children.add((TreeModel) model);
                        }
                        callback.onSuccess(children);
                    } else {
                        callback.onSuccess(new ArrayList<TreeModel>());
                    }
                }
            }
        };
        this.loader = new BaseTreeLoader<TreeModel>(proxy);
        this.store = new TreeStore<TreeModel>(loader);
        this.store.setKeyProvider(new SensorKeyProvider());
        this.store.setStoreSorter(new StoreSorter<TreeModel>(new SensorComparator()));

        this.tree = new TreePanel<TreeModel>(store);
        this.tree.setAutoLoad(true);
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

        setupDragDrop();
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onRemoveClick() {

        // get sensor models from the selection
        final List<SensorModel> sensors = getSelectedSensors();

        if (sensors.size() > 0) {
            this.isRemoving = true;

            AppEvent event = new AppEvent(MySensorsEvents.ShowDeleteDialog);
            event.setData("sensors", sensors);
            Dispatcher.forwardEvent(event);

        } else {
            MessageBox.info(null, "No sensors selected. You can only remove sensors!", null);
        }
    }

    private void onRemoveFailure() {
        if (this.isRemoving) {
            this.isRemoving = false;
            refreshLoader(true);
        }
    }

    private void onRemoveSuccess() {
        if (this.isRemoving) {
            this.isRemoving = false;
            refreshLoader(true);
        }
    }

    protected void onShareClick() {
        List<SensorModel> sensors = getSelectedSensors();
        AppEvent shareEvent = new AppEvent(MySensorsEvents.ShowShareDialog);
        shareEvent.setData("sensors", sensors);
        fireEvent(shareEvent);
    }

    private void onVizClick() {
        List<TreeModel> selection = tree.getSelectionModel().getSelection();
        // TODO get child sensors of selected users, groups and devices
        Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, selection);
    }

    private void refreshLoader(boolean force) {
        if (force || this.store.getChildCount() == 0) {
            loader.load();
        }
    }

    private void setBusy(boolean busy) {
        String icon = busy ? Constants.ICON_LOADING : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }

    /**
     * Sets up the tag tree panel for drag and drop of the tags.
     */
    private void setupDragDrop() {
        TreePanelDragSource source = new TreePanelDragSource(this.tree);
        source.setTreeStoreState(true);
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
        } else {
            Log.e(TAG, "Failed to show my sensors panel: parent=null");
        }
    }
}
