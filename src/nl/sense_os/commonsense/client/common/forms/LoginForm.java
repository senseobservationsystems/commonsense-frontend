package nl.sense_os.commonsense.client.common.forms;

import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.KeyCodes;

public class LoginForm extends FormPanel {
    private TextField<String> username;
    private TextField<String> password;
    private CheckBox rememberMe;
    private Button submit;

    public LoginForm() {
        super();

        this.setBodyBorder(false);
        this.setHeaderVisible(false);
        this.setScrollMode(Scroll.AUTOY);
        this.setHeight(170);
        this.setLabelAlign(LabelAlign.TOP);

        initFields();
        initButtons();
    }

    public String getPassword() {
        return this.password.getValue();
    }

    public boolean getRememberMe() {
        return this.rememberMe.getValue();
    }

    public String getUsername() {
        return this.username.getValue();
    }

    private void initButtons() {

        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent be) {
                final Button b = be.getButton();
                if (b.equals(submit)) {
                    submit();
                }
            }
        };

        // submit button
        this.submit = new Button("Sign in", IconHelper.create(Constants.ICON_BUTTON_GO), l);
        this.submit.setType("submit");

        this.setButtonAlign(HorizontalAlignment.CENTER);
        this.addButton(submit);

        final FormButtonBinding binding = new FormButtonBinding(this);
        binding.addButton(submit);

        setupSubmit();
    }

    private void initFields() {

        final FormData formData = new FormData("-10");

        // username field
        this.username = new TextField<String>();
        this.username.setFieldLabel("Username");
        this.username.setAllowBlank(false);

        // password field
        this.password = new TextField<String>();
        this.password.setFieldLabel("Password");
        this.password.setAllowBlank(false);
        this.password.setPassword(true);

        // remember me check box
        this.rememberMe = new CheckBox();
        this.rememberMe.setHideLabel(true);
        this.rememberMe.setBoxLabel("Remember username");
        this.rememberMe.setValue(true);

        this.add(this.username, formData);
        this.add(this.password, formData);
        this.add(this.rememberMe, formData);
    }

    public void setBusy(boolean busy) {
        if (busy) {
            this.submit.setIcon(IconHelper.create(Constants.ICON_LOADING));
        } else {
            this.submit.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
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
        this.username.addKeyListener(submitListener);
        this.password.addKeyListener(submitListener);

        // form action is not a regular URL, but we listen for the submit event instead
        this.setAction("javascript:;");
    }

    public void setUsername(String username) {
        if (null != username) {
            this.username.setValue(username);
        } else {
            this.username.clear();
        }
    }
}
