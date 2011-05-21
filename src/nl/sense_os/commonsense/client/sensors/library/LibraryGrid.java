package nl.sense_os.commonsense.client.sensors.library;

import java.util.List;

import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.client.env.list.EnvEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.client.sensors.share.SensorShareEvents;
import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.client.states.defaults.StateDefaultsEvents;
import nl.sense_os.commonsense.client.states.list.StateListEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.utility.SenseKeyProvider;
import nl.sense_os.commonsense.client.utility.SensorProcessor;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.models.SensorModel;

import com.extjs.gxt.ui.client.Style.SelectionMode;
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
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LibraryGrid extends View {

    private static final String TAG = "LibraryGrid";
    private ContentPanel panel;
    private BaseListLoader<ListLoadResult<SensorModel>> loader;
    private GroupingStore<SensorModel> store;
    private Grid<SensorModel> grid;
    private ToolBar toolBar;
    private Button shareButton;
    private Button removeButton;
    private Button vizButton;
    private StoreFilterField<SensorModel> filter;
    private ToolBar filterBar;
    private boolean force = true;

    public LibraryGrid(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(LibraryEvents.ShowLibrary)) {
            // Log.d(TAG, "ShowLibrary");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(StateCreateEvents.CreateServiceComplete)
                || type.equals(StateListEvents.RemoveComplete)
                || type.equals(StateDefaultsEvents.CheckDefaultsSuccess)
                || type.equals(SensorDeleteEvents.DeleteSuccess)
                || type.equals(SensorDeleteEvents.DeleteFailure)
                || type.equals(EnvCreateEvents.CreateSuccess)
                || type.equals(EnvEvents.DeleteSuccess)) {
            // Log.d(TAG, "Library changed");
            onLibChanged();

        } else if (type.equals(LibraryEvents.Done)) {
            // Log.d(TAG, "Done");
            setBusy(false);

        } else if (type.equals(LibraryEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusy(true);

        } else if (type.equals(LibraryEvents.ListUpdated)) {
            // Log.d(TAG, "ListUpdated");
            onListUpdate();

        } else if (type.equals(VizEvents.Show)) {
            // Log.d(TAG, "Show Visualization");
            refreshLoader(true);

        } else {
            Log.e(TAG, "Ignoring event... " + event + ", source: " + event.getSource() + ".");
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
        this.panel.getHeader().addTool(refresh);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("Sensor library");

        // track whether the panel is expanded
        this.panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                refreshLoader(false);
            }
        });

        force = true;

        initGrid();
        initFilter();
        initToolBar();
        initHeaderTool();

        // do layout
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
                } else if (record.getPhysicalSensor().contains(filter.toLowerCase())) {
                    return true;
                } else if (record.getDevice() != null
                        && record.getDevice().getType().contains(filter.toLowerCase())) {
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

        // proxy
        DataProxy<ListLoadResult<SensorModel>> proxy = new DataProxy<ListLoadResult<SensorModel>>() {

            @Override
            public void load(DataReader<ListLoadResult<SensorModel>> reader, Object loadConfig,
                    AsyncCallback<ListLoadResult<SensorModel>> callback) {
                // only load when the panel is not collapsed
                if (panel.isExpanded()) {
                    if (loadConfig instanceof ListLoadConfig) {
                        // Log.d(TAG, "Load library... Renew cache: " + force);
                        AppEvent loadRequest = new AppEvent(LibraryEvents.LoadRequest);
                        loadRequest.setData("callback", callback);
                        loadRequest.setData("renewCache", force);
                        fireEvent(loadRequest);
                        force = false;
                    } else {
                        Log.w(TAG, "Unexpected load config: " + loadConfig);
                        callback.onFailure(null);
                    }
                }
            }
        };

        // list loader
        this.loader = new BaseListLoader<ListLoadResult<SensorModel>>(proxy);

        // list store
        this.store = new GroupingStore<SensorModel>(loader);
        this.store.setKeyProvider(new SenseKeyProvider<SensorModel>());

        // Column model
        ColumnModel cm = LibraryColumnsFactory.create();

        // grouping view for the grid
        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(true);
        view.setForceFit(true);
        view.setGroupRenderer(new SensorGroupRenderer(cm));

        this.grid = new Grid<SensorModel>(this.store, cm);
        this.grid.setModelProcessor(new SensorProcessor<SensorModel>());
        this.grid.setView(view);
        this.grid.setBorders(false);
        this.grid.setId("library-grid");
        this.grid.setStateful(true);
        this.grid.setLoadMask(true);
    }

    private void onListUpdate() {
        this.filter.clear();
    }

    private void onLibChanged() {
        refreshLoader(false);
    }

    private void onRemoveClick() {
        // get sensor models from the selection
        final List<SensorModel> sensors = this.grid.getSelectionModel().getSelection();

        if (sensors.size() > 0) {
            AppEvent event = new AppEvent(SensorDeleteEvents.ShowDeleteDialog);
            event.setData("sensors", sensors);
            Dispatcher.forwardEvent(event);

        } else {
            MessageBox.info(null, "No sensors selected. You can only remove sensors!", null);
        }
    }

    private void onShareClick() {
        List<SensorModel> sensors = this.grid.getSelectionModel().getSelection();
        AppEvent shareEvent = new AppEvent(SensorShareEvents.ShowShareDialog);
        shareEvent.setData("sensors", sensors);
        Dispatcher.forwardEvent(shareEvent);
    }

    private void onVizClick() {
        List<SensorModel> selection = this.grid.getSelectionModel().getSelection();
        Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, selection);
    }

    private void refreshLoader(boolean force) {
        this.force = force;
        this.loader.load();
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.panel.getHeader().setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            this.panel.getHeader().setIcon(IconHelper.create(""));
        }
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