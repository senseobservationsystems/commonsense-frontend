package nl.sense_os.commonsense.client.states.list;

import java.util.ArrayList;
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
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.utility.SensorKeyProvider;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.TreeModel;
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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
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

public class StateTree extends View {

    protected static final String TAG = "StateTree";
    private ContentPanel panel;
    private TreeGrid<SensorModel> tree;
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

    public StateTree(Controller controller) {
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

    private void disconnectSensor() {
        TreeModel sensor = this.tree.getSelectionModel().getSelectedItem();
        TreeModel service = sensor.getParent();

        AppEvent event = new AppEvent(StateEvents.RemoveRequested);
        event.setData("sensor", sensor);
        event.setData("service", service);
        Dispatcher.forwardEvent(event);
        setBusy(true);
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

    private SensorModel getSelectedState() {
        TreeModel selection = this.tree.getSelectionModel().getSelectedItem();

        TreeModel state = selection;
        TreeModel parent = state.getParent();
        while (parent != null) {
            state = parent;
            parent = state.getParent();
        }
        if (false == (state instanceof SensorModel)) {
            Log.w(TAG, "Selected state is not a SensorModel?!");
            return null;
        }
        return (SensorModel) state;
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(StateEvents.ShowGrid)) {
            // Log.d(TAG, "ShowGrid");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(VizEvents.Show)) {
            // Log.d(TAG, "Show Visualization");
            refreshLoader(false);

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);

        } else if (type.equals(StateEvents.Done)) {
            // Log.d(TAG, "TreeUpdated");
            setBusy(false);

        } else if (type.equals(StateEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusy(true);

        } else if (type.equals(StateEvents.RemoveComplete)) {
            // Log.d(TAG, "RemoveComplete");
            onRemoveComplete(event);

        } else if (type.equals(StateEvents.RemoveFailed)) {
            Log.w(TAG, "RemoveFailed");
            onRemoveFailed(event);

        } else if (type.equals(StateConnectEvents.ConnectSuccess)) {
            // Log.d(TAG, "ConnectSuccess");
            refreshLoader(true);

        } else if (type.equals(StateCreateEvents.CreateServiceComplete)) {
            // Log.d(TAG, "CreateServiceComplete");
            refreshLoader(true);

        } else if (type.equals(StateDefaultsEvents.CheckDefaultsSuccess)) {
            // Log.d(TAG, "CheckDefaultsSuccess");
            refreshLoader(true);

        } else if (type.equals(SensorDeleteEvents.DeleteSuccess)) {
            // Log.d(TAG, "External trigger for update");
            refreshLoader(true);

        } else {
            Log.e(TAG, "Unexpected event type: " + type);
        }
    }

    private void initHeaderTool() {
        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                refreshLoader(true);
            }
        });

        // add to panel
        this.panel.getHeader().addTool(refresh);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("Manage states");

        // track whether the panel is expanded
        panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                refreshLoader(false);
            }
        });

        initGrid();
        initHeaderTool();
        initToolBar();
    }

    private void initToolBar() {
        TreeGridSelectionModel<SensorModel> selectionModel = new TreeGridSelectionModel<SensorModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<SensorModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<SensorModel> se) {
                SensorModel selection = se.getSelectedItem();
                if (null != selection) {
                    deleteButton.enable();
                    editButton.enable();
                    connectButton.enable();

                    // only able to disconnect if sensor is selected
                    TreeModel parent = selection.getParent();
                    if (parent != null) {
                        disconnectButton.enable();
                    } else {
                        disconnectButton.disable();
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
                    feedbackButton.setEnabled(canHazFeedback);

                } else {
                    editButton.enable();
                    feedbackButton.enable();
                    deleteButton.disable();
                    connectButton.disable();
                    disconnectButton.disable();
                }
            }
        });
        this.tree.setSelectionModel(selectionModel);

        final SelectionListener<MenuEvent> l = new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent me) {
                MenuItem source = (MenuItem) me.getItem();
                if (source.equals(createButton)) {
                    onCreateClick();
                } else if (source.equals(deleteButton)) {
                    deleteState();
                } else if (source.equals(editButton)) {
                    onEditClick();
                } else if (source.equals(connectButton)) {
                    onConnectClick();
                } else if (source.equals(disconnectButton)) {
                    confirmDisconnect();
                } else if (source.equals(feedbackButton)) {
                    showFeedback();
                } else if (source.equals(defaultsButton)) {
                    checkDefaultStates();
                } else {
                    Log.w(TAG, "Unexpected button clicked");
                }
            }
        };

        // menu item for editing service stuff
        Menu serviceMenu = new Menu();

        this.createButton = new MenuItem("New State", l);
        serviceMenu.add(createButton);

        this.defaultsButton = new MenuItem("Default States", l);
        serviceMenu.add(defaultsButton);

        SeparatorMenuItem separator = new SeparatorMenuItem();
        serviceMenu.add(separator);

        this.deleteButton = new MenuItem("Delete State", l);
        this.deleteButton.disable();
        serviceMenu.add(deleteButton);

        SeparatorMenuItem separator2 = new SeparatorMenuItem();
        serviceMenu.add(separator2);

        this.editButton = new MenuItem("Algorithm Parameters", l);
        this.editButton.disable();
        serviceMenu.add(editButton);

        this.feedbackButton = new MenuItem("Give Algorithm Feedback", l);
        this.feedbackButton.disable();
        serviceMenu.add(feedbackButton);

        // menu item for editing sensor stuff
        Menu sensorsMenu = new Menu();

        this.connectButton = new MenuItem("Connect Sensor", l);
        this.connectButton.disable();
        sensorsMenu.add(connectButton);

        this.disconnectButton = new MenuItem("Disconnect Sensor", l);
        this.disconnectButton.disable();
        sensorsMenu.add(disconnectButton);

        // create tool bar
        final MenuBar toolBar = new MenuBar();
        toolBar.add(new MenuBarItem("State", serviceMenu));
        toolBar.add(new MenuBarItem("Sensors", sensorsMenu));

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    private void initGrid() {
        // tree store
        DataProxy<List<SensorModel>> proxy = new DataProxy<List<SensorModel>>() {

            @Override
            public void load(DataReader<List<SensorModel>> reader, Object loadConfig,
                    AsyncCallback<List<SensorModel>> callback) {
                // only load when the panel is not collapsed
                Log.d(TAG, "load: " + loadConfig);
                if (null == loadConfig) {
                    Dispatcher.forwardEvent(StateEvents.ListRequested, callback);
                } else if (loadConfig instanceof SensorModel) {
                    List<ModelData> childrenModels = ((SensorModel) loadConfig).getChildren();
                    List<SensorModel> children = new ArrayList<SensorModel>();
                    for (ModelData model : childrenModels) {
                        children.add((SensorModel) model);
                    }
                    callback.onSuccess(children);
                } else {
                    callback.onSuccess(new ArrayList<SensorModel>());
                }
            }
        };
        this.loader = new BaseTreeLoader<SensorModel>(proxy);
        this.store = new TreeStore<SensorModel>(this.loader);
        this.store.setKeyProvider(new SensorKeyProvider<SensorModel>());
        // this.store.setStoreSorter(new StoreSorter<SensorModel>(new SensorComparator()));

        List<ColumnConfig> columns = LibraryColumnsFactory.create().getColumns();
        // ColumnConfig foo = new ColumnConfig("text", 10);
        // foo.setRenderer(new TreeGridCellRenderer<ModelData>());
        // columns.add(0, foo);
        ColumnModel cm = new ColumnModel(columns);
        cm.getColumnById(SensorModel.TYPE).setRenderer(new TreeGridCellRenderer<SensorModel>() {

            @Override
            public Object render(SensorModel model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<SensorModel> store, Grid<SensorModel> grid) {
                SensorIconProvider<SensorModel> provider = new SensorIconProvider<SensorModel>();
                provider.getIcon(model).getHTML();
                return provider.getIcon(model).getHTML();
            }
        });

        this.tree = new TreeGrid<SensorModel>(this.store, cm);
        this.tree.setId("stateGrid");
        this.tree.setStateful(true);

        // toolbar with filter field
        ToolBar filterBar = new ToolBar();
        filterBar.add(new LabelToolItem("Filter: "));
        StoreFilterField<SensorModel> filter = new StoreFilterField<SensorModel>() {

            @Override
            protected boolean doSelect(Store<SensorModel> store, SensorModel parent,
                    SensorModel record, String property, String filter) {
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

        // add grid to panel
        ContentPanel content = new ContentPanel(new FitLayout());
        content.setBodyBorder(false);
        content.setHeaderVisible(false);
        content.setTopComponent(filterBar);
        content.add(this.tree);

        this.panel.add(content);
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
            loader.load();
        }
    }

    private void setBusy(boolean busy) {
        String icon = busy ? Constants.ICON_LOADING : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }

    protected void showFeedback() {
        SensorModel state = getSelectedState();

        List<SensorModel> sensors = new ArrayList<SensorModel>();
        for (ModelData model : state.getChildren()) {
            sensors.add((SensorModel) model);
        }

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
