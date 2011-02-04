package nl.sense_os.commonsense.client.views;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldSetEvent;
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
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class StateEditor extends View {

    private static final String TAG = "StateEditor";
    private TreeModel service;
    private ComboBox<ModelData> methodField;
    private FieldSet paramFields;
    private LabelField returnField;
    private ListStore<ModelData> store;
    private Window window;
    private FormPanel form;
    private Button submitButton;
    private Button cancelButton;

    public StateEditor(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.ShowEditor)) {
            Log.d(TAG, "Show");
            onShow(event);
        } else if (type.equals(StateEvents.InvokeMethodComplete)) {
            Log.d(TAG, "InvokeMethodComplete");
            onInvokeComplete(event);
        } else if (type.equals(StateEvents.InvokeMethodFailed)) {
            Log.w(TAG, "InvokeMethodFailed");
            onInvokeFailed(event);
        } else if (type.equals(StateEvents.MethodsUpdated)) {
            Log.d(TAG, "MethodsUpdated");
            onMethodsUpdated(event);
        } else if (type.equals(StateEvents.MethodsNotUpdated)) {
            Log.w(TAG, "MethodsNotUpdated");
            onMethodsNotUpdated(event);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    protected void hideWindow() {
        this.window.hide();
    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button pressed = ce.getButton();
                if (pressed.equals(submitButton)) {
                    if (form.isValid()) {
                        onSubmit();
                    }
                } else if (pressed.equals(cancelButton)) {
                    hideWindow();
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };
        this.submitButton = new Button("Submit", l);

        this.cancelButton = new Button("Cancel", l);

        final FormButtonBinding binding = new FormButtonBinding(this.form);
        binding.addButton(this.submitButton);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.submitButton);
        this.form.addButton(this.cancelButton);

        setBusy(false);
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
    private void initFields() {
        this.store = new ListStore<ModelData>();

        methodField = new ComboBox<ModelData>();
        methodField.setFieldLabel("Method");
        methodField.setDisplayField("name");
        methodField.setEmptyText("Select service method...");
        methodField.setStore(this.store);
        methodField.setTypeAhead(true);
        methodField.setTriggerAction(TriggerAction.ALL);

        methodField.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ModelData> se) {
                ModelData method = se.getSelectedItem();
                updateParametersField(method);
                updateReturnField(method);
                form.layout();
            }
        });

        Listener<FieldSetEvent> l = new Listener<FieldSetEvent>() {
            public void handleEvent(FieldSetEvent be) {
                form.layout(true);
            }
        };
        paramFields = new FieldSet();
        paramFields.addListener(Events.Collapse, l);
        paramFields.addListener(Events.Expand, l);
        paramFields.setLayout(new FormLayout());
        paramFields.setHeading("Parameters");
        LabelField temp = new LabelField("no parameters");
        paramFields.add(temp);
        paramFields.disable();

        returnField = new LabelField();
        returnField.setFieldLabel("Result:");
        returnField.disable();

        final FormData formData = new FormData("-10");
        form.add(methodField, formData);
        form.add(paramFields, formData);
        form.add(returnField, formData);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new Window();
        this.window.setSize(400, 247);
        this.window.setResizable(true);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Set/get service parameters");

        initForm();
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

    private void onInvokeComplete(AppEvent event) {
        setBusy(false);
        returnField.setValue(event.<String> getData());
        returnField.enable();
    }

    private void onInvokeFailed(AppEvent event) {
        setBusy(false);
        MessageBox.confirm(null, "Method failed, retry?", new Listener<MessageBoxEvent>() {

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

    private void onMethodsNotUpdated(AppEvent event) {
        this.store.removeAll();
    }

    private void onMethodsUpdated(AppEvent event) {
        List<ModelData> methods = event.<List<ModelData>> getData();
        this.store.removeAll();
        this.store.add(methods);
    }

    private void onShow(AppEvent event) {
        service = event.<TreeModel> getData();
        event.setType(StateEvents.MethodsRequested);
        Dispatcher.forwardEvent(event);

        this.store.removeAll();

        this.window.show();
    }

    @SuppressWarnings("unchecked")
    protected void onSubmit() {
        setBusy(true);

        ModelData method = methodField.getValue();
        List<String> params = new ArrayList<String>();
        for (Component c : paramFields.getItems()) {
            if (c instanceof TextField<?>) {
                params.add(((TextField<String>) c).getValue());
            }
        }
        AppEvent event = new AppEvent(StateEvents.InvokeMethodRequested);
        event.setData("service", service);
        event.setData("method", method);
        event.setData("parameters", params);
        Dispatcher.forwardEvent(event);
    }

    protected void updateParametersField(ModelData method) {
        List<String> params = method.<List<String>> get("parameters");

        paramFields.removeAll();
        if (params.size() > 0) {
            for (String param : params) {
                TextField<String> field = new TextField<String>();
                field.setFieldLabel(param);
                field.setName(param);
                field.setAllowBlank(false);
                paramFields.add(field, new FormData("-10"));
            }

            paramFields.enable();
        } else {
            LabelField temp = new LabelField("no parameters");
            paramFields.add(temp);

            paramFields.disable();
        }
        paramFields.layout();
    }
    protected void updateReturnField(ModelData method) {
        String returns = method.<String> get("return");

        if (returns.length() > 0) {
            returnField.setFieldLabel("Result (" + returns + "):");
        } else {
            returnField.setFieldLabel("Result:");
        }
        returnField.disable();
    }
}
