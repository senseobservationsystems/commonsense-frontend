package nl.sense_os.commonsense.client.sensors.library;

import java.util.List;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.client.env.list.EnvEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
import nl.sense_os.commonsense.client.sensors.share.SensorShareEvents;
import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
import nl.sense_os.commonsense.client.states.defaults.StateDefaultsEvents;
import nl.sense_os.commonsense.client.states.list.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
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
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SensorLibrary extends View {

    private static final String TAG = "SensorLibrary";
    private ContentPanel panel;
    private ListLoader<ListLoadResult<SensorModel>> loader;
    private GroupingStore<SensorModel> store;
    private Grid<SensorModel> grid;
    private ToolBar toolBar;
    private ToolButton refreshButton;
    private Button shareButton;
    private Button removeButton;
    private Button vizButton;
    private StoreFilterField<SensorModel> filter;
    private ToolBar filterBar;
    private boolean isLibraryDirty = false;

    public SensorLibrary(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(SensorLibraryEvents.ShowLibrary)) {
            // Log.d(TAG, "ShowTree");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(StateCreateEvents.CreateServiceComplete)
                || type.equals(StateEvents.RemoveComplete)
                || type.equals(StateDefaultsEvents.CheckDefaultsSuccess)
                || type.equals(SensorDeleteEvents.DeleteSuccess)
                || type.equals(SensorDeleteEvents.DeleteFailure)
                || type.equals(EnvCreateEvents.CreateSuccess)
                || type.equals(EnvEvents.DeleteSuccess)) {
            // Log.d(TAG, "Library changed");
            onLibChanged();

        } else if (type.equals(SensorLibraryEvents.Done)) {
            // Log.d(TAG, "TreeUpdated");
            setBusy(false);

        } else if (type.equals(SensorLibraryEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusy(true);

        } else if (type.equals(SensorLibraryEvents.ListUpdated)) {
            // Log.d(TAG, "ListUpdated");
            onListUpdate();

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
        this.panel.setHeading("Sensor library");

        // track whether the panel is expanded
        this.panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                refreshLoader(false);
            }
        });

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
        // tree store
        DataProxy<ListLoadResult<SensorModel>> proxy = new DataProxy<ListLoadResult<SensorModel>>() {

            @Override
            public void load(DataReader<ListLoadResult<SensorModel>> reader, Object loadConfig,
                    AsyncCallback<ListLoadResult<SensorModel>> callback) {
                // only load when the panel is not collapsed
                if (loadConfig instanceof ListLoadConfig) {
                    fireEvent(new AppEvent(SensorLibraryEvents.ListRequested, callback));
                } else {
                    Log.w(TAG, "Unexpected loadconfig: " + loadConfig);
                    callback.onFailure(null);
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
        ColumnModel cm = LibraryColumnsFactory.create();

        GroupingView groupingView = new GroupingView();
        groupingView.setShowGroupedColumn(true);
        groupingView.setForceFit(true);
        groupingView.setGroupRenderer(new GridGroupRenderer() {

            public String render(GroupColumnData data) {

                String field = data.group;
                if (data.field.equals(SensorModel.TYPE)) {
                    int group = Integer.parseInt(data.group);
                    switch (group) {
                        case 0 :
                            field = "Feeds";
                            break;
                        case 1 :
                            field = "Physical";
                            break;
                        case 2 :
                            field = "States";
                            break;
                        case 3 :
                            field = "Environment sensors";
                            break;
                        case 4 :
                            field = "Public sensors";
                            break;
                        default :
                            field = "Unsorted";
                    }
                } else if (data.field.equals("dev_uuid")) {

                } else {
                    if (data.group.equals("")) {
                        return "Ungrouped";
                    } else {
                        return data.group;
                    }
                }

                String count = data.models.size() == 1 ? "Sensor" : "Sensors";
                return field + " (" + data.models.size() + " " + count + ")";
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
        this.isLibraryDirty = false;
    }

    private void onLibChanged() {
        this.isLibraryDirty = true;
        refreshLoader(false);
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onRemoveClick() {
        Log.d(TAG, "OnRemoveClick");

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

    protected void onShareClick() {
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
        if (force || (this.store.getCount() == 0 || this.isLibraryDirty) && this.panel.isExpanded()) {
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
