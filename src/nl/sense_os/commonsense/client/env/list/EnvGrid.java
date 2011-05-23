package nl.sense_os.commonsense.client.env.list;

import java.util.Arrays;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.models.EnvironmentModel;
import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EnvGrid extends View {

    protected static final Logger logger = Logger.getLogger("EnvGrid");
    private Button createButton;
    private Button deleteButton;
    private Grid<EnvironmentModel> grid;
    private ContentPanel panel;
    private ListStore<EnvironmentModel> store;
    private ListLoader<ListLoadResult<EnvironmentModel>> loader;
    private boolean isListDirty;
    private ToolButton refreshTool;
    private ToolBar toolBar;

    public EnvGrid(Controller controller) {
        super(controller);
    }

    private void createEnvironment() {
        Dispatcher.forwardEvent(EnvCreateEvents.ShowCreator);
    }

    private void deleteEnvironment() {
        AppEvent delete = new AppEvent(EnvEvents.DeleteRequest);
        delete.setData("environment", this.grid.getSelectionModel().getSelectedItem());
        fireEvent(delete);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(EnvEvents.ShowGrid)) {
            // logger.fine( "ShowGrid");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(EnvEvents.ListUpdated)) {
            // logger.fine( "ListUpdated");
            onListUpdated(event);

        } else if (type.equals(VizEvents.Show)) {
            // logger.fine( "Show Visualization");
            refreshLoader(false);

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // logger.fine( "LoggedOut");
            onLoggedOut(event);

        } else if (type.equals(EnvEvents.Working)) {
            // logger.fine( "Working");
            setBusyIcon(true);

        } else if (type.equals(EnvEvents.Done)) {
            // logger.fine( "Working");
            setBusyIcon(false);

        } else if (type.equals(EnvCreateEvents.CreateSuccess)
                || type.equals(EnvEvents.DeleteSuccess)) {
            // logger.fine( "Done");
            this.isListDirty = true;
            refreshLoader(false);

        } else {
            logger.severe("Unexpected event type: " + type);
        }
    }

    private void initGrid() {
        // tree store
        DataProxy<ListLoadResult<EnvironmentModel>> proxy = new DataProxy<ListLoadResult<EnvironmentModel>>() {

            @Override
            public void load(DataReader<ListLoadResult<EnvironmentModel>> reader,
                    Object loadConfig, AsyncCallback<ListLoadResult<EnvironmentModel>> callback) {

                // only load when the panel is not collapsed
                if (loadConfig instanceof ListLoadConfig) {
                    fireEvent(new AppEvent(EnvEvents.ListRequested, callback));
                } else {
                    logger.warning("Unexpected loadconfig: " + loadConfig);
                    callback.onFailure(null);
                }
            }
        };
        this.loader = new BaseListLoader<ListLoadResult<EnvironmentModel>>(proxy);
        this.store = new ListStore<EnvironmentModel>(this.loader);

        ColumnConfig name = new ColumnConfig(EnvironmentModel.NAME, "Name", 100);
        ColumnConfig floors = new ColumnConfig(EnvironmentModel.FLOORS, "Floors", 100);
        ColumnConfig id = new ColumnConfig(EnvironmentModel.ID, "ID", 50);
        id.setHidden(true);
        ColumnConfig outline = new ColumnConfig(EnvironmentModel.OUTLINE, "Outline", 200);
        outline.setHidden(true);
        ColumnConfig position = new ColumnConfig(EnvironmentModel.POSITION, "Position", 100);
        position.setHidden(true);

        ColumnModel cm = new ColumnModel(Arrays.asList(id, name, floors, position, outline));

        this.grid = new Grid<EnvironmentModel>(this.store, cm);
        this.grid.setId("buildingGrid");
        this.grid.setStateful(true);
        this.grid.setLoadMask(true);
        this.grid.setAutoExpandColumn(EnvironmentModel.NAME);
    }

    private void initHeaderTool() {
        refreshTool = new ToolButton("x-tool-refresh");
        refreshTool.addSelectionListener(new SelectionListener<IconButtonEvent>() {

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
        this.panel.setHeading("Manage environments");

        // track whether the panel is expanded
        this.panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                refreshLoader(false);
            }
        });

        initGrid();
        initHeaderTool();
        initToolBar();

        // add grid to panel
        this.panel.setTopComponent(toolBar);
        this.panel.add(this.grid);
        this.panel.getHeader().addTool(this.refreshTool);
    }

    private void initToolBar() {

        final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(EnvGrid.this.createButton)) {
                    createEnvironment();
                } else if (source.equals(EnvGrid.this.deleteButton)) {
                    onDeleteClick();
                } else {
                    logger.warning("Unexpected buttons pressed");
                }
            }
        };

        this.createButton = new Button("Create", l);

        this.deleteButton = new Button("Remove", l);
        this.deleteButton.disable();

        // create tool bar
        toolBar = new ToolBar();
        toolBar.add(this.createButton);
        toolBar.add(this.deleteButton);

        // enable/disable buttons according to grid selection
        GridSelectionModel<EnvironmentModel> selectionModel = new GridSelectionModel<EnvironmentModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel
                .addSelectionChangedListener(new SelectionChangedListener<EnvironmentModel>() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent<EnvironmentModel> se) {
                        EnvironmentModel selection = se.getSelectedItem();
                        if (null != selection) {
                            EnvGrid.this.deleteButton.enable();
                        } else {
                            EnvGrid.this.deleteButton.disable();
                        }
                    }
                });
        this.grid.setSelectionModel(selectionModel);
    }

    private void onDeleteClick() {
        MessageBox.confirm(null, "Are you sure you want to remove this environment?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getText())) {
                            deleteEnvironment();
                        }
                    }
                });
    }

    private void onListUpdated(AppEvent event) {
        this.isListDirty = false;
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void refreshLoader(boolean force) {
        if (force || (this.store.getCount() == 0 || this.isListDirty) && this.panel.isExpanded()) {
            this.loader.load();
        }
    }

    private void setBusyIcon(boolean busy) {
        if (busy) {
            this.panel.getHeader().setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            this.panel.getHeader().setIcon(IconHelper.create(""));
        }
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
        } else {
            logger.severe("Failed to show buildings panel: parent=null");
        }
    }
}
