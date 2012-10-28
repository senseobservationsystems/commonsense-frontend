package nl.sense_os.commonsense.main.client.states.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtServiceMethod;

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
import com.google.gwt.core.client.JsonUtils;

public class StateEditor extends View {

    private static final Logger LOG = Logger.getLogger(StateEditor.class.getName());
    private GxtSensor stateSensor;
    private ComboBox<GxtServiceMethod> methodField;
    private FieldSet paramFields;
    private LabelField returnField;
    private ListStore<GxtServiceMethod> methodStore;
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
	window.hide();
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
	submitButton = new Button("Submit", l);
	submitButton.setIconStyle("sense-btn-icon-go");

	cancelButton = new Button("Cancel", l);

	final FormButtonBinding binding = new FormButtonBinding(form);
	binding.addButton(submitButton);

	form.addButton(submitButton);
	form.addButton(cancelButton);
    }

    private void initFields() {
	methodStore = new ListStore<GxtServiceMethod>();

	methodField = new ComboBox<GxtServiceMethod>();
	methodField.setFieldLabel("Method");
	methodField.setDisplayField(GxtServiceMethod.NAME);
	methodField.setEmptyText("Select state service method...");
	methodField.setStore(methodStore);
	methodField.setTypeAhead(true);
	methodField.setTriggerAction(TriggerAction.ALL);

	methodField.addSelectionChangedListener(new SelectionChangedListener<GxtServiceMethod>() {

	    @Override
	    public void selectionChanged(SelectionChangedEvent<GxtServiceMethod> se) {
		GxtServiceMethod method = se.getSelectedItem();
		updateParametersField(method);
		updateReturnField(method);
		form.layout();
	    }
	});

	Listener<FieldSetEvent> l = new Listener<FieldSetEvent>() {
	    @Override
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

    private void initForm() {

	form = new FormPanel();
	form.setHeaderVisible(false);
	form.setBodyBorder(false);
	form.setScrollMode(Scroll.AUTOY);

	initFields();
	initButtons();

	window.add(form);
    }

    @Override
    protected void initialize() {
	super.initialize();

	window = new CenteredWindow();
	window.setHeading("Set or get algorithm parameters");
	window.setSize(640, 360);
	window.setLayout(new FitLayout());

	initForm();
    }

    private String makePrettyJson(String response) {
	response = response.replace("&quot;", "\"");
	String pretty = "";
	if (JsonUtils.safeToEval(response)) {
	    int indentAmount = 0;
	    String indentString = "  ";
	    for (int i = 0; i < response.length(); i++) {

		// check for new lines
		if (response.charAt(i) == '{') {
		    pretty += "{\n";
		    indentAmount++;
		    // indent
		    for (int j = 0; j < indentAmount; j++) {
			pretty += indentString;
		    }
		} else if (response.charAt(i) == '[') {
		    pretty += "[\n";
		    indentAmount++;
		    // indent
		    for (int j = 0; j < indentAmount; j++) {
			pretty += indentString;
		    }
		} else if (response.charAt(i) == ',') {
		    pretty += ",\n";
		    // indent
		    for (int j = 0; j < indentAmount; j++) {
			pretty += indentString;
		    }
		} else if (response.charAt(i) == '}') {
		    pretty += "\n";
		    indentAmount--;
		    // indent
		    for (int j = 0; j < indentAmount; j++) {
			pretty += indentString;
		    }
		    pretty += "}";
		} else if (response.charAt(i) == ']') {
		    pretty += "\n";
		    indentAmount--;
		    // indent
		    for (int j = 0; j < indentAmount; j++) {
			pretty += indentString;
		    }
		    pretty += "]";
		} else {
		    pretty += response.charAt(i);
		}
	    }
	} else {
	    pretty = response;
	}
	return "<pre>" + pretty + "</pre>";
    }

    private void onInvokeComplete(AppEvent event) {
	setBusy(false);
	String response = event.<String> getData();
	String pretty = makePrettyJson(response);
	returnField.setValue(pretty);
	returnField.enable();
    }

    private void onInvokeFailed(AppEvent event) {
	setBusy(false);
	MessageBox.confirm(null, "Method failed, retry?", new Listener<MessageBoxEvent>() {

	    @Override
	    public void handleEvent(MessageBoxEvent be) {
		if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
		    onSubmit();
		}
	    }
	});
    }

    private void onShow(AppEvent event) {
	stateSensor = event.<GxtSensor> getData();
	List<GxtServiceMethod> methods = stateSensor.get("methods");

	if (null != methods) {
	    methodStore.removeAll();
	    methodStore.add(methods);
	    methodField.clear();

	    window.show();
	    window.center();

	} else {
	    MessageBox.alert(null, "This state algorithm cannot be edited!", null);
	}
    }

    @SuppressWarnings("unchecked")
    private void onSubmit() {
	setBusy(true);

	GxtServiceMethod method = methodField.getValue();
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

    private void setBusy(boolean busy) {
	if (busy) {
	    submitButton.setIconStyle("sense-btn-icon-loading");
	    cancelButton.disable();
	} else {
	    submitButton.setIconStyle("sense-btn-icon-go");
	    cancelButton.enable();
	}
    }

    private void updateParametersField(GxtServiceMethod method) {
	List<String> params = method != null ? method.getParameters() : new ArrayList<String>();

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

    private void updateReturnField(GxtServiceMethod method) {
	String returns = method != null ? method.getReturnValue() : "";

	if (returns.length() > 0) {
	    returnField.setFieldLabel("Result (" + returns + "):");
	} else {
	    returnField.setFieldLabel("Result:");
	}
	returnField.disable();
    }
}
