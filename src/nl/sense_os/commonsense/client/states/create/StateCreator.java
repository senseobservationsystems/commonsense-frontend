package nl.sense_os.commonsense.client.states.create;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.ServiceModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.sensors.library.LibraryColumnsFactory;
import nl.sense_os.commonsense.client.sensors.library.SensorGroupRenderer;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.utility.SenseKeyProvider;
import nl.sense_os.commonsense.client.utility.SensorProcessor;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
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
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
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
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
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

    private static final Logger logger = Logger.getLogger("StateCreator");
    private Window window;
    private FormPanel form;
    private TextField<String> nameField;
    private ComboBox<ServiceModel> servicesField;
    private ListStore<ServiceModel> servicesStore;
    private AdapterField sensorsField;
    private GroupingStore<SensorModel> sensorsStore;
    private Grid<SensorModel> sensorsGrid;
    private ToolBar sensorsFilterBar;
    private StoreFilterField<SensorModel> sensorsFilter;
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
            logger.fine("ShowCreator");
            showWindow();

        } else if (type.equals(StateCreateEvents.CreateServiceCancelled)) {
            // logger.fine( "CreateCancelled");
            onCancelled(event);

        } else if (type.equals(StateCreateEvents.CreateServiceComplete)) {
            // logger.fine( "CreateComplete");
            onComplete(event);

        } else if (type.equals(StateCreateEvents.CreateServiceFailed)) {
            logger.warning("CreateFailed");
            onFailed(event);

        } else if (type.equals(StateCreateEvents.LoadSensorsSuccess)) {
            // logger.fine( "LoadSensorsSuccess");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onLoadSensorsComplete(sensors);

        } else if (type.equals(StateCreateEvents.LoadSensorsFailure)) {
            logger.warning("LoadSensorsFailure");
            onLoadSensorsComplete(null);

        } else if (type.equals(StateCreateEvents.AvailableServicesUpdated)) {
            // logger.fine( "AvailableServicesUpdated");
            final List<ServiceModel> services = event.<List<ServiceModel>> getData("services");
            onAvailableServicesComplete(services);

        } else if (type.equals(StateCreateEvents.AvailableServicesNotUpdated)) {
            logger.warning("AvailableServicesNotUpdated");
            onAvailableServicesComplete(null);

        } else {
            logger.warning("Unexpected event type: " + type);
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
                    logger.warning("Unexpected button pressed");
                }
            }
        };
        createButton = new Button("Create", SenseIconProvider.ICON_BUTTON_GO, l);

        cancelButton = new Button("Cancel", l);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(createButton);

        form.setButtonAlign(HorizontalAlignment.CENTER);
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
                new SelectionChangedListener<SensorModel>() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent<SensorModel> se) {

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

        servicesStore = new ListStore<ServiceModel>();

        servicesField = new ComboBox<ServiceModel>();
        servicesField.setFieldLabel("Algorithm type");
        servicesField.setEmptyText("Select service algorithm type...");
        servicesField.setStore(servicesStore);
        servicesField.setDisplayField(ServiceModel.NAME);
        servicesField.setAllowBlank(false);
        servicesField.setTriggerAction(TriggerAction.ALL);
        servicesField.setTypeAhead(true);
        servicesField.setForceSelection(true);

        // update sensors and data fields when a service is selected
        servicesField.addSelectionChangedListener(new SelectionChangedListener<ServiceModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ServiceModel> se) {
                ServiceModel selected = se.getSelectedItem();
                SensorModel sensor = sensorsGrid.getSelectionModel().getSelectedItem();
                dataFieldsStore.removeAll();

                if (null != selected) {
                    String sensorName = sensor.<String> get(SensorModel.NAME);
                    sensorName = sensorName.replace(' ', '_');

                    List<String> dataFields = selected.<List<String>> get(ServiceModel.DATA_FIELDS);
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
        window.setHeading("Create state sensor");
        window.setSize(720, 550);
        window.setLayout(new FitLayout());

        initForm();
    }

    private void initSensorsFilter() {

        sensorsFilterBar = new ToolBar();
        sensorsFilterBar.add(new LabelToolItem("Filter: "));
        sensorsFilter = new StoreFilterField<SensorModel>() {

            @Override
            protected boolean doSelect(Store<SensorModel> store, SensorModel parent,
                    SensorModel record, String property, String filter) {

                String matchMe = record.getDisplayName().toLowerCase() + " "
                        + record.getPhysicalSensor().toLowerCase() + " "
                        + record.<String> get(SensorModel.DEVICE_TYPE, "").toLowerCase() + " "
                        + record.<String> get(SensorModel.ENVIRONMENT_NAME, "").toLowerCase();
                if (matchMe.contains(filter.toLowerCase())) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        final CheckBox onlyMe = new CheckBox();
        onlyMe.setBoxLabel("Only my own sensors");
        onlyMe.setHideLabel(true);
        onlyMe.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                StoreFilter<SensorModel> filter = new StoreFilter<SensorModel>() {
                    @Override
                    public boolean select(com.extjs.gxt.ui.client.store.Store<SensorModel> store,
                            SensorModel parent, SensorModel item, String property) {
                        UserModel user = Registry.get(Constants.REG_USER);
                        return item.get(SensorModel.OWNER_USERNAME, "").equals(user.getUsername());
                    };
                };

                if (onlyMe.getValue()) {
                    sensorsStore.addFilter(filter);
                    sensorsStore.applyFilters(null);
                } else {
                    sensorsStore.removeFilter(filter);
                    sensorsStore.clearFilters();
                }
            }
        });
        sensorsFilter.bind(sensorsStore);
        sensorsFilterBar.add(sensorsFilter);
        sensorsFilterBar.add(new SeparatorToolItem());
        sensorsFilterBar.add(onlyMe);
    }

    private void initSensorsGrid() {

        sensorsStore = new GroupingStore<SensorModel>();
        sensorsStore.setKeyProvider(new SenseKeyProvider<SensorModel>());

        // Column model
        ColumnModel cm = LibraryColumnsFactory.create();

        // grouping view
        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(true);
        view.setForceFit(true);
        view.setGroupRenderer(new SensorGroupRenderer(cm));

        sensorsGrid = new Grid<SensorModel>(sensorsStore, cm);
        sensorsGrid.setModelProcessor(new SensorProcessor<SensorModel>());
        sensorsGrid.setView(view);
        sensorsGrid.setBorders(false);
    }

    private void onAvailableServicesComplete(List<ServiceModel> services) {
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
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            submitForm();
                        } else {
                            window.hide();
                        }
                    }
                });
    }

    private void onLoadSensorsComplete(List<SensorModel> sensors) {
        sensorsStore.removeAll();
        servicesStore.removeAll();
        dataFieldsStore.removeAll();

        if (sensors != null) {
            sensorsStore.add(sensors);
        } else {
            window.hide();
            MessageBox.alert(null, "Error getting list of source sensors!", null);
        }
    }

    private void setBusy(boolean busy) {
        if (busy) {
            createButton.setIcon(SenseIconProvider.ICON_LOADING);
            cancelButton.disable();
        } else {
            createButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
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
