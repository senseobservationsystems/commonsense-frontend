package nl.sense_os.commonsense.client.register;

import nl.sense_os.commonsense.client.common.grid.CenteredWindow;
import nl.sense_os.commonsense.client.services.PhoneNumberParser;
import nl.sense_os.commonsense.client.services.PhoneNumberParserAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RegisterForm extends View {

    private class PhoneValidator implements Validator {

        @Override
        public String validate(Field<?> field, String value) {
            if (null == phoneValidatorMessage) {
                return null;
            } else {
                if (phoneValidatorMessage.equals("not mobile")) {
                    phoneValidatorMessage = null;
                    return "Not a valid mobile number";
                } else if (phoneValidatorMessage.equals("not valid")) {
                    phoneValidatorMessage = null;
                    return "Not a valid telephone number";
                }
            }
            return null;
        }
    }

    private String phoneValidatorMessage;
    private static final String TAG = "RegisterForm";
    /**
     * @see http://www.regular-expressions.info/email.html
     */
    private static final String EMAIL_REGEX = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+(?:[a-zA-Z]{2}|com|org|net|edu|gov|mil|biz|info|mobi|name|aero|asia|jobs|museum)";
    private CenteredWindow window;
    private FormPanel form;
    private TextField<String> username;
    private TextField<String> password;
    private TextField<String> name;
    private TextField<String> surname;
    private TextField<String> email;
    private TextField<String> mobile;
    private Button submit;

    public RegisterForm(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(RegisterEvents.Show)) {
            // Log.d(TAG, "Show");
            showWindow();

        } else if (type.equals(RegisterEvents.Hide)) {
            // Log.d(TAG, "Hide");
            hideWindow();

        } else if (type.equals(RegisterEvents.RegisterSuccess)) {
            // Log.d(TAG, "RegisterSuccess");
            hideWindow();

        } else if (type.equals(RegisterEvents.RegisterFailure)) {
            Log.w(TAG, "RegisterFailure");
            onFailure();

        } else {
            Log.e(TAG, "Unexpected event type!");
        }
    }

    private void hideWindow() {
        this.window.hide();
    }

    private void initButtons() {

        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent be) {
                final Button b = be.getButton();
                if (b.equals(submit)) {
                    form.submit();
                } else {
                    Log.w(TAG, "Unexpected button pressed!");
                }
            }
        };

        // submit button
        this.submit = new Button("Submit", IconHelper.create(Constants.ICON_BUTTON_GO), l);
        this.submit.setType("submit");

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(submit);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(submit);

        setupSubmit(form);
    }

    private void initFields() {

        final FormData formData = new FormData("-10");

        // username field
        this.username = new TextField<String>();
        this.username.setFieldLabel("Username:");
        this.username.setAllowBlank(false);

        // password field
        this.password = new TextField<String>();
        this.password.setFieldLabel("Password:");
        this.password.setMinLength(4);
        this.password.setPassword(true);

        // name field
        this.name = new TextField<String>();
        this.name.setFieldLabel("First name:");
        this.name.setAllowBlank(false);

        // surname field
        this.surname = new TextField<String>();
        this.surname.setFieldLabel("Surname:");
        this.surname.setAllowBlank(false);

        // email field
        this.email = new TextField<String>();
        this.email.setFieldLabel("Email:");
        this.email.setRegex(EMAIL_REGEX);
        this.email.getMessages().setRegexText("Invalid email address");

        // mobile field
        this.mobile = new TextField<String>();
        this.mobile.setFieldLabel("Mobile:");
        this.mobile.setAllowBlank(false);
        this.mobile.setValidator(new PhoneValidator());

        this.form.add(this.username, formData);
        this.form.add(this.password, formData);
        this.form.add(this.password, formData);
        this.form.add(this.name, formData);
        this.form.add(this.surname, formData);
        this.form.add(this.email, formData);
        this.form.add(this.mobile, formData);
    }

    private void initForm() {

        // main form panel
        this.form = new FormPanel();
        this.form.setLabelSeparator("");
        this.form.setBodyBorder(false);
        this.form.setHeaderVisible(false);
        this.form.setScrollMode(Scroll.AUTOY);

        initFields();
        initButtons();

        this.window.add(form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setSize(323, 250);
        this.window.setResizable(false);
        this.window.setPlain(true);
        this.window.setClosable(false);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Register");
        this.window.setMonitorWindowResize(true);

        initForm();
    }

    private void onFailure() {
        MessageBox.confirm(null, "Failed to register. Retry?", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                Button b = be.getButtonClicked();
                if (b.getText().equalsIgnoreCase("yes")) {
                    register();
                } else {
                    setBusy(false);
                }
            }
        });
    }

    private void register() {

        setBusy(true);

        AppEvent request = new AppEvent(RegisterEvents.RegisterRequest);
        request.setData("username", this.username.getValue());
        request.setData("password", this.password.getValue());
        request.setData("name", this.name.getValue());
        request.setData("surname", this.surname.getValue());
        request.setData("email", this.email.getValue());
        request.setData("mobile", this.mobile.getValue());
        fireEvent(request);
    }

    private void resetFields() {
        this.username.clear();
        this.password.clear();
        this.name.clear();
        this.surname.clear();
        this.email.clear();
        this.mobile.clear();
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.submit.setIcon(IconHelper.create(Constants.ICON_LOADING));
        } else {
            this.submit.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
        }
    }

    /**
     * Defines how to submit the form, and the actions to take when the form is submitted.
     */
    private void setupSubmit(final FormPanel form) {

        // ENTER-key listener to submit the form using the keyboard
        final KeyListener submitListener = new KeyListener() {
            @Override
            public void componentKeyDown(ComponentEvent event) {
                if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (form.isValid()) {
                        form.submit();
                    }
                }
            }
        };
        this.username.addKeyListener(submitListener);
        this.password.addKeyListener(submitListener);

        // form action is not a regular URL, but we perform a custom login request instead
        form.setAction("javascript:;");
        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                validatePhoneNumber();
            }

        });
    }

    private void showWindow() {
        resetFields();
        this.window.show();
        this.window.center();
    }

    private void validatePhoneNumber() {

        setBusy(true);

        PhoneNumberParserAsync parser = GWT.create(PhoneNumberParser.class);
        parser.getOutputForSingleNumber(mobile.getValue(), "NL", new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                mobile.setRawValue("Error validating");
                setBusy(false);
            }

            @Override
            public void onSuccess(String result) {
                if (result.equals("not valid")) {
                    phoneValidatorMessage = result;
                    mobile.validate();
                    setBusy(false);
                } else if (result.equals("not mobile")) {
                    phoneValidatorMessage = result;
                    mobile.validate();
                    setBusy(false);
                } else {
                    mobile.setRawValue(result);
                    register();
                }
            }
        });
    }
}
