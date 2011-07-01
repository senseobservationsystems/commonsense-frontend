package nl.sense_os.commonsense.client.common.components;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.KeyCodes;

public class LoginForm extends FormPanel {

    private static final Logger LOG = Logger.getLogger(LoginForm.class.getName());
    private TextField<String> username;
    private TextField<String> password;
    private CheckBox rememberMe;
    private Button submit;
    private Button google;

    public LoginForm() {
        super();

        setBodyBorder(false);
        setHeaderVisible(false);
        setScrollMode(Scroll.AUTOY);
        this.setHeight(175);
        setLabelAlign(LabelAlign.TOP);

        initFields();
        initButtons();
    }

    public String getPassword() {
        return password.getValue();
    }

    public boolean getRememberMe() {
        return rememberMe.getValue();
    }

    public String getUsername() {
        return username.getValue();
    }

    private void initButtons() {

        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent be) {
                final Button b = be.getButton();
                if (b.equals(submit)) {
                    submit();
                } else if (b.equals(google)) {
                    onGoogleClick();
                } else {
                    LOG.severe("Unknown button pressed!");
                }
            }
        };

        // submit button
        submit = new Button("Sign in", SenseIconProvider.ICON_BUTTON_GO, l);
        submit.setType("submit");

        // google login button
        google = new Button("Use Google account", SenseIconProvider.ICON_GOOGLE, l);

        setButtonAlign(HorizontalAlignment.CENTER);
        addButton(submit);
        addButton(google);

        final FormButtonBinding binding = new FormButtonBinding(this);
        binding.addButton(submit);

        setupSubmit();
    }

    private void onGoogleClick() {
        Dispatcher.forwardEvent(LoginEvents.GoogleAuthRequest);
    }

    private void initFields() {

        final FormData formData = new FormData("-10");

        // username field
        username = new TextField<String>();
        username.setFieldLabel("Username");
        username.setAllowBlank(false);

        // password field
        password = new TextField<String>();
        password.setFieldLabel("Password");
        password.setAllowBlank(false);
        password.setPassword(true);

        // remember me check box
        rememberMe = new CheckBox();
        rememberMe.setHideLabel(true);
        rememberMe.setBoxLabel("Remember username");
        rememberMe.setValue(true);

        this.add(username, formData);
        this.add(password, formData);
        this.add(rememberMe, formData);
    }

    public void setBusy(boolean busy) {
        if (busy) {
            submit.setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            submit.setIcon(SenseIconProvider.ICON_BUTTON_GO);
        }
    }

    public void setPassword(String password) {
        if (null != password) {
            this.password.setValue(password);
        } else {
            this.password.clear();
        }
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe.setValue(rememberMe);
    }

    /**
     * Defines how to submit the form, and the actions to take when the form is submitted.
     */
    private void setupSubmit() {

        // ENTER-key listener to submit the form using the keyboard
        final KeyListener submitListener = new KeyListener() {
            @Override
            public void componentKeyDown(ComponentEvent event) {
                if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (isValid()) {
                        submit();
                    }
                }
            }
        };
        username.addKeyListener(submitListener);
        password.addKeyListener(submitListener);

        // form action is not a regular URL, but we listen for the submit event instead
        setAction("javascript:;");
    }

    public void setUsername(String username) {
        if (null != username && !username.equalsIgnoreCase("null")) {
            this.username.setValue(username);
        } else {
            this.username.clear();
        }
    }
}
