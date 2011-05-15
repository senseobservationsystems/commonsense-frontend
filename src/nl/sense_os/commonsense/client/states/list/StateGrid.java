package nl.sense_os.commonsense.client.states.list;

import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.client.sensors.library.LibraryColumnsFactory;
import nl.sense_os.commonsense.client.states.connect.StateConnectEvents;
import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.client.states.defaults.StateDefaultsEvents;
import nl.sense_os.commonsense.client.states.edit.StateEditEvents;
import nl.sense_os.commonsense.client.states.feedback.FeedbackEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.client.utility.SensorProcessor;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.TreeGridDragSource;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
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
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuBar;
import com.extjs.gxt.ui.client.widget.menu.MenuBarItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateGrid extends View {

    protected static final String TAG = "StateGrid";
    private ContentPanel panel;
    private TreeGrid<SensorModel> grid;
    private TreeStore<SensorModel> store;
    private TreeLoader<SensorModel> loader;
    private boolean isListDirty = false;
    private MenuItem createButton;
    private MenuItem deleteButton;
    private MenuItem disconnectButton;
    private MenuItem connectButton;
    private MenuItem editButton;
    private MenuItem feedbackButton;
    private MenuItem defaultsButton;
    private ToolBar filterBar;
    private ToolButton refreshButton;
    private MenuBar toolBar;
    private StoreFilterField<SensorModel> filter;

    public StateGrid(Controller controller) {
        super(controller);
    }

    private void checkDefaultStates() {
        Dispatcher.forwardEvent(StateDefaultsEvents.CheckDefaults);
    }

    private void confirmDisconnect() {
        MessageBox.confirm(null,
                "Are you sure you want to disconnect this sensor from this state?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getText())) {
                            disconnectSensor();
                        }
                    }
                });
    }

    /**
     * Dispatches request to show "delete dialog" for the selected state.
     */
    private void deleteState() {
        SensorModel state = getSelectedState();
        AppEvent delete = new AppEvent(SensorDeleteEvents.ShowDeleteDialog);
        delete.setData("sensors", Arrays.asList(state));
        Dispatcher.forwardEvent(delete);
    }

    private void disconnectSensor() {
        TreeModel sensor = this.grid.getSelectionModel().getSelectedItem();
        TreeModel service = sensor.getParent();

        AppEvent event = new AppEvent(StateListEvents.RemoveRequested);
        event.setData("sensor", sensor);
        event.setData("service", service);
        Dispatcher.forwardEvent(event);
        setBusy(true);
    }

    private SensorModel getSelectedState() {
        SensorModel selection = this.grid.getSelectionModel().getSelectedItem();
        while (store.getParent(selection) instanceof SensorModel) {
            selection = (SensorModel) store.getParent(selection);
        }
        return selection;
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(StateListEvents.ShowGrid)) {
            // Log.d(TAG, "ShowGrid");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(VizEvents.Show)) {
            // Log.d(TAG, "Show Visualization");
            refreshLoader(false);

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);

        } else if (type.equals(StateListEvents.Done)) {
            // Log.d(TAG, "TreeUpdated");
            setBusy(false);

        } else if (type.equals(StateListEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusy(true);

        } else if (type.equals(StateListEvents.LoadComplete)) {
            // Log.d(TAG, "TreeUpdated");
            onLoadComplete();

        } else if (type.equals(StateListEvents.RemoveComplete)) {
            // Log.d(TAG, "RemoveComplete");
            onRemoveComplete(event);

        } else if (type.equals(StateListEvents.RemoveFailed)) {
            Log.w(TAG, "RemoveFailed");
            onRemoveFailed(event);

        } else if (type.equals(StateConnectEvents.ConnectSuccess)
                || type.equals(StateCreateEvents.CreateServiceComplete)
                || type.equals(StateDefaultsEvents.CheckDefaultsSuccess)
                || type.equals(SensorDeleteEvents.DeleteSuccess)) {
            // Log.d(TAG, "External trigger for update");
            refreshLoader(true);

        } else {
            Log.e(TAG, "Unexpected event type: " + type);
        }
    }

    private void initFilter() {
        filterBar = new ToolBar();
        filterBar.add(new LabelToolItem("Filter: "));
        this.filter = new StoreFilterField<SensorModel>() {

            @Override
            protected boolean doSelect(Store<SensorModel> store, SensorModel parent,
                    SensorModel record, String property, String filter) {
                filter = filter.toLowerCase();
                if (record.getName().toLowerCase().contains(filter)) {
                    return true;
                } else if (record.getPhysicalSensor().toLowerCase().contains(filter)) {
                    return true;
                } else if (record.getDevice() != null
                        && record.getDevice().getType().toLowerCase().contains(filter)) {
                    return true;
                } else if (record.getDataType().toLowerCase().contains(filter)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        filter.bind(this.store);
        filterBar.add(filter);

        // TODO fix filtering
        filter.setEnabled(false);
    }

    private void initGrid() {

        // proxy
        DataProxy<List<SensorModel>> proxy = new DataProxy<List<SensorModel>>() {

            @Override
            public void load(DataReader<List<SensorModel>> reader, Object loadConfig,
                    AsyncCallback<List<SensorModel>> callback) {

                if (panel.isExpanded()) {
                    AppEvent request = new AppEvent(StateListEvents.LoadRequest);
                    request.setData("loadConfig", loadConfig);
                    request.setData("callback", callback);
                    Dispatcher.forwardEvent(request);
                } else {
                    callback.onFailure(null);
                }
            }
        };

        // tree loader
        this.loader = new BaseTreeLoader<SensorModel>(proxy) {

            @Override
            public boolean hasChildren(SensorModel parent) {
                // only state sensors have children
                return parent.getType().equals("2");
            };
        };

        // tree store
        this.store = new TreeStore<SensorModel>(this.loader);
        this.store.setStoreSorter(new StoreSorter<SensorModel>(new SensorComparator()));

        // column model, make sure you add a TreeGridCellRenderer
        List<ColumnConfig> columns = LibraryColumnsFactory.create().getColumns();
        ColumnModel cm = new ColumnModel(columns);
        ColumnConfig type = cm.getColumnById(SensorModel.TYPE);
        type.setRenderer(new TreeGridCellRenderer<SensorModel>() {

            @Override
            protected String getText(TreeGrid<SensorModel> grid, SensorModel model,
                    String property, int rowIndex, int colIndex) {
                // type text is always empty, use SenseIconProvider to differentiate
                return "";
            }
        });
        type.setWidth(85);

        this.grid = new TreeGrid<SensorModel>(this.store, cm);
        this.grid.setModelProcessor(new SensorProcessor<SensorModel>());
        this.grid.setId("stateGrid");
        this.grid.setStateful(true);
        this.grid.setAutoLoad(true);
        this.grid.setAutoExpandColumn(SensorModel.DISPLAY_NAME);
        this.grid.setIconProvider(new SenseIconProvider<SensorModel>());
    }

    private void initHeaderTool() {
        refreshButton = new ToolButton("x-tool-refresh");
        refreshButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {

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
        this.panel.setHeading("Manage states");

        // track whether the panel is expanded
        this.panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                refreshLoader(false);
            }
        });

        initGrid();
        initFilter();
        initHeaderTool();
        initToolBar();

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

    private void initToolBar() {
        TreeGridSelectionModel<SensorModel> selectionModel = new TreeGridSelectionModel<SensorModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<SensorModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<SensorModel> se) {
                SensorModel selection = se.getSelectedItem();
                if (null != selection) {
                    StateGrid.this.deleteButton.enable();
                    StateGrid.this.editButton.enable();
                    StateGrid.this.connectButton.enable();

                    // only able to disconnect if sensor is selected
                    TreeModel parent = selection.getParent();
                    if (parent != null) {
                        StateGrid.this.disconnectButton.enable();
                    } else {
                        StateGrid.this.disconnectButton.disable();
                    }

                    // only able to give feedback if state has manualLearn method
                    SensorModel state = getSelectedState();
                    List<ModelData> methods = state.get("methods");
                    boolean canHazFeedback = false;
                    for (ModelData method : methods) {
                        if (method.get("name").equals("GetManualInputMode")) {
                            canHazFeedback = true;
                            break;
                        }
                    }
                    StateGrid.this.feedbackButton.setEnabled(canHazFeedback);

                } else {
                    StateGrid.this.editButton.enable();
                    StateGrid.this.feedbackButton.enable();
                    StateGrid.this.deleteButton.disable();
                    StateGrid.this.connectButton.disable();
                    StateGrid.this.disconnectButton.disable();
                }
            }
        });
        this.grid.setSelectionModel(selectionModel);

        final SelectionListener<MenuEvent> l = new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent me) {
                MenuItem source = (MenuItem) me.getItem();
                if (source.equals(StateGrid.this.createButton)) {
                    onCreateClick();
                } else if (source.equals(StateGrid.this.deleteButton)) {
                    deleteState();
                } else if (source.equals(StateGrid.this.editButton)) {
                    onEditClick();
                } else if (source.equals(StateGrid.this.connectButton)) {
                    onConnectClick();
                } else if (source.equals(StateGrid.this.disconnectButton)) {
                    confirmDisconnect();
                } else if (source.equals(StateGrid.this.feedbackButton)) {
                    showFeedback();
                } else if (source.equals(StateGrid.this.defaultsButton)) {
                    checkDefaultStates();
                } else {
                    Log.w(TAG, "Unexpected button clicked");
                }
            }
        };

        // menu item for editing service stuff
        Menu serviceMenu = new Menu();

        this.createButton = new MenuItem("New State", l);
        serviceMenu.add(this.createButton);

        this.defaultsButton = new MenuItem("Default States", l);
        serviceMenu.add(this.defaultsButton);

        SeparatorMenuItem separator = new SeparatorMenuItem();
        serviceMenu.add(separator);

        this.deleteButton = new MenuItem("Delete State", l);
        this.deleteButton.disable();
        serviceMenu.add(this.deleteButton);

        SeparatorMenuItem separator2 = new SeparatorMenuItem();
        serviceMenu.add(separator2);

        this.editButton = new MenuItem("Algorithm Parameters", l);
        this.editButton.disable();
        serviceMenu.add(this.editButton);

        this.feedbackButton = new MenuItem("Give Algorithm Feedback", l);
        this.feedbackButton.disable();
        serviceMenu.add(this.feedbackButton);

        // menu item for editing sensor stuff
        Menu sensorsMenu = new Menu();

        this.connectButton = new MenuItem("Connect Sensor", l);
        this.connectButton.disable();
        sensorsMenu.add(this.connectButton);

        this.disconnectButton = new MenuItem("Disconnect Sensor", l);
        this.disconnectButton.disable();
        sensorsMenu.add(this.disconnectButton);

        // create tool bar
        this.toolBar = new MenuBar();
        toolBar.add(new MenuBarItem("State", serviceMenu));
        toolBar.add(new MenuBarItem("Sensors", sensorsMenu));

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    private void onConnectClick() {
        SensorModel selectedService = getSelectedState();
        Dispatcher.forwardEvent(StateConnectEvents.ShowSensorConnecter, selectedService);
    }

    private void onCreateClick() {
        Dispatcher.forwardEvent(StateCreateEvents.ShowCreator);
    }

    private void onEditClick() {
        SensorModel selectedService = getSelectedState();
        AppEvent event = new AppEvent(StateEditEvents.ShowEditor);
        event.setData(selectedService);
        Dispatcher.forwardEvent(event);
    }

    private void onLoadComplete() {
        this.isListDirty = false;
        // this.filter.clear(); // TODO: does not work well with the tree loader
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onRemoveComplete(AppEvent event) {
        setBusy(false);
        refreshLoader(true);
    }

    private void onRemoveFailed(AppEvent event) {
        setBusy(false);
        MessageBox.confirm(null, "Failed to disconnect sensor. Retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            disconnectSensor();
                        }
                    }
                });
    }

    private void refreshLoader(boolean force) {
        if (force || (this.store.getChildCount() == 0 || this.isListDirty)
                && this.panel.isExpanded()) {
            // Log.d(TAG, "Refresh loader...");
            this.loader.load();
        }
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.panel.getHeader().setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            this.panel.getHeader().setIcon(IconHelper.create(""));
        }
    }

    /**
     * Sets up the sensor list for drag and drop.
     */
    private void setupDragDrop() {
        new TreeGridDragSource(this.grid);
    }

    private void showFeedback() {
        SensorModel state = getSelectedState();
        List<SensorModel> sensors = store.getChildren(state);

        AppEvent event = new AppEvent(FeedbackEvents.FeedbackInit);
        event.setData("state", state);
        event.setData("sensors", sensors);
        Dispatcher.forwardEvent(event);
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
        } else {
            Log.e(TAG, "Failed to show states panel: parent=null");
        }
    }
}
