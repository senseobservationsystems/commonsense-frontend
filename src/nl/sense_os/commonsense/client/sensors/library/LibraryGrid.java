package nl.sense_os.commonsense.client.sensors.library;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.common.utility.SenseKeyProvider;
import nl.sense_os.commonsense.client.common.utility.SensorOwnerFilter;
import nl.sense_os.commonsense.client.common.utility.SensorProcessor;
import nl.sense_os.commonsense.client.common.utility.SensorTextFilter;
import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.client.env.list.EnvEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.client.sensors.share.SensorShareEvents;
import nl.sense_os.commonsense.client.sensors.unshare.UnshareEvents;
import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.client.states.defaults.StateDefaultsEvents;
import nl.sense_os.commonsense.client.states.list.StateListEvents;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
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
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LibraryGrid extends View {

    private static final Logger LOG = Logger.getLogger(LibraryGrid.class.getName());
    private ContentPanel panel;
    private BaseListLoader<ListLoadResult<SensorModel>> loader;
    private GroupingStore<SensorModel> store;
    private Grid<SensorModel> grid;
    private ToolBar toolBar;
    private Button shareButton;
    private Button unshareButton;
    private Button removeButton;
    private Button vizButton;
    private ToolBar filterBar;
    private boolean forceRefresh = true;

    public LibraryGrid(Controller controller) {
        super(controller);
        // LOG.setLevel(Level.ALL);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(LibraryEvents.ShowLibrary)) {
            LOG.finest("ShowLibrary");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(StateListEvents.RemoveComplete)
                || type.equals(StateDefaultsEvents.CheckDefaultsSuccess)
                || type.equals(SensorDeleteEvents.DeleteSuccess)
                || type.equals(SensorDeleteEvents.DeleteFailure)
                || type.equals(EnvCreateEvents.CreateSuccess)
                || type.equals(EnvEvents.DeleteSuccess)
                || type.equals(UnshareEvents.UnshareComplete)
                || type.equals(SensorShareEvents.ShareComplete)) {
            LOG.finest("Library changed");
            onLibChanged();

        } else if (type.equals(StateCreateEvents.CreateServiceComplete)) {
            LOG.finest("CreateServiceComplete");
            refreshLoader(true);

        } else if (type.equals(LibraryEvents.Done)) {
            LOG.finest("Done");
            setBusy(false);

        } else if (type.equals(LibraryEvents.Working)) {
            LOG.finest("Working");
            setBusy(true);

        } else if (type.equals(LibraryEvents.ListUpdated)) {
            LOG.finest("ListUpdated");
            onListUpdate();

        } else if (type.equals(VizEvents.Show)) {
            LOG.finest("Show Visualization");
            refreshLoader(true);

        } else {
            LOG.severe("Unexpected event: " + event);
        }
    }

    /**
     * Initializes filter toolbar for the grid with sensors. The bar contains text filter and an
     * owner filter.
     */
    private void initFilters() {

        // text filter
        SensorTextFilter<SensorModel> textFilter = new SensorTextFilter<SensorModel>();
        textFilter.bind(store);

        // filter to show only my own sensors
        final SensorOwnerFilter<SensorModel> ownerFilter = new SensorOwnerFilter<SensorModel>();
        store.addFilter(ownerFilter);

        // checkbox to toggle filter
        final CheckBox filterOnlyMe = new CheckBox();
        filterOnlyMe.setBoxLabel("Only my own sensors");
        filterOnlyMe.setHideLabel(true);
        filterOnlyMe.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {

                ownerFilter.setEnabled(filterOnlyMe.getValue());
                store.applyFilters(null);
            }
        });

        // add filters to filter bar
        filterBar = new ToolBar();
        filterBar.add(new LabelToolItem("Filter: "));
        filterBar.add(textFilter);
        filterBar.add(new SeparatorToolItem());
        filterBar.add(filterOnlyMe);
    }

    private void initGrid() {

        // proxy
        DataProxy<ListLoadResult<SensorModel>> proxy = new DataProxy<ListLoadResult<SensorModel>>() {

            @Override
            public void load(DataReader<ListLoadResult<SensorModel>> reader, Object loadConfig,
                    AsyncCallback<ListLoadResult<SensorModel>> callback) {
                // only load when the panel is not collapsed
                if (panel.isExpanded()) {
                    if (loadConfig instanceof ListLoadConfig) {
                        // LOG.fine( "Load library... Renew cache: " + forceRefresh);
                        AppEvent loadRequest = new AppEvent(LibraryEvents.LoadRequest);
                        loadRequest.setData("callback", callback);
                        loadRequest.setData("renewCache", forceRefresh);
                        fireEvent(loadRequest);
                        forceRefresh = false;
                    } else {
                        LOG.warning("Unexpected load config: " + loadConfig);
                        callback.onFailure(null);
                    }
                } else {
                    LOG.warning("failed to load data: panel is not expanded...");
                    callback.onFailure(null);
                }
            }
        };

        // list loader
        loader = new BaseListLoader<ListLoadResult<SensorModel>>(proxy);

        // list store
        store = new GroupingStore<SensorModel>(loader);
        store.setKeyProvider(new SenseKeyProvider<SensorModel>());
        store.setMonitorChanges(true);

        // this.store.groupBy(SensorModel.DEVICE_TYPE, true);
        store.sort(SensorModel.DISPLAY_NAME, SortDir.ASC);
        store.setDefaultSort(SensorModel.DISPLAY_NAME, SortDir.ASC);

        // Column model
        ColumnModel cm = LibraryColumnsFactory.create();

        // grouping view for the grid
        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(true);
        view.setForceFit(true);
        view.setGroupRenderer(new SensorGroupRenderer(cm));
        view.setStartCollapsed(true);

        grid = new Grid<SensorModel>(store, cm);
        grid.setModelProcessor(new SensorProcessor<SensorModel>());
        grid.setView(view);
        grid.setBorders(false);
        grid.setId("library-grid");
        grid.setStateful(true);
        grid.setLoadMask(true);
    }

    private void initHeaderTool() {
        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                refreshLoader(true);
            }
        });
        panel.getHeader().addTool(refresh);
    }

    @Override
    protected void initialize() {
        LOG.finest("Initialize...");

        panel = new ContentPanel(new FitLayout());
        panel.setHeading("Sensor library");
        panel.setAnimCollapse(false);

        // track whether the panel is expanded
        panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                refreshLoader(false);
            }
        });

        forceRefresh = true;

        initGrid();
        initFilters();
        initToolBar();
        initHeaderTool();

        // do layout
        panel.setTopComponent(toolBar);
        ContentPanel content = new ContentPanel(new FitLayout());
        content.setBodyBorder(false);
        content.setHeaderVisible(false);
        content.setTopComponent(filterBar);
        content.add(grid);
        panel.add(content);

        setupDragDrop();

        super.initialize();
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
                } else if (source.equals(unshareButton)) {
                    onUnshareClick();
                } else if (source.equals(removeButton)) {
                    onRemoveClick();
                } else {
                    LOG.warning("Unexpected button pressed");
                }
            }
        };

        // initialize the buttons
        vizButton = new Button("Visualize", l);
        vizButton.disable();

        shareButton = new Button("Share", l);
        shareButton.disable();

        unshareButton = new Button("Unshare", l);
        unshareButton.disable();

        removeButton = new Button("Remove", l);
        removeButton.disable();

        // listen to selection of tree items to enable/disable buttons
        GridSelectionModel<SensorModel> selectionModel = new GridSelectionModel<SensorModel>();
        selectionModel.setSelectionMode(SelectionMode.MULTI);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<SensorModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<SensorModel> se) {
                List<SensorModel> selection = se.getSelection();
                if (selection != null && selection.size() > 0) {
                    vizButton.enable();
                    shareButton.enable();
                    if (selection.size() == 1 && selection.get(0).getUsers() != null
                            && selection.get(0).getUsers().size() > 0) {
                        unshareButton.enable();
                    } else {
                        unshareButton.disable();
                    }
                    removeButton.enable();
                } else {
                    vizButton.disable();
                    shareButton.disable();
                    unshareButton.disable();
                    removeButton.disable();
                }
            }
        });
        grid.setSelectionModel(selectionModel);

        // create tool bar
        toolBar = new ToolBar();
        toolBar.add(vizButton);
        toolBar.add(shareButton);
        toolBar.add(unshareButton);
        toolBar.add(removeButton);
    }

    private void onLibChanged() {
        refreshLoader(false);
    }

    private void onListUpdate() {
        // re-filter the sensors store
        store.clearFilters();
        store.applyFilters(null);
    }

    private void onRemoveClick() {
        // get sensor models from the selection
        final List<SensorModel> sensors = grid.getSelectionModel().getSelection();

        if (sensors.size() > 0) {
            AppEvent event = new AppEvent(SensorDeleteEvents.ShowDeleteDialog);
            event.setData("sensors", sensors);
            Dispatcher.forwardEvent(event);

        } else {
            MessageBox.info(null, "No sensors selected. You can only remove sensors!", null);
        }
    }

    private void onShareClick() {
        List<SensorModel> sensors = grid.getSelectionModel().getSelection();
        AppEvent shareEvent = new AppEvent(SensorShareEvents.ShowShareDialog);
        shareEvent.setData("sensors", sensors);
        Dispatcher.forwardEvent(shareEvent);
    }

    private void onUnshareClick() {
        AppEvent shareEvent = new AppEvent(UnshareEvents.ShowUnshareDialog);
        shareEvent.setData("sensor", grid.getSelectionModel().getSelectedItem());
        Dispatcher.forwardEvent(shareEvent);
    }

    private void onVizClick() {
        List<SensorModel> selection = grid.getSelectionModel().getSelection();
        Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, selection);
    }

    private void refreshLoader(boolean force) {
        this.forceRefresh = this.forceRefresh || force;
        loader.load();
    }

    private void setBusy(boolean busy) {
        if (busy) {
            if (!SenseIconProvider.ICON_LOADING.equals(panel.getHeader().getIcon())) {
                panel.getHeader().setIcon(SenseIconProvider.ICON_LOADING);
            }
        } else {
            panel.getHeader().setIcon(IconHelper.create(""));
        }
    }

    /**
     * Sets up the grid for drag and drop of the sensors.
     */
    private void setupDragDrop() {
        new GridDragSource(grid);
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(panel);
            parent.layout();
        } else {
            LOG.severe("Failed to show my sensors panel: parent=null");
        }
    }
}
