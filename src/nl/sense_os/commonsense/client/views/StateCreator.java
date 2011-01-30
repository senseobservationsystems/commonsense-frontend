package nl.sense_os.commonsense.client.views;

import java.util.List;

import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.services.TagsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateCreator extends View {

    private static final String TAG = "StateCreator";
    private Window window;
    private FormPanel form;
    private ListStore<TreeModel> servicesStore;
    private ComboBox<TreeModel> servicesField;
    private ListStore<ModelData> sensorsStore;
    private ComboBox<ModelData> sensorsField;
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
        } else if (type.equals(StateEvents.CreateCancelled)) {
            Log.d(TAG, "CreateCancelled");
            onCancelled(event);
        } else if (type.equals(StateEvents.CreateComplete)) {
            Log.d(TAG, "CreateComplete");
            onComplete(event);
        } else if (type.equals(StateEvents.CreateFailed)) {
            Log.w(TAG, "CreateFailed");
            onFailed(event);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void initForm() {

        this.form = new FormPanel();
        this.form.setHeaderVisible(false);

        initFields();
        initButtons();

        this.window.add(form);
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
                    StateCreator.this.fireEvent(StateEvents.CreateCancelled);
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
        final TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
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
        this.servicesField.setFieldLabel("Select service");
        this.servicesField.setStore(this.servicesStore);
        this.servicesField.setDisplayField("text");
        this.servicesField.setAllowBlank(false);
        this.servicesField.setUseQueryCache(true);

        this.sensorsStore = new ListStore<ModelData>();
        this.servicesField.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                TreeModel selection = se.getSelectedItem();
                if (null != selection) {
                    sensorsStore.removeAll();
                    List<ModelData> children = selection.getChildren();
                    if (children.size() > 0) {
                        sensorsStore.add(selection.getChildren());
                        sensorsField.select(0);
                    }
                } else {
                    sensorsStore.removeAll();
                }
            }
        });

        this.sensorsField = new ComboBox<ModelData>();
        this.sensorsField.setFieldLabel("Select sensor");
        this.sensorsField.setStore(this.sensorsStore);
        this.sensorsField.setDisplayField("text");
        this.sensorsField.setAllowBlank(false);

        this.form.add(this.servicesField);
        this.form.add(this.sensorsField);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new Window();
        this.window.setSize(350, 200);
        this.window.setResizable(false);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Create state sensor");

        initForm();
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
        MessageBox.alert(null, "Failed to create group.", null);
        setBusy(false);
    }

    private void onShow(AppEvent event) {
        this.form.reset();
        this.window.show();
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new AppEvent(StateEvents.CreateRequested);
        event.setData("service", this.servicesField.getValue());
        event.setData("sensor", this.sensorsField.getValue());
        fireEvent(event);
    }
}
