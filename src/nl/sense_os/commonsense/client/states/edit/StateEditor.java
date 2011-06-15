package nl.sense_os.commonsense.client.states.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.ServiceMethodModel;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
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

    private static final Logger LOG = Logger.getLogger(StateEditor.class.getName());
    private SensorModel stateSensor;
    private ComboBox<ServiceMethodModel> methodField;
    private FieldSet paramFields;
    private LabelField returnField;
    private ListStore<ServiceMethodModel> methodStore;
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
        if (type.equals(StateEditEvents.ShowEditor)) {
            LOG.finest("Show");
            onShow(event);

        } else if (type.equals(StateEditEvents.InvokeMethodComplete)) {
            LOG.finest("InvokeMethodComplete");
            onInvokeComplete(event);

        } else if (type.equals(StateEditEvents.InvokeMethodFailed)) {
            LOG.warning("InvokeMethodFailed");
            onInvokeFailed(event);

        } else {
            LOG.warning("Unexpected event type: " + type);
        }
    }

    private void hideWindow() {
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
                    LOG.warning("Unexpected button pressed");
                }
            }
        };
        this.submitButton = new Button("Submit", SenseIconProvider.ICON_BUTTON_GO, l);

        this.cancelButton = new Button("Cancel", l);

        final FormButtonBinding binding = new FormButtonBinding(this.form);
        binding.addButton(this.submitButton);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.submitButton);
        this.form.addButton(this.cancelButton);
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
        this.methodStore = new ListStore<ServiceMethodModel>();

        methodField = new ComboBox<ServiceMethodModel>();
        methodField.setFieldLabel("Method");
        methodField.setDisplayField(ServiceMethodModel.NAME);
        methodField.setEmptyText("Select state service method...");
        methodField.setStore(this.methodStore);
        methodField.setTypeAhead(true);
        methodField.setTriggerAction(TriggerAction.ALL);

        methodField.addSelectionChangedListener(new SelectionChangedListener<ServiceMethodModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ServiceMethodModel> se) {
                ServiceMethodModel method = se.getSelectedItem();
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

        this.window = new CenteredWindow();
        this.window.setHeading("Set or get algorithm parameters");
        this.window.setSize(400, 247);
        this.window.setLayout(new FitLayout());

        initForm();
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

    private void onShow(AppEvent event) {
        stateSensor = event.<SensorModel> getData();
        List<ServiceMethodModel> methods = stateSensor.get("methods");

        if (null != methods) {

            this.methodStore.removeAll();
            this.methodStore.add(methods);

            this.window.show();
            this.window.center();
        } else {
            MessageBox.alert(null, "This state algorithm cannot be edited!", null);
        }
    }

    @SuppressWarnings("unchecked")
    private void onSubmit() {
        setBusy(true);

        ServiceMethodModel method = methodField.getValue();
        List<String> params = new ArrayList<String>();
        for (Component c : paramFields.getItems()) {
            if (c instanceof TextField<?>) {
                params.add(((TextField<String>) c).getValue());
            }
        }
        AppEvent event = new AppEvent(StateEditEvents.InvokeMethodRequested);
        event.setData("stateSensor", stateSensor);
        event.setData("method", method);
        event.setData("parameters", params);
        Dispatcher.forwardEvent(event);
    }

    private void updateParametersField(ServiceMethodModel method) {
        List<String> params = method.getParameters();

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

    private void updateReturnField(ServiceMethodModel method) {
        String returns = method.getReturnValue();

        if (returns.length() > 0) {
            returnField.setFieldLabel("Result (" + returns + "):");
        } else {
            returnField.setFieldLabel("Result:");
        }
        returnField.disable();
    }
}
