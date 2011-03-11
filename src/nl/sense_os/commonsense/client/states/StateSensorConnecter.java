package nl.sense_os.commonsense.client.states;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.grid.CenteredWindow;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.utility.SensorKeyProvider;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
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
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateSensorConnecter extends View {

    private static final String TAG = "StateSensorConnecter";
    private Window window;
    private FormPanel form;
    private Button submitButton;
    private Button cancelButton;
    private TreeStore<TreeModel> store;
    private TreePanel<TreeModel> tree;
    private BaseTreeLoader<TreeModel> loader;
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
        this.submitButton = new Button("Connect", IconHelper.create(Constants.ICON_BUTTON_GO), l);
        this.cancelButton = new Button("Cancel", l);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.submitButton);
        this.form.addButton(this.cancelButton);

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
        panel.setHeaderVisible(false);
        panel.setStyleAttribute("backgroundColor", "white");
        panel.add(this.tree);

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
        this.window.setSize(404, 250);
        this.window.setResizable(false);
        this.window.setPlain(true);
        this.window.setMonitorWindowResize(true);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Connect sensor(s) to state");

        initForm();
    }

    private void initTree() {

        // trees store
        RpcProxy<List<TreeModel>> proxy = new RpcProxy<List<TreeModel>>() {

            @Override
            public void load(Object loadConfig, AsyncCallback<List<TreeModel>> callback) {
                if (null == loadConfig) {
                    AppEvent event = new AppEvent(StateEvents.AvailableSensorsRequested);
                    event.setData("service", service);
                    event.setData("callback", callback);
                    Dispatcher.forwardEvent(event);
                } else if (loadConfig instanceof TreeModel) {
                    List<ModelData> childrenModels = ((TreeModel) loadConfig).getChildren();
                    List<TreeModel> children = new ArrayList<TreeModel>();
                    for (ModelData model : childrenModels) {
                        children.add((TreeModel) model);
                    }
                    callback.onSuccess(children);
                } else {
                    callback.onSuccess(new ArrayList<TreeModel>());
                }
            }
        };
        this.loader = new BaseTreeLoader<TreeModel>(proxy);
        this.store = new TreeStore<TreeModel>(loader);
        this.store.setKeyProvider(new SensorKeyProvider());

        // sort tree
        this.store.setStoreSorter(new StoreSorter<TreeModel>(new SensorComparator()));

        this.tree = new TreePanel<TreeModel>(store);
        this.tree.setBorders(false);
        this.tree.setDisplayProperty("text");
        this.tree.setIconProvider(new SensorIconProvider());
    }

    private void refreshLoader() {
        this.loader.load();
    }

    private void onShow(AppEvent event) {
        this.service = event.getData();
        this.store.removeAll();
        refreshLoader();

        requestServiceName();

        this.submitButton.disable();
        setBusy(false);
        this.window.show();
        this.window.center();
    }

    private void requestServiceName() {
        AppEvent request = new AppEvent(StateEvents.ServiceNameRequest);
        request.setData("service", service);
        fireEvent(request);

    }
    private void setBusy(boolean busy) {
        if (busy) {
            this.submitButton.setIcon(IconHelper.create(Constants.ICON_LOADING));
            this.cancelButton.disable();
        } else {
            this.submitButton.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
            this.cancelButton.enable();
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
