package nl.sense_os.commonsense.client.views;

import java.util.List;

import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class StateSensorConnecter extends View {

    private static final String TAG = "StateSensorConnecter";
    private Window window;
    private FormPanel form;
    private Button submitButton;
    private Button cancelButton;
    private TreeStore<TreeModel> store;
    private TreePanel<TreeModel> tree;
    private TreeModel service;

    public StateSensorConnecter(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.ShowSensorConnecter)) {
            Log.d(TAG, "Show");
            onShow(event);
        } else if (type.equals(StateEvents.AvailableSensorsUpdated)) {
            Log.d(TAG, "AvailableSensorsUpdated");
            onAvailableSensorsUpdated(event);
        } else if (type.equals(StateEvents.AvailableSensorsNotUpdated)) {
            Log.w(TAG, "AvailableSensorsNotUpdated");
            onAvailableSensorsNotUpdated(event);
        } else if (type.equals(StateEvents.ConnectComplete)) {
            Log.d(TAG, "ConnectComplete");
            hideWindow();
        } else if (type.equals(StateEvents.ConnectFailed)) {
            Log.w(TAG, "ConnectFailed");
            onConnectFailed();
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void onConnectFailed() {
        MessageBox.alert(null, "Connect failed, please retry", null);
    }

    protected void hideWindow() {
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
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };
        this.submitButton = new Button("Create", l);
        this.cancelButton = new Button("Cancel", l);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.submitButton);
        this.form.addButton(this.cancelButton);

        // set the submit button icon
        setBusy(false);

        // handle selections
        TreePanelSelectionModel<TreeModel> selectionModel = new TreePanelSelectionModel<TreeModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                TreeModel selection = se.getSelectedItem();
                if (null != selection) {
                    int tagType = selection.<Integer> get("tagType");
                    if (tagType == TagModel.TYPE_SENSOR) {
                        submitButton.enable();
                    } else {
                        submitButton.disable();
                    }
                } else {
                    submitButton.disable();
                }
            }
        });
        this.tree.setSelectionModel(selectionModel);
    }

    private void initFields() {

        initTree();

        ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setWidth(this.form.getFieldWidth());
        panel.setHeight(300);
        panel.setHeaderVisible(false);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.add(this.tree);

        AdapterField field = new AdapterField(panel);
        field.setFieldLabel("Select the sensor to connect to the service");
        this.form.add(field);
    }

    private void initForm() {
        this.form = new FormPanel();
        this.form.setHeaderVisible(false);
        this.form.setLabelAlign(LabelAlign.TOP);
        this.form.setFieldWidth(275);

        initFields();
        initButtons();

        this.window.add(this.form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new Window();
        this.window.setSize(350, 350);
        this.window.setResizable(false);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Connect sensor(s) to state");

        initForm();
    }

    private void initTree() {

        // trees store
        this.store = new TreeStore<TreeModel>();
        this.store.setKeyProvider(new ModelKeyProvider<TreeModel>() {

            @Override
            public String getKey(TreeModel model) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_GROUP) {
                    return "group " + model.<String> get("text");
                } else if (tagType == TagModel.TYPE_DEVICE) {
                    return "device " + model.<String> get("uuid")
                            + model.getParent().<String> get("text");
                } else if (tagType == TagModel.TYPE_SENSOR) {
                    return "sensor " + model.<String> get("id")
                            + model.getParent().<String> get("uuid");
                } else if (tagType == TagModel.TYPE_SERVICE) {
                    return "service " + model.<String> get("service_name")
                            + model.<String> get("data_fields");
                } else {
                    Log.e(TAG, "unexpected tag type in ModelKeyProvider");
                    return model.toString();
                }
            }
        });

        // sort tree
        this.store.setStoreSorter(new StoreSorter<TreeModel>(new SensorComparator()));

        this.tree = new TreePanel<TreeModel>(store);
        this.tree.setBorders(false);
        this.tree.setDisplayProperty("text");
        this.tree.setIconProvider(new ModelIconProvider<TreeModel>() {

            @Override
            public AbstractImagePrototype getIcon(TreeModel model) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_GROUP) {
                    return IconHelper.create("gxt/images/gxt/icons/folder.gif");
                } else if (tagType == TagModel.TYPE_DEVICE) {
                    return IconHelper.create("gxt/images/gxt/icons/folder.gif");
                } else if (tagType == TagModel.TYPE_SENSOR) {
                    return IconHelper.create("gxt/images/gxt/icons/tabs.gif");
                } else {
                    Log.e(TAG, "unexpected tag type in ModelIconProvider");
                    return IconHelper.create("gxt/images/gxt/icons/done.gif");
                }
            }
        });
    }

    private void onAvailableSensorsUpdated(AppEvent event) {
        this.store.removeAll();
        this.store.add(event.<List<TreeModel>> getData(), true);

        if (this.store.getChildCount() > 0) {
            this.tree.getSelectionModel().select(0, false);
        }
    }

    private void onAvailableSensorsNotUpdated(AppEvent event) {
        this.store.removeAll();
    }

    private void onShow(AppEvent event) {
        this.service = event.getData();

        // update the list of sensors
        Dispatcher.forwardEvent(StateEvents.AvailableSensorsRequested, this.service);

        this.submitButton.disable();
        setBusy(false);
        this.window.show();
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.submitButton.setIcon(IconHelper.create("gxt/images/gxt/icons/loading.gif"));
            this.cancelButton.setEnabled(false);
        } else {
            this.submitButton.setIcon(IconHelper.create("gxt/images/gxt/icons/page-next.gif"));
            this.cancelButton.setEnabled(true);
        }
    }

    protected void submitForm() {
        TreeModel sensor = this.tree.getSelectionModel().getSelectedItem();
        AppEvent event = new AppEvent(StateEvents.ConnectRequested);
        event.setData("service", service);
        event.setData("sensor", sensor);
        Dispatcher.forwardEvent(event);

        setBusy(true);
    }
}
