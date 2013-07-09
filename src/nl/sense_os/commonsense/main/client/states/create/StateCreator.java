package nl.sense_os.commonsense.main.client.states.create;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtService;
import nl.sense_os.commonsense.main.client.gxt.util.LibraryColumnsFactory;
import nl.sense_os.commonsense.main.client.gxt.util.SenseKeyProvider;
import nl.sense_os.commonsense.main.client.gxt.util.SensorGroupRenderer;
import nl.sense_os.commonsense.main.client.gxt.util.SensorOwnerFilter;
import nl.sense_os.commonsense.main.client.gxt.util.SensorProcessor;
import nl.sense_os.commonsense.main.client.gxt.util.SensorTextFilter;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
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
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class StateCreator extends View {

    private static final Logger LOG = Logger.getLogger(StateCreator.class.getName());
    private Window window;
    private FormPanel form;
    private TextField<String> nameField;
    private ComboBox<GxtService> servicesField;
    private ListStore<GxtService> servicesStore;
    private AdapterField sensorsField;
    private GroupingStore<GxtSensor> sensorsStore;
    private Grid<GxtSensor> sensorsGrid;
    private ToolBar sensorsFilterBar;
    private ListStore<ModelData> dataFieldsStore;
    private Grid<ModelData> dataFieldsGrid;
    private AdapterField dataFieldsField;

    private Button createButton;
    private Button cancelButton;

    public StateCreator(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateCreateEvents.ShowCreator)) {
            LOG.fine("NewCreator");
            showWindow();

        } else if (type.equals(StateCreateEvents.CreateServiceCancelled)) {
            // LOG.fine( "CreateCancelled");
            onCancelled(event);

        } else if (type.equals(StateCreateEvents.CreateServiceComplete)) {
            // LOG.fine( "CreateComplete");
            onComplete(event);

        } else if (type.equals(StateCreateEvents.CreateServiceFailed)) {
            LOG.warning("CreateFailed");
            onFailed(event);

        } else if (type.equals(StateCreateEvents.LoadSensorsSuccess)) {
            // LOG.fine( "LoadSensorsSuccess");
            final List<GxtSensor> sensors = event.<List<GxtSensor>> getData("sensors");
            onLoadSensorsComplete(sensors);

        } else if (type.equals(StateCreateEvents.LoadSensorsFailure)) {
            LOG.warning("LoadSensorsFailure");
            onLoadSensorsComplete(null);

        } else if (type.equals(StateCreateEvents.AvailableServicesUpdated)) {
            // LOG.fine( "AvailableServicesUpdated");
            final List<GxtService> services = event.<List<GxtService>> getData("services");
            onAvailableServicesComplete(services);

        } else if (type.equals(StateCreateEvents.AvailableServicesNotUpdated)) {
            LOG.warning("AvailableServicesNotUpdated");
            onAvailableServicesComplete(null);

        } else {
            LOG.warning("Unexpected event type: " + type);
        }
    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button pressed = ce.getButton();
                if (pressed.equals(createButton)) {
                    if (form.isValid()) {
                        submitForm();
                    }
                } else if (pressed.equals(cancelButton)) {
                    StateCreator.this.fireEvent(StateCreateEvents.CreateServiceCancelled);
                } else {
                    LOG.warning("Unexpected button pressed");
                }
            }
        };
        createButton = new Button("Create", l);
        createButton.setIconStyle("sense-btn-icon-go");

        cancelButton = new Button("Cancel", l);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(createButton);

        form.addButton(createButton);
        form.addButton(cancelButton);
    }

    private void initFields() {

        nameField = new TextField<String>();
        nameField.setFieldLabel("State sensor name");
        nameField.setAllowBlank(false);

        initSensorsGrid();
        initSensorsFilter();
        ContentPanel sensorsPanel = new ContentPanel(new FitLayout());
        sensorsPanel.setHeaderVisible(false);
        sensorsPanel.setStyleAttribute("backgroundColor", "white");
        sensorsPanel.setTopComponent(sensorsFilterBar);
        sensorsPanel.add(sensorsGrid);

        sensorsField = new AdapterField(sensorsPanel);
        sensorsField.setHeight(300);
        sensorsField.setResizeWidget(true);
        sensorsField.setFieldLabel("Input sensor");

        sensorsGrid.getSelectionModel().addSelectionChangedListener(
                new SelectionChangedListener<GxtSensor>() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent<GxtSensor> se) {

                        servicesField.clear();
                        TreeModel selected = se.getSelectedItem();
                        if (selected != null) {
                            AppEvent getServices = new AppEvent(
                                    StateCreateEvents.AvailableServicesRequested);
                            getServices.setData("sensor", selected);
                            StateCreator.this.fireEvent(getServices);
                        } else {
                            servicesStore.removeAll();
                        }
                    }
                });

        servicesStore = new ListStore<GxtService>();

        servicesField = new ComboBox<GxtService>();
        servicesField.setFieldLabel("Algorithm type");
        servicesField.setEmptyText("Select service algorithm type...");
        servicesField.setStore(servicesStore);
        servicesField.setDisplayField(GxtService.NAME);
        servicesField.setAllowBlank(false);
        servicesField.setTriggerAction(TriggerAction.ALL);
        servicesField.setTypeAhead(true);
        servicesField.setForceSelection(true);

        // update sensors and data fields when a service is selected
        servicesField.addSelectionChangedListener(new SelectionChangedListener<GxtService>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GxtService> se) {
                GxtService selected = se.getSelectedItem();
                GxtSensor sensor = sensorsGrid.getSelectionModel().getSelectedItem();
                dataFieldsStore.removeAll();

                if (null != selected) {
                    String sensorName = sensor.<String> get(GxtSensor.NAME);
                    sensorName = sensorName.replace(' ', '_');

                    List<String> dataFields = selected.<List<String>> get(GxtService.DATA_FIELDS);
                    for (String fieldName : dataFields) {
                        if (fieldName.contains(sensorName)
                                && fieldName.length() > sensorName.length()) {
                            int beginIndex = sensorName.length() + 1;
                            fieldName = fieldName.substring(beginIndex, fieldName.length());
                        }
                        ModelData fieldModel = new BaseModelData();
                        fieldModel.set("text", fieldName);
                        dataFieldsStore.add(fieldModel);
                    }
                }
            }
        });

        dataFieldsStore = new ListStore<ModelData>();

        ColumnModel cm = new ColumnModel(Arrays.asList(new ColumnConfig("text", "", form
                .getFieldWidth())));
        dataFieldsGrid = new Grid<ModelData>(dataFieldsStore, cm);
        dataFieldsGrid.setHideHeaders(true);
        dataFieldsGrid.setAutoExpandColumn("text");

        dataFieldsField = new AdapterField(dataFieldsGrid);
        dataFieldsField.setFieldLabel("Data fields");
        dataFieldsField.setResizeWidget(true);
        dataFieldsField.setBorders(true);

        final FormData formData = new FormData("-10");
        form.add(nameField, formData);
        form.add(sensorsField, formData);
        form.add(servicesField, formData);
        form.add(dataFieldsField, formData);
    }

    private void initForm() {

        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        form.setScrollMode(Scroll.AUTOY);
        form.setLabelWidth(150);

        initFields();
        initButtons();

        window.add(form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        window = new CenteredWindow();
        window.setHeadingText("Create state sensor");
        window.setSize(720, 550);
        window.setLayout(new FitLayout());

        initForm();
    }

    private void initSensorsFilter() {

        // text filter
        SensorTextFilter<GxtSensor> textFilter = new SensorTextFilter<GxtSensor>();
        textFilter.bind(sensorsStore);

        // filter to show only my own sensors
        final SensorOwnerFilter<GxtSensor> ownerFilter = new SensorOwnerFilter<GxtSensor>();
        sensorsStore.addFilter(ownerFilter);

        // checkbox to toggle filter
        final CheckBox filterOnlyMe = new CheckBox();
        filterOnlyMe.setBoxLabel("Only my own sensors");
        filterOnlyMe.setHideLabel(true);
        filterOnlyMe.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {

                ownerFilter.setEnabled(filterOnlyMe.getValue());
                sensorsStore.applyFilters(null);
            }
        });

        // add filters to filter bar
        sensorsFilterBar = new ToolBar();
        sensorsFilterBar.add(new LabelToolItem("Filter: "));
        sensorsFilterBar.add(textFilter);
        sensorsFilterBar.add(new SeparatorToolItem());
        sensorsFilterBar.add(filterOnlyMe);
    }

    private void initSensorsGrid() {

        sensorsStore = new GroupingStore<GxtSensor>();
        sensorsStore.setKeyProvider(new SenseKeyProvider<GxtSensor>());

        // Column model
        ColumnModel cm = LibraryColumnsFactory.create();

        // grouping view
        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(true);
        view.setForceFit(true);
        view.setGroupRenderer(new SensorGroupRenderer(cm));

        sensorsGrid = new Grid<GxtSensor>(sensorsStore, cm);
        sensorsGrid.setModelProcessor(new SensorProcessor<GxtSensor>());
        sensorsGrid.setView(view);
        sensorsGrid.setBorders(false);
    }

    private void onAvailableServicesComplete(List<GxtService> services) {
        servicesStore.removeAll();
        dataFieldsStore.removeAll();

        if (services != null) {
            servicesStore.add(services);
        } else {
            window.hide();
            MessageBox.alert(null, "Error getting list of available services!", null);
        }
    }

    private void onCancelled(AppEvent event) {
        window.hide();
        setBusy(false);
    }

    private void onComplete(AppEvent event) {
        window.hide();
        setBusy(false);
    }

    private void onFailed(AppEvent event) {
        setBusy(false);
        MessageBox.confirm(null, "Failed to create state sensor, retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getHtml().equalsIgnoreCase("yes")) {
                            submitForm();
                        } else {
                            window.hide();
                        }
                    }
                });
    }

    private void onLoadSensorsComplete(List<GxtSensor> sensors) {
        sensorsStore.removeAll();
        servicesStore.removeAll();
        dataFieldsStore.removeAll();

        if (sensors != null) {
            sensorsStore.add(sensors);
            sensorsStore.clearFilters();
            sensorsStore.applyFilters(null);
        } else {
            window.hide();
            MessageBox.alert(null, "Error getting list of source sensors!", null);
        }
    }

    private void setBusy(boolean busy) {
        if (busy) {
            createButton.setIconStyle("sense-btn-icon-loading");
            cancelButton.disable();
        } else {
            createButton.setIconStyle("sense-btn-icon-go");
            cancelButton.enable();
        }
    }

    private void showWindow() {
        form.reset();
        window.show();
        window.center();

        fireEvent(StateCreateEvents.LoadSensors);
    }

    private void submitForm() {
        setBusy(true);

        AppEvent event = new AppEvent(StateCreateEvents.CreateServiceRequested);
        event.setData("name", nameField.getValue());
        event.setData("service", servicesField.getValue());
        event.setData("sensor", sensorsGrid.getSelectionModel().getSelectedItem());
        event.setData("dataFields", dataFieldsGrid.getSelectionModel().getSelectedItems());
        fireEvent(event);
    }
}
