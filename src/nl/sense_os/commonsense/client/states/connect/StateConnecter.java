package nl.sense_os.commonsense.client.states.connect;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.sensors.library.LibraryColumnsFactory;
import nl.sense_os.commonsense.client.sensors.library.SensorGroupRenderer;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.utility.SenseKeyProvider;
import nl.sense_os.commonsense.client.utility.SensorProcessor;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
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
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateConnecter extends View {

    private static final Logger logger = Logger.getLogger("StateConnecter");
    private Window window;
    private FormPanel form;
    private Button submitButton;
    private Button cancelButton;
    private GroupingStore<SensorModel> store;
    private ListLoader<ListLoadResult<SensorModel>> loader;
    private TreeModel service;
    private String serviceName;
    private MessageBox waitDialog;
    private Grid<SensorModel> grid;

    public StateConnecter(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateConnectEvents.ShowSensorConnecter)) {
            // logger.fine( "Show");
            onShow(event);

        } else if (type.equals(StateConnectEvents.ConnectSuccess)) {
            // logger.fine( "ConnectSuccess");
            hideWindow();

        } else if (type.equals(StateConnectEvents.ConnectFailure)) {
            logger.warning("ConnectFailure");
            onConnectFailure();

        } else if (type.equals(StateConnectEvents.ServiceNameSuccess)) {
            // logger.fine( "ServiceNameSuccess");
            final String serviceName = event.getData("name");
            onServiceNameSuccess(serviceName);

        } else if (type.equals(StateConnectEvents.ServiceNameFailure)) {
            logger.warning("ServiceNameFailure");
            onServiceNameFailure();

        } else {
            logger.warning("Unexpected event type: " + type);
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
                    logger.warning("Unexpected button pressed");
                }
            }
        };
        this.submitButton = new Button("Connect", SenseIconProvider.ICON_BUTTON_GO, l);
        this.cancelButton = new Button("Cancel", l);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.submitButton);
        this.form.addButton(this.cancelButton);

        // handle selections
        GridSelectionModel<SensorModel> selectionModel = new GridSelectionModel<SensorModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<SensorModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<SensorModel> se) {
                SensorModel selection = se.getSelectedItem();
                if (null != selection) {
                    submitButton.enable();
                } else {
                    submitButton.disable();
                }
            }
        });
        this.grid.setSelectionModel(selectionModel);
    }

    private void initFields() {

        initGrid();

        ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeaderVisible(false);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.add(this.grid);

        AdapterField field = new AdapterField(panel);
        field.setHeight(150);
        field.setResizeWidget(true);
        field.setFieldLabel("Select the sensor to connect to the service");

        final FormData formData = new FormData("-10");
        this.form.add(field, formData);
    }

    private void initForm() {
        this.form = new FormPanel();
        this.form.setHeaderVisible(false);
        this.form.setLabelAlign(LabelAlign.TOP);
        this.form.setBodyBorder(false);
        // this.form.setFieldWidth(275);

        initFields();
        initButtons();

        this.window.add(this.form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Connect sensor(s) to state");
        this.window.setSize(404, 250);
        this.window.setLayout(new FitLayout());

        initForm();
    }

    private void initGrid() {

        // proxy
        DataProxy<ListLoadResult<SensorModel>> proxy = new DataProxy<ListLoadResult<SensorModel>>() {

            @Override
            public void load(DataReader<ListLoadResult<SensorModel>> reader, Object loadConfig,
                    AsyncCallback<ListLoadResult<SensorModel>> callback) {
                // only load when the panel is not collapsed
                if (loadConfig instanceof ListLoadConfig) {
                    // logger.fine( "Load library... Renew cache: " + force);
                    AppEvent loadRequest = new AppEvent(
                            StateConnectEvents.AvailableSensorsRequested);
                    loadRequest.setData("name", serviceName);
                    loadRequest.setData("callback", callback);
                    fireEvent(loadRequest);
                } else {
                    logger.warning("Unexpected load config: " + loadConfig);
                    callback.onFailure(null);
                }
            }
        };

        // list loader
        this.loader = new BaseListLoader<ListLoadResult<SensorModel>>(proxy);

        // list store
        this.store = new GroupingStore<SensorModel>(loader);
        this.store.setKeyProvider(new SenseKeyProvider<SensorModel>());
        this.store.setMonitorChanges(true);

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
        this.grid.setId("state-connecter-grid");
        this.grid.setStateful(true);
        this.grid.setLoadMask(true);
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

        this.waitDialog.close();

        MessageBox.confirm(null, "Failed to get service name, retry?",
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

        this.waitDialog.close();

        this.store.removeAll();
        refreshLoader();

        this.submitButton.disable();
        setBusy(false);
        this.window.show();
        this.window.center();
    }

    private void onShow(AppEvent event) {
        this.service = event.getData();

        requestServiceName();
    }

    private void refreshLoader() {
        this.loader.load();
    }

    private void requestServiceName() {
        this.waitDialog = MessageBox.wait(null, "Please wait.", "Getting service details...");

        AppEvent request = new AppEvent(StateConnectEvents.ServiceNameRequest);
        request.setData("service", this.service);
        fireEvent(request);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.submitButton.setIcon(SenseIconProvider.ICON_LOADING);
            this.cancelButton.disable();
        } else {
            this.submitButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
            this.cancelButton.enable();
        }
    }

    private void submitForm() {
        TreeModel sensor = this.grid.getSelectionModel().getSelectedItem();
        AppEvent event = new AppEvent(StateConnectEvents.ConnectRequested);
        event.setData("service", service);
        event.setData("serviceName", serviceName);
        event.setData("sensor", sensor);
        Dispatcher.forwardEvent(event);

        setBusy(true);
    }
}
