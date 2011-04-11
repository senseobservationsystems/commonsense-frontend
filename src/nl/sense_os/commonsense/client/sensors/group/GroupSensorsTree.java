package nl.sense_os.commonsense.client.sensors.group;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.groups.GroupEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.SensorsEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.utility.SensorKeyProvider;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.UserModel;

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

public class GroupSensorsTree extends View {

    private static final String TAG = "GroupSensorsTree";
    private Button unshareButton;
    private ContentPanel panel;
    private boolean isCollapsed;
    private ToolButton refreshButton;
    private Button vizButton;
    private TreeStore<TreeModel> store;
    private TreePanel<TreeModel> tree;
    private BaseTreeLoader<TreeModel> loader;

    public GroupSensorsTree(Controller c) {
        super(c);
    }

    private List<SensorModel> getSelectedSensors() {

        final List<TreeModel> selection = this.tree.getSelectionModel().getSelection();

        final List<SensorModel> sensors = new ArrayList<SensorModel>();
        for (TreeModel item : selection) {
            if (item instanceof SensorModel) {
                sensors.add((SensorModel) item);
                Log.d(TAG, "Owner " + item.get(SensorModel.OWNER));
            } else {
                List<ModelData> children = item.getChildren();
                for (ModelData child : children) {
                    if (child instanceof SensorModel) {
                        sensors.add((SensorModel) child);
                        Log.d(TAG, "Owner " + child.get(SensorModel.OWNER));
                    } else if (child instanceof TreeModel) {
                        List<ModelData> grandchildren = ((TreeModel) child).getChildren();
                        for (ModelData grandchild : grandchildren) {
                            if (grandchild instanceof SensorModel) {
                                sensors.add((SensorModel) grandchild);
                                Log.d(TAG, "Owner " + grandchild.get(SensorModel.OWNER));
                            }
                        }
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

        } else if (type.equals(GroupSensorsEvents.ShowTree)) {
            // Log.d(TAG, "ShowTree");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(GroupSensorsEvents.Done)) {
            // Log.d(TAG, "ListUpdated");
            setBusy(false);

        } else if (type.equals(GroupSensorsEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusy(true);

        } else if (type.equals(GroupEvents.ListUpdated)) {
            // Log.d(TAG, "Group ListUpdated");
            refreshLoader(true);

        } else if (type.equals(SensorsEvents.UnshareSuccess)) {
            // Log.d(TAG, "UnshareSuccess");
            onRemoveSuccess();

        } else if (type.equals(SensorsEvents.UnshareFailure)) {
            Log.w(TAG, "UnshareFailure");
            onRemoveFailure();

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
        this.panel.setHeading("My group sensors");

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
                } else if (source.equals(unshareButton)) {
                    onRemoveClick();
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };

        // initialize the buttons
        this.vizButton = new Button("Visualize", l);
        this.vizButton.disable();

        this.unshareButton = new Button("Unshare", l);
        this.unshareButton.disable();

        // listen to selection of tree items to enable/disable buttons
        TreePanelSelectionModel<TreeModel> selectionModel = new TreePanelSelectionModel<TreeModel>();
        selectionModel.setSelectionMode(SelectionMode.MULTI);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                List<SensorModel> sensors = getSelectedSensors();
                if (sensors.size() > 0) {
                    vizButton.enable();
                    unshareButton.enable();
                } else {
                    vizButton.disable();
                    unshareButton.disable();
                }
            }
        });
        this.tree.setSelectionModel(selectionModel);

        // create tool bar
        final ToolBar toolBar = new ToolBar();
        toolBar.add(this.vizButton);
        toolBar.add(this.unshareButton);

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
                        Dispatcher.forwardEvent(GroupSensorsEvents.ListRequest, callback);
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
        this.tree.setId("groupSensorsTree");
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

            // add the ID of the user or group that the sensor is shared with
            for (SensorModel sensor : sensors) {
                TreeModel parent = sensor.getParent();
                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }
                sensor.set("user", parent.get(UserModel.ID));
            }

            AppEvent event = new AppEvent(SensorsEvents.ShowUnshareDialog);
            event.setData("sensors", sensors);
            Dispatcher.forwardEvent(event);

        } else {
            // should never happen
            MessageBox.info(null, "No sensors selected. You can only remove sensors!", null);
        }
    }
    private void onRemoveFailure() {
        refreshLoader(true);
    }

    private void onRemoveSuccess() {
        refreshLoader(true);
    }

    private void onVizClick() {
        List<SensorModel> selection = getSelectedSensors();
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
            Log.e(TAG, "Failed to show group sensors panel: parent=null");
        }
    }
}
