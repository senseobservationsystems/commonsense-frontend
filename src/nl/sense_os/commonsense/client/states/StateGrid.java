package nl.sense_os.commonsense.client.states;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.utility.SensorKeyProvider;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
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
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
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
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateGrid extends View {

    protected static final String TAG = "StateGrid";
    private ContentPanel panel;
    private boolean isCollapsed;
    private TreeGrid<TreeModel> grid;
    private TreeStore<TreeModel> store;
    private BaseTreeLoader<TreeModel> loader;
    private MenuItem createButton;
    private MenuItem disconnectButton;
    private MenuItem connectButton;
    private MenuItem editButton;
    private MenuItem feedbackButton;

    public StateGrid(Controller controller) {
        super(controller);
    }

    private void confirmRemove() {
        MessageBox.confirm(null, "Are you sure you want to remove this state sensor?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getText())) {
                            removeService();
                        }
                    }
                });
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(StateEvents.ShowGrid)) {
            // Log.d(TAG, "ShowGrid");
            onShow(event);

        } else if (type.equals(VizEvents.Show)) {
            // Log.d(TAG, "Show Visualization");
            refreshLoader(false);

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);

        } else if (type.equals(StateEvents.Done)) {
            // Log.d(TAG, "ListUpdated");
            setBusy(false);

        } else if (type.equals(StateEvents.RemoveComplete)) {
            // Log.d(TAG, "RemoveComplete");
            onRemoveComplete(event);

        } else if (type.equals(StateEvents.RemoveFailed)) {
            Log.w(TAG, "RemoveFailed");
            onRemoveFailed(event);

        } else if (type.equals(StateEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusy(true);

        } else if (type.equals(StateEvents.ConnectComplete)) {
            // Log.d(TAG, "ConnectComplete");
            refreshLoader(true);

        } else if (type.equals(StateEvents.CreateServiceComplete)) {
            // Log.d(TAG, "CreateServiceComplete");
            refreshLoader(true);

        } else {
            Log.e(TAG, "Unexpected event type: " + type);
        }
    }

    private void initGrid() {
        // tree store
        RpcProxy<List<TreeModel>> proxy = new RpcProxy<List<TreeModel>>() {

            @Override
            public void load(Object loadConfig, AsyncCallback<List<TreeModel>> callback) {
                // only load when the panel is not collapsed
                if (false == isCollapsed) {
                    if (null == loadConfig) {
                        Dispatcher.forwardEvent(StateEvents.ListRequested, callback);
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

        ColumnConfig id = new ColumnConfig("id", "Id", 50);
        ColumnConfig name = new ColumnConfig("name", "Name", 150);

        name.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnModel cm = new ColumnModel(Arrays.asList(name, id));

        this.grid = new TreeGrid<TreeModel>(this.store, cm);
        this.grid.setId("stateGrid");
        this.grid.setAutoLoad(true);
        this.grid.setAutoExpandColumn("name");
        this.grid.setStateful(true);
        this.grid.setIconProvider(new SensorIconProvider());

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

        // add grid to panel
        ContentPanel content = new ContentPanel(new FitLayout());
        content.setBodyBorder(false);
        content.setHeaderVisible(false);
        content.setTopComponent(filterBar);
        content.add(this.grid);

        this.panel.add(content);
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
        initHeaderTool();
        initToolBar();
    }

    private void initToolBar() {
        TreeGridSelectionModel<TreeModel> selectionModel = new TreeGridSelectionModel<TreeModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                TreeModel selection = se.getSelectedItem();
                if (null != selection) {
                    if (selection.get("service_name") == null) {
                        editButton.enable();
                        feedbackButton.enable();
                        connectButton.enable();
                        disconnectButton.enable();
                    } else {
                        editButton.enable();
                        feedbackButton.enable();
                        connectButton.enable();
                        disconnectButton.disable();
                    }
                } else {
                    editButton.enable();
                    feedbackButton.enable();
                    connectButton.disable();
                    disconnectButton.disable();
                }
            }
        });
        this.grid.setSelectionModel(selectionModel);

        final SelectionListener<MenuEvent> l = new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent me) {
                MenuItem source = (MenuItem) me.getItem();
                if (source.equals(createButton)) {
                    onCreateClick();
                } else if (source.equals(editButton)) {
                    onEditClick();
                } else if (source.equals(connectButton)) {
                    onAddClick();
                } else if (source.equals(disconnectButton)) {
                    confirmRemove();
                } else if (source.equals(feedbackButton)) {
                    showFeedback();
                } else {
                    Log.w(TAG, "Unexpected button clicked");
                }
            }
        };

        // menu item for editing service stuff
        Menu serviceMenu = new Menu();

        this.createButton = new MenuItem("New State", l);
        serviceMenu.add(createButton);

        this.editButton = new MenuItem("Algorithm Parameters", l);
        this.editButton.disable();
        serviceMenu.add(editButton);

        this.feedbackButton = new MenuItem("Give Feedback", l);
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

    protected void onAddClick() {
        TreeModel selectedService = this.grid.getSelectionModel().getSelectedItem();
        if (selectedService.get("service_name") == null) {
            selectedService = selectedService.getParent();
        }
        Dispatcher.forwardEvent(StateEvents.ShowSensorConnecter, selectedService);
    }

    protected void onCreateClick() {
        Dispatcher.forwardEvent(StateEvents.ShowCreator);
    }

    protected void onEditClick() {
        TreeModel selectedService = this.grid.getSelectionModel().getSelectedItem();
        if (selectedService.getParent() != null) {
            selectedService = selectedService.getParent();
        }
        AppEvent event = new AppEvent(StateEvents.ShowEditor);
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
                            removeService();
                        }
                    }
                });
    }

    private void onShow(AppEvent event) {
        ContentPanel parent = event.<ContentPanel> getData();
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
        } else {
            Log.e(TAG, "Failed to show states panel: parent=null");
        }
    }

    private void refreshLoader(boolean force) {
        if (force || this.store.getChildCount() == 0) {
            loader.load();
        }
    }

    protected void removeService() {
        TreeModel sensor = this.grid.getSelectionModel().getSelectedItem();
        TreeModel service = sensor.getParent();

        AppEvent event = new AppEvent(StateEvents.RemoveRequested);
        event.setData("sensor", sensor);
        event.setData("service", service);
        Dispatcher.forwardEvent(event);
        setBusy(true);
    }

    private void setBusy(boolean busy) {
        String icon = busy ? Constants.ICON_LOADING : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }

    protected void showFeedback() {
        TreeModel selected = this.grid.getSelectionModel().getSelectedItem();
        while (selected.getParent() != null) {
            selected = selected.getParent();
        }

        AppEvent event = new AppEvent(StateEvents.ShowFeedback);
        event.setData("service", selected);
        Dispatcher.forwardEvent(event);
    }
}
