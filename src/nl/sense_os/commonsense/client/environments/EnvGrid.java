package nl.sense_os.commonsense.client.environments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EnvGrid extends View {

    protected static final String TAG = "EnvGrid";
    private Button createButton;
    private Button deleteButton;
    private TreeGrid<TreeModel> grid;
    private Button importButton;
    private ContentPanel panel;
    private boolean isCollapsed;
    private TreeStore<TreeModel> store;
    private BaseTreeLoader<TreeModel> loader;

    public EnvGrid(Controller controller) {
        super(controller);
    }

    protected void create() {
        // fireEvent(EnvEvents.ShowCreator);
    }

    protected void delete() {
        Log.w(TAG, "Delete button logic not implemented");
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(EnvEvents.ShowGrid)) {
            // Log.d(TAG, "ShowGrid");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(EnvEvents.ListNotUpdated)) {
            Log.w(TAG, "ListNotUpdated");
            onGroupsNotUpdated(event);

        } else if (type.equals(EnvEvents.ListUpdated)) {
            Log.d(TAG, "ListUpdated");
            onListUpdated(event);

        } else if (type.equals(VizEvents.Show)) {
            // Log.d(TAG, "Show Visualization");
            refreshLoader();

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);

        } else if (type.equals(EnvEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusyIcon(true);

        } else {
            Log.e(TAG, "Unexpected event type: " + type);
        }
    }

    private void importEnvironment() {
        Log.w(TAG, "Import button logic not implemented");
    }

    private void initGrid() {
        // tree store
        @SuppressWarnings({"unchecked", "rawtypes"})
        DataProxy proxy = new DataProxy() {

            @Override
            public void load(DataReader reader, Object loadConfig, AsyncCallback callback) {
                // only load when the panel is not collapsed
                if (false == isCollapsed) {
                    if (null == loadConfig) {
                        Dispatcher.forwardEvent(EnvEvents.ListRequested, callback);
                    } else if (loadConfig instanceof TreeModel) {
                        List<ModelData> childrenModels = ((TreeModel) loadConfig).getChildren();
                        callback.onSuccess(childrenModels);
                    } else {
                        callback.onSuccess(new ArrayList<TreeModel>());
                    }
                }
            }
        };
        this.loader = new BaseTreeLoader<TreeModel>(proxy);
        this.store = new TreeStore<TreeModel>(loader);

        ColumnConfig email = new ColumnConfig("email", "Email", 100);
        ColumnConfig name = new ColumnConfig("name", "Name", 100);

        name.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnModel cm = new ColumnModel(Arrays.asList(name, email));

        this.grid = new TreeGrid<TreeModel>(this.store, cm);
        this.grid.setId("buildingGrid");
        this.grid.setStateful(true);

        TreeGridSelectionModel<TreeModel> selectionModel = new TreeGridSelectionModel<TreeModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                TreeModel selection = se.getSelectedItem();
                if (null != selection) {
                    deleteButton.enable();
                    importButton.enable();
                } else {
                    deleteButton.disable();
                    importButton.disable();
                }
            }
        });
        this.grid.setSelectionModel(selectionModel);

        // add grid to panel
        this.panel.add(this.grid);
    }

    private void initHeaderTool() {
        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                loader.load();
            }
        });

        // add to panel
        this.panel.getHeader().addTool(refresh);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("Manage environments");

        // track whether the panel is expanded
        Listener<ComponentEvent> collapseListener = new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                EventType type = be.getType();
                if (type.equals(Events.Expand)) {
                    isCollapsed = false;
                    refreshLoader();
                } else if (type.equals(Events.Collapse)) {
                    isCollapsed = true;
                }
            }
        };
        panel.addListener(Events.Expand, collapseListener);
        panel.addListener(Events.Collapse, collapseListener);

        initHeaderTool();
        initToolBar();
        initGrid();
    }

    private void initToolBar() {

        final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(createButton)) {
                    create();
                } else if (source.equals(deleteButton)) {
                    onDeleteClick();
                } else if (source.equals(importButton)) {
                    importEnvironment();
                }
            }
        };

        this.createButton = new Button("Create", l);

        this.importButton = new Button("Import", l);
        this.importButton.disable();

        this.deleteButton = new Button("Remove", l);
        this.deleteButton.disable();

        // create tool bar
        final ToolBar toolBar = new ToolBar();
        toolBar.add(this.importButton);
        toolBar.add(this.createButton);
        toolBar.add(this.deleteButton);

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    private void onDeleteClick() {
        MessageBox.confirm(null, "Are you sure you want to remove this environment?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getText())) {
                            delete();
                        }
                    }
                });
    }

    private void onGroupsNotUpdated(AppEvent event) {
        // Throwable caught = event.<Throwable> getData();
        setBusyIcon(false);
        this.store.removeAll();
    }

    private void onListUpdated(AppEvent event) {
        List<TreeModel> groups = event.<List<TreeModel>> getData();
        setBusyIcon(false);
        this.store.removeAll();
        if (null != groups) {
            this.store.add(groups, true);
        }
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    protected void refreshLoader() {
        if (this.store.getChildCount() == 0) {
            loader.load();
        }
    }

    private void setBusyIcon(boolean busy) {
        String icon = busy ? Constants.ICON_LOADING : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
        } else {
            Log.e(TAG, "Failed to show buildings panel: parent=null");
        }
    }
}
