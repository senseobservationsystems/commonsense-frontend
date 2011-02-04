package nl.sense_os.commonsense.client.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.services.SensorsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.utility.SensorKeyProvider;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
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
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateCreator extends View {

    private static final String TAG = "StateCreator";
    private Window window;
    private FormPanel form;
    private TextField<String> nameField;
    private ComboBox<TreeModel> servicesField;
    private ListStore<TreeModel> servicesStore;
    private AdapterField sensorsField;
    private TreeModel selectedService;
    private BaseTreeLoader<TreeModel> sensorsLoader;
    private TreeStore<TreeModel> sensorsStore;
    private TreePanel<TreeModel> sensorsTree;
    private ListStore<ModelData> dataFieldsStore;
    private Grid<ModelData> dataFieldsGrid;
    private AdapterField dataFieldsField;
    private List<String> dataFields;
    private Button createButton;
    private Button cancelButton;

    public StateCreator(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.ShowCreator)) {
            onShow(event);
        } else if (type.equals(StateEvents.CreateServiceCancelled)) {
            Log.d(TAG, "CreateCancelled");
            onCancelled(event);
        } else if (type.equals(StateEvents.CreateServiceComplete)) {
            Log.d(TAG, "CreateComplete");
            onComplete(event);
        } else if (type.equals(StateEvents.CreateServiceFailed)) {
            Log.w(TAG, "CreateFailed");
            onFailed(event);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button pressed = ce.getButton();
                if (pressed.equals(createButton)) {
                    if (form.isValid()) {
                        onSubmit();
                    }
                } else if (pressed.equals(cancelButton)) {
                    StateCreator.this.fireEvent(StateEvents.CreateServiceCancelled);
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };
        this.createButton = new Button("Create", l);

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

        final SensorsServiceAsync service = Registry
                .<SensorsServiceAsync> get(Constants.REG_TAGS_SVC);
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);

        RpcProxy<List<TreeModel>> servicesProxy = new RpcProxy<List<TreeModel>>() {

            @Override
            protected void load(Object loadConfig, AsyncCallback<List<TreeModel>> callback) {
                service.getAvailableServices(sessionId, callback);
            }
        };
        @SuppressWarnings("rawtypes")
        ListLoader servicesLoader = new BaseListLoader(servicesProxy);
        this.servicesStore = new ListStore<TreeModel>(servicesLoader);

        this.servicesField = new ComboBox<TreeModel>();
        this.servicesField.setFieldLabel("Service type");
        this.servicesField.setEmptyText("Select service algorithm type...");
        this.servicesField.setStore(this.servicesStore);
        this.servicesField.setDisplayField("text");
        this.servicesField.setAllowBlank(false);
        this.servicesField.setTriggerAction(TriggerAction.ALL);
        this.servicesField.setTypeAhead(true);

        // update sensors and data fields when a service is selected
        this.servicesField.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                TreeModel newService = se.getSelectedItem();

                if (null != newService) {
                    if (null != selectedService
                            && newService.<String> get("service_name").equals(
                                    selectedService.<String> get("service_name"))) {
                        return;
                    }
                    selectedService = newService;
                    sensorsLoader.load();

                    sensorsField.enable();

                    dataFields = newService.<List<String>> get("data_fields");
                } else {
                    selectedService = newService;
                    sensorsStore.removeAll();
                    sensorsField.disable();
                }

            }
        });

        initSensorsTree();
        ContentPanel sensorsPanel = new ContentPanel(new FitLayout());
        sensorsPanel.setHeaderVisible(false);
        sensorsPanel.setStyleAttribute("backgroundColor", "white");
        sensorsPanel.add(this.sensorsTree);

        this.sensorsField = new AdapterField(sensorsPanel);
        this.sensorsField.setHeight(150);
        this.sensorsField.setResizeWidget(true);
        this.sensorsField.setFieldLabel("Input sensor");

        this.dataFieldsStore = new ListStore<ModelData>();
        this.dataFields = new ArrayList<String>();
        this.sensorsTree.getSelectionModel().addSelectionChangedListener(
                new SelectionChangedListener<TreeModel>() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                        TreeModel sensor = se.getSelectedItem();
                        dataFieldsStore.removeAll();
                        dataFieldsField.reset();

                        if (null != sensor) {
                            dataFieldsField.enable();

                            // populate the store
                            int tagType = sensor.get("tagType");
                            if (tagType == TagModel.TYPE_SENSOR) {
                                String name = sensor.<String> get("name").replaceAll("\\s", "_");
                                for (String fieldName : dataFields) {
                                    if (fieldName.startsWith(name)) {

                                        // create ModelData representing this sensor's datafield
                                        ModelData dataField = new BaseModelData();
                                        if (fieldName.length() == name.length()) {
                                            dataField.set("text", fieldName);
                                        } else {
                                            dataField.set("text", fieldName.substring(
                                                    name.length() + 1, fieldName.length()));
                                        }

                                        // check for double entries
                                        boolean doubleEntry = false;
                                        for (ModelData model : dataFieldsStore.getModels()) {
                                            if (model.get("text").equals(dataField.get("text"))) {
                                                doubleEntry = true;
                                                break;
                                            }
                                        }

                                        if (false == doubleEntry) {
                                            dataFieldsStore.add(dataField);
                                        }
                                    }
                                }
                            } else {
                                dataFieldsStore.removeAll();
                                dataFieldsField.disable();
                            }
                        } else {
                            dataFieldsStore.removeAll();
                            dataFieldsField.disable();
                        }
                    }
                });

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
        this.form.add(this.servicesField, formData);
        this.form.add(this.sensorsField, formData);
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

        this.window = new Window();
        this.window.setSize(400, 400);
        this.window.setResizable(false);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Create state sensor");

        initForm();
    }

    private void initSensorsTree() {

        // trees store
        @SuppressWarnings({"unchecked", "rawtypes"})
        DataProxy proxy = new DataProxy() {

            @Override
            public void load(DataReader reader, Object loadConfig, AsyncCallback callback) {
                if (null == loadConfig && null != selectedService) {
                    AppEvent event = new AppEvent(StateEvents.AvailableSensorsRequested);
                    event.setData("service", selectedService);
                    event.setData("callback", callback);
                    Dispatcher.forwardEvent(event);
                } else if (loadConfig instanceof TreeModel) {
                    List<ModelData> childrenModels = ((TreeModel) loadConfig).getChildren();
                    callback.onSuccess(childrenModels);
                } else {
                    callback.onSuccess(new ArrayList<TreeModel>());
                }
            }
        };
        this.sensorsLoader = new BaseTreeLoader<TreeModel>(proxy);
        this.sensorsStore = new TreeStore<TreeModel>(this.sensorsLoader);
        this.sensorsStore.setKeyProvider(new SensorKeyProvider());

        // sort tree
        this.sensorsStore.setStoreSorter(new StoreSorter<TreeModel>(new SensorComparator()));

        this.sensorsTree = new TreePanel<TreeModel>(sensorsStore);
        this.sensorsTree.setBorders(false);
        this.sensorsTree.setDisplayProperty("text");
        this.sensorsTree.setIconProvider(new SensorIconProvider());
        this.sensorsTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void onCancelled(AppEvent event) {
        this.window.hide();
        setBusy(false);
    }

    private void onComplete(AppEvent event) {
        this.window.hide();
        Dispatcher.forwardEvent(StateEvents.ListRequested);
        setBusy(false);
    }

    private void onFailed(AppEvent event) {
        setBusy(false);
        MessageBox.confirm(null, "Failed to create state sensor, retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            onSubmit();
                        } else {
                            window.hide();
                        }
                    }
                });
    }

    private void onShow(AppEvent event) {
        this.form.reset();
        this.window.show();
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new AppEvent(StateEvents.CreateServiceRequested);
        event.setData("name", this.nameField.getValue());
        event.setData("service", this.servicesField.getValue());
        event.setData("sensor", this.sensorsTree.getSelectionModel().getSelectedItem());
        event.setData("dataFields", this.dataFieldsGrid.getSelectionModel().getSelectedItems());
        fireEvent(event);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.createButton.setIcon(IconHelper.create("gxt/images/gxt/icons/loading.gif"));
            this.cancelButton.setEnabled(false);
        } else {
            this.createButton.setIcon(IconHelper.create("gxt/images/gxt/icons/page-next.gif"));
            this.cancelButton.setEnabled(true);
        }
    }
}
