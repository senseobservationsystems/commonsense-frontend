package nl.sense_os.commonsense.client.sensors.personal;

import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.visualization.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
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
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MySensorsGrid extends View {

    private static final String TAG = "MySensorsGrid";
    private boolean isCollapsed = false;
    private ContentPanel panel;
    private GroupingStore<SensorModel> store;
    private ToolButton refreshButton;
    private Button shareButton;
    private Button removeButton;
    private ToolBar toolBar;
    private Button vizButton;
    private Grid<SensorModel> grid;
    private ListLoader<ListLoadResult<SensorModel>> loader;
    private boolean isRemoving;
    private StoreFilterField<SensorModel> filter;
    private ToolBar filterBar;

    public MySensorsGrid(Controller controller) {
        super(controller);
    }

    private ColumnModel createColumnModel() {
        ColumnConfig id = new ColumnConfig(SensorModel.ID, "ID", 50);
        id.setHidden(true);
        ColumnConfig type = new ColumnConfig(SensorModel.TYPE, "Type", 50);
        ColumnConfig name = new ColumnConfig(SensorModel.NAME, "Name", 200);
        ColumnConfig devType = new ColumnConfig(SensorModel.DEVICE_TYPE, "Physical sensor", 200);
        devType.setRenderer(new GridCellRenderer<SensorModel>() {

            @Override
            public Object render(SensorModel model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<SensorModel> store, Grid<SensorModel> grid) {
                if (!model.getDeviceType().equals(model.getName())) {
                    return model.getDeviceType();
                } else {
                    return "";
                }
            }
        });
        ColumnConfig devId = new ColumnConfig(SensorModel.DEVICE_ID, "Device ID", 50);
        devId.setHidden(true);
        ColumnConfig device = new ColumnConfig(SensorModel.DEVICE_DEVTYPE, "Device", 200);
        type.setRenderer(new GridCellRenderer<SensorModel>() {

            @Override
            public Object render(SensorModel model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<SensorModel> store, Grid<SensorModel> grid) {
                SensorIconProvider<SensorModel> provider = new SensorIconProvider<SensorModel>();
                provider.getIcon(model).getHTML();
                return provider.getIcon(model).getHTML();
            }
        });
        ColumnConfig dataType = new ColumnConfig(SensorModel.DATA_TYPE, "Data type", 100);
        dataType.setHidden(true);

        ColumnModel cm = new ColumnModel(Arrays.asList(type, id, name, devType, devId, device,
                dataType));

        return cm;
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
            // Log.d(TAG, "TreeUpdated");
            setBusy(false);

        } else if (type.equals(MySensorsEvents.ListUpdated)) {
            // Log.d(TAG, "TreeUpdated");
            onListUpdate();

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
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("My personal sensors (BETA)");

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

        initGrid();
        initFilter();
        initToolBar();
        initHeaderTool();

        // do layout
        this.panel.getHeader().addTool(this.refreshButton);
        this.panel.setTopComponent(this.toolBar);
        ContentPanel content = new ContentPanel(new FitLayout());
        content.setBodyBorder(false);
        content.setHeaderVisible(false);
        content.setTopComponent(this.filterBar);
        content.add(this.grid);
        this.panel.add(content);

        setupDragDrop();
    }

    private void initFilter() {

        this.filterBar = new ToolBar();
        this.filterBar.add(new LabelToolItem("Filter: "));
        this.filter = new StoreFilterField<SensorModel>() {

            @Override
            protected boolean doSelect(Store<SensorModel> store, SensorModel parent,
                    SensorModel record, String property, String filter) {

                if (record.getName().contains(filter.toLowerCase())) {
                    return true;
                } else if (record.getDeviceType().contains(filter.toLowerCase())) {
                    return true;
                } else if (record.getDevDeviceType().contains(filter.toLowerCase())) {
                    return true;
                } else if (record.getDataType().contains(filter.toLowerCase())) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        this.filter.bind(this.store);
        this.filterBar.add(this.filter);
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
        GridSelectionModel<SensorModel> selectionModel = new GridSelectionModel<SensorModel>();
        selectionModel.setSelectionMode(SelectionMode.MULTI);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<SensorModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<SensorModel> se) {
                List<SensorModel> selection = se.getSelection();
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
        this.grid.setSelectionModel(selectionModel);

        // create tool bar
        this.toolBar = new ToolBar();
        this.toolBar.add(this.vizButton);
        this.toolBar.add(this.shareButton);
        this.toolBar.add(this.removeButton);

    }

    private void initGrid() {
        // tree store
        RpcProxy<ListLoadResult<SensorModel>> proxy = new RpcProxy<ListLoadResult<SensorModel>>() {

            @Override
            public void load(Object loadConfig, AsyncCallback<ListLoadResult<SensorModel>> callback) {
                // only load when the panel is not collapsed
                if (false == isCollapsed) {
                    if (loadConfig instanceof BaseListLoadConfig) {
                        fireEvent(new AppEvent(MySensorsEvents.ListRequested, callback));
                    } else {
                        Log.w(TAG, "Unexpected loadconfig: " + loadConfig);
                    }
                }
            }
        };
        this.loader = new BaseListLoader<ListLoadResult<SensorModel>>(proxy);
        this.store = new GroupingStore<SensorModel>(loader);
        this.store.setKeyProvider(new ModelKeyProvider<SensorModel>() {

            @Override
            public String getKey(SensorModel model) {
                return model.getId() + model.getName() + model.getDeviceType() + model.getType();
            }

        });
        // this.store.setStoreSorter(new StoreSorter<SensorModel>(new SensorComparator()));
        this.store.groupBy(SensorModel.TYPE);
        this.store.setDefaultSort(SensorModel.TYPE, SortDir.DESC);
        this.store.setSortField(SensorModel.TYPE);

        // Column model
        ColumnModel cm = createColumnModel();

        GroupingView groupingView = new GroupingView();
        groupingView.setShowGroupedColumn(true);
        groupingView.setForceFit(true);
        groupingView.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                if (data.field.equals(SensorModel.TYPE)) {
                    int group = Integer.parseInt(data.group);
                    String f = data.group;
                    switch (group) {
                        case 0 :
                            f = "Feeds";
                            break;
                        case 1 :
                            f = "Physical";
                            break;
                        case 2 :
                            f = "States";
                            break;
                        case 3 :
                            f = "Environment sensors";
                            break;
                        case 4 :
                            f = "Public sensors";
                            break;
                        default :
                            f = "Unsorted";
                    }
                    String l = data.models.size() == 1 ? "Sensor" : "Sensors";
                    return f + " (" + data.models.size() + " " + l + ")";
                } else {
                    if (data.group.equals("")) {
                        return "Ungrouped";
                    } else {
                        return data.group;
                    }
                }
            }
        });

        this.grid = new Grid<SensorModel>(this.store, cm);
        this.grid.setView(groupingView);
        this.grid.setBorders(false);
        this.grid.setStateful(true);
        this.grid.setLoadMask(true);
        this.grid.setId("mySensorsGrid");
    }

    private void onListUpdate() {
        this.filter.clear();
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onRemoveClick() {

        // get sensor models from the selection
        final List<SensorModel> sensors = this.grid.getSelectionModel().getSelection();

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
        List<SensorModel> sensors = this.grid.getSelectionModel().getSelection();
        AppEvent shareEvent = new AppEvent(MySensorsEvents.ShowShareDialog);
        shareEvent.setData("sensors", sensors);
        fireEvent(shareEvent);
    }

    private void onVizClick() {
        List<SensorModel> selection = this.grid.getSelectionModel().getSelection();
        // TODO get child sensors of selected users, groups and devices
        Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, selection);
    }

    private void refreshLoader(boolean force) {
        if (force || this.store.getCount() == 0) {
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
        new GridDragSource(this.grid);
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
