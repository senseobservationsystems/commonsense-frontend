package nl.sense_os.commonsense.client.states.create;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.sensors.library.LibraryColumnsFactory;
import nl.sense_os.commonsense.client.sensors.library.SensorGroupRenderer;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.utility.SenseKeyProvider;
import nl.sense_os.commonsense.client.utility.SensorProcessor;
import nl.sense_os.commonsense.shared.models.SensorModel;
import nl.sense_os.commonsense.shared.models.ServiceModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
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
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
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
        this.createButton = new Button("Create", SenseIconProvider.ICON_BUTTON_GO, l);

        this.cancelButton = new Button("Cancel", l);

        final FormButtonBinding binding = new FormButtonBinding(this.form);
        binding.addButton(this.createButton);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.createButton);
        this.form.addButton(this.cancelButton);
    }

    private void initFields() {

        this.nameField = new TextField<String>();
        this.nameField.setFieldLabel("State sensor name");
        this.nameField.setAllowBlank(false);

        initSensorsTree();
        ContentPanel sensorsPanel = new ContentPanel(new FitLayout());
        sensorsPanel.setHeaderVisible(false);
        sensorsPanel.setStyleAttribute("backgroundColor", "white");
        sensorsPanel.add(this.sensorsGrid);

        this.sensorsField = new AdapterField(sensorsPanel);
        this.sensorsField.setHeight(300);
        this.sensorsField.setResizeWidget(true);
        this.sensorsField.setFieldLabel("Input sensor");

        this.sensorsGrid.getSelectionModel().addSelectionChangedListener(
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

        this.servicesStore = new ListStore<ServiceModel>();

        this.servicesField = new ComboBox<ServiceModel>();
        this.servicesField.setFieldLabel("Algorithm type");
        this.servicesField.setEmptyText("Select service algorithm type...");
        this.servicesField.setStore(this.servicesStore);
        this.servicesField.setDisplayField(ServiceModel.NAME);
        this.servicesField.setAllowBlank(false);
        this.servicesField.setTriggerAction(TriggerAction.ALL);
        this.servicesField.setTypeAhead(true);
        this.servicesField.setForceSelection(true);

        // update sensors and data fields when a service is selected
        this.servicesField
                .addSelectionChangedListener(new SelectionChangedListener<ServiceModel>() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent<ServiceModel> se) {
                        ServiceModel selected = se.getSelectedItem();
                        SensorModel sensor = sensorsGrid.getSelectionModel().getSelectedItem();
                        dataFieldsStore.removeAll();

                        if (null != selected) {
                            String sensorName = sensor.<String> get(SensorModel.NAME);
                            sensorName = sensorName.replace(' ', '_');

                            List<String> dataFields = selected
                                    .<List<String>> get(ServiceModel.DATA_FIELDS);
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

        this.dataFieldsStore = new ListStore<ModelData>();

        ColumnModel cm = new ColumnModel(Arrays.asList(new ColumnConfig("text", "", this.form
                .getFieldWidth())));
        dataFieldsGrid = new Grid<ModelData>(this.dataFieldsStore, cm);
        dataFieldsGrid.setHideHeaders(true);
        dataFieldsGrid.setAutoExpandColumn("text");

        this.dataFieldsField = new AdapterField(dataFieldsGrid);
        this.dataFieldsField.setFieldLabel("Data fields");
        this.dataFieldsField.setResizeWidget(true);
        this.dataFieldsField.setBorders(true);

        final FormData formData = new FormData("-10");
        this.form.add(this.nameField, formData);
        this.form.add(this.sensorsField, formData);
        this.form.add(this.servicesField, formData);
        this.form.add(this.dataFieldsField, formData);
    }

    private void initForm() {

        this.form = new FormPanel();
        this.form.setHeaderVisible(false);
        this.form.setBodyBorder(false);
        this.form.setScrollMode(Scroll.AUTOY);

        initFields();
        initButtons();

        this.window.add(form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Create state sensor");
        this.window.setSize(450, 550);
        this.window.setLayout(new FitLayout());

        initForm();
    }

    private void initSensorsTree() {

        this.sensorsStore = new GroupingStore<SensorModel>();
        this.sensorsStore.setKeyProvider(new SenseKeyProvider<SensorModel>());

        // Column model
        ColumnModel cm = LibraryColumnsFactory.create();

        // grouping view
        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(true);
        view.setForceFit(true);
        view.setGroupRenderer(new SensorGroupRenderer(cm));

        this.sensorsGrid = new Grid<SensorModel>(this.sensorsStore, cm);
        this.sensorsGrid.setModelProcessor(new SensorProcessor<SensorModel>());
        this.sensorsGrid.setView(view);
        this.sensorsGrid.setBorders(false);
    }

    private void onAvailableServicesComplete(List<ServiceModel> services) {
        this.servicesStore.removeAll();
        this.dataFieldsStore.removeAll();

        if (services != null) {
            this.servicesStore.add(services);
        } else {
            this.window.hide();
            MessageBox.alert(null, "Error getting list of available services!", null);
        }
    }

    private void onCancelled(AppEvent event) {
        this.window.hide();
        setBusy(false);
    }

    private void onComplete(AppEvent event) {
        this.window.hide();
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
        this.sensorsStore.removeAll();
        this.servicesStore.removeAll();
        this.dataFieldsStore.removeAll();

        if (sensors != null) {
            this.sensorsStore.add(sensors);
        } else {
            this.window.hide();
            MessageBox.alert(null, "Error getting list of source sensors!", null);
        }
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.createButton.setIcon(SenseIconProvider.ICON_LOADING);
            this.cancelButton.disable();
        } else {
            this.createButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
            this.cancelButton.enable();
        }
    }

    private void showWindow() {
        this.form.reset();
        this.window.show();
        this.window.center();

        fireEvent(StateCreateEvents.LoadSensors);
    }

    private void submitForm() {
        setBusy(true);

        AppEvent event = new AppEvent(StateCreateEvents.CreateServiceRequested);
        event.setData("name", this.nameField.getValue());
        event.setData("service", this.servicesField.getValue());
        event.setData("sensor", this.sensorsGrid.getSelectionModel().getSelectedItem());
        event.setData("dataFields", this.dataFieldsGrid.getSelectionModel().getSelectedItems());
        fireEvent(event);
    }
}
