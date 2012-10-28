package nl.sense_os.commonsense.main.client.states.connect;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.util.LibraryColumnsFactory;
import nl.sense_os.commonsense.main.client.gxt.util.SenseKeyProvider;
import nl.sense_os.commonsense.main.client.gxt.util.SensorGroupRenderer;
import nl.sense_os.commonsense.main.client.gxt.util.SensorOwnerFilter;
import nl.sense_os.commonsense.main.client.gxt.util.SensorProcessor;
import nl.sense_os.commonsense.main.client.gxt.util.SensorTextFilter;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateConnecter extends View {

    private static final Logger LOG = Logger.getLogger(StateConnecter.class.getName());
    private Window window;
    private FormPanel form;
    private Button submitButton;
    private Button cancelButton;
    private GroupingStore<GxtSensor> store;
    private ListLoader<ListLoadResult<GxtSensor>> loader;
    private GxtSensor stateSensor;
    private String serviceName;
    private MessageBox waitDialog;
    private Grid<GxtSensor> grid;
    private ToolBar filterBar;

    public StateConnecter(Controller c) {
        super(c);
        // LOG.setLevel(Level.WARNING);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateConnectEvents.ShowSensorConnecter)) {
            LOG.fine("Show");
            onShow(event);

        } else if (type.equals(StateConnectEvents.ConnectSuccess)) {
            LOG.fine("ConnectSuccess");
            hideWindow();

        } else if (type.equals(StateConnectEvents.ConnectFailure)) {
            LOG.warning("ConnectFailure");
            onConnectFailure();

        } else if (type.equals(StateConnectEvents.ServiceNameSuccess)) {
            LOG.fine("ServiceNameSuccess");
            final String serviceName = event.getData("name");
            onServiceNameSuccess(serviceName);

        } else if (type.equals(StateConnectEvents.ServiceNameFailure)) {
            LOG.warning("ServiceNameFailure");
            onServiceNameFailure();

        } else {
            LOG.warning("Unexpected event type: " + type);
        }
    }

    private void hideWindow() {
        window.hide();
    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button pressed = ce.getButton();
                if (pressed.equals(submitButton)) {
                    if (form.isValid()) {
                        submitForm();
                    }
                } else if (pressed.equals(cancelButton)) {
                    hideWindow();
                } else {
                    LOG.warning("Unexpected button pressed");
                }
            }
        };
        submitButton = new Button("Connect", l);
        submitButton.setIconStyle("sense-btn-icon-go");
        cancelButton = new Button("Cancel", l);

        form.addButton(submitButton);
        form.addButton(cancelButton);

        // handle selections
        GridSelectionModel<GxtSensor> selectionModel = new GridSelectionModel<GxtSensor>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<GxtSensor>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GxtSensor> se) {
                GxtSensor selection = se.getSelectedItem();
                if (null != selection) {
                    submitButton.enable();
                } else {
                    submitButton.disable();
                }
            }
        });
        grid.setSelectionModel(selectionModel);
    }

    private void initFields() {

        initGrid();
        initFilters();

        ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeaderVisible(false);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.setTopComponent(filterBar);
        panel.add(grid);

        AdapterField field = new AdapterField(panel);
        field.setHeight(380);
        field.setResizeWidget(true);
        field.setFieldLabel("Select a sensor to use as input for the state sensor");

        final FormData formData = new FormData("-10");
        form.add(field, formData);
    }

    /**
     * Initializes filter toolbar for the grid with sensors. The bar contains text filter and an
     * owner filter.
     */
    private void initFilters() {

        // text filter
        SensorTextFilter<GxtSensor> textFilter = new SensorTextFilter<GxtSensor>();
        textFilter.bind(store);

        // filter to show only my own sensors
        final SensorOwnerFilter<GxtSensor> ownerFilter = new SensorOwnerFilter<GxtSensor>();
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

    private void initForm() {
        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setLabelAlign(LabelAlign.TOP);
        form.setBodyBorder(false);
        // this.form.setFieldWidth(275);

        initFields();
        initButtons();

        window.add(form);
    }

    private void initGrid() {

        // proxy
        DataProxy<ListLoadResult<GxtSensor>> proxy = new DataProxy<ListLoadResult<GxtSensor>>() {

            @Override
            public void load(DataReader<ListLoadResult<GxtSensor>> reader, Object loadConfig,
                    AsyncCallback<ListLoadResult<GxtSensor>> callback) {
                // only load when the panel is not collapsed
                if (loadConfig instanceof ListLoadConfig) {
                    LOG.finest("Load library...");
                    AppEvent loadRequest = new AppEvent(
                            StateConnectEvents.AvailableSensorsRequested);
                    loadRequest.setData("name", serviceName);
                    loadRequest.setData("callback", callback);
                    fireEvent(loadRequest);
                } else {
                    LOG.warning("Unexpected load config: " + loadConfig);
                    callback.onFailure(null);
                }
            }
        };

        // list loader
        loader = new BaseListLoader<ListLoadResult<GxtSensor>>(proxy);

        // list store
        store = new GroupingStore<GxtSensor>(loader);
        store.setKeyProvider(new SenseKeyProvider<GxtSensor>());
        store.setMonitorChanges(true);

        // Column model
        ColumnModel cm = LibraryColumnsFactory.create();

        // grouping view for the grid
        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(true);
        view.setForceFit(true);
        view.setGroupRenderer(new SensorGroupRenderer(cm));

        grid = new Grid<GxtSensor>(store, cm);
        grid.setModelProcessor(new SensorProcessor<GxtSensor>());
        grid.setView(view);
        grid.setBorders(false);
        grid.setId("state-connecter-grid");
        grid.setStateful(true);
        grid.setLoadMask(true);
    }

    @Override
    protected void initialize() {
        super.initialize();

        window = new CenteredWindow();
        window.setHeading("Connect sensor(s) to state");
        window.setSize(540, 480);
        window.setLayout(new FitLayout());

        initForm();
    }

    private void onConnectFailure() {
        setBusy(false);
        MessageBox.confirm(null, "Connect failed, retry?", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                    submitForm();
                } else {
                    hideWindow();
                }
            }
        });
    }

    private void onServiceNameFailure() {

        waitDialog.close();

        MessageBox.confirm(null, "Failed to get state service name, retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            requestServiceName();
                        } else {
                            hideWindow();
                        }
                    }
                });
    }

    private void onServiceNameSuccess(String serviceName) {
        this.serviceName = serviceName;

        waitDialog.close();

        store.removeAll();
        refreshLoader();

        submitButton.disable();
        setBusy(false);
        window.show();
        window.center();
    }

    private void onShow(AppEvent event) {
        stateSensor = event.getData();

        requestServiceName();
    }

    private void refreshLoader() {
        loader.load();
    }

    private void requestServiceName() {
        waitDialog = MessageBox.wait(null, "Please wait.", "Getting state sensor details...");

        AppEvent request = new AppEvent(StateConnectEvents.ServiceNameRequest);
        request.setData("stateSensor", stateSensor);
        fireEvent(request);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            submitButton.setIconStyle("sense-btn-icon-loading");
            cancelButton.disable();
        } else {
            submitButton.setIconStyle("sense-btn-icon-go");
            cancelButton.enable();
        }
    }

    private void submitForm() {
        TreeModel sensor = grid.getSelectionModel().getSelectedItem();
        AppEvent event = new AppEvent(StateConnectEvents.ConnectRequested);
        event.setData("stateSensor", stateSensor);
        event.setData("serviceName", serviceName);
        event.setData("sensor", sensor);
        Dispatcher.forwardEvent(event);

        setBusy(true);
    }
}
