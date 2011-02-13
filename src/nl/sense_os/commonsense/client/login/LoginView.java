package nl.sense_os.commonsense.client.login;

import java.util.Date;

import nl.sense_os.commonsense.client.common.grid.CenteredWindow;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
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
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Cookies;

public class LoginView extends View {

    private static final String TAG = "LoginView";
    private CenteredWindow window;
    private FormPanel form;
    private TextField<String> password;
    private CheckBox rememberMe;
    private Button cancel;
    private Button submit;
    private TextField<String> username;

    public LoginView(Controller controller) {
        super(controller);
    }

    private void cancelLogin() {
        Dispatcher.forwardEvent(LoginEvents.CancelLogin);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType eventType = event.getType();
        if (eventType.equals(LoginEvents.Show)) {
            // Log.d(TAG, "Show");
            onShow(event);
        } else if (eventType.equals(LoginEvents.Hide)) {
            // Log.d(TAG, "Hide");
            hideWindow();
        } else if (eventType.equals(LoginEvents.AuthenticationFailure)) {
            Log.w(TAG, "AuthenticationFailure");
            onAuthenticationFailure(event);
        } else if (eventType.equals(LoginEvents.LoggedIn)) {
            Log.d(TAG, "LoggedIn");
            onLoggedIn(event);
        } else if (eventType.equals(LoginEvents.LoggedOut)) {
            Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else if (eventType.equals(LoginEvents.LoginError)) {
            Log.w(TAG, "LoginError");
            onError(event);
        } else if (eventType.equals(LoginEvents.LoginCancelled)) {
            // Log.d(TAG, "LoginCancelled");
            onCancelled(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + eventType);
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
                } else if (b.equals(cancel)) {
                    cancelLogin();
                }
            }
        };

        // submit button
        this.submit = new Button("Submit", IconHelper.create(Constants.ICON_BUTTON_GO), l);
        this.submit.setType("submit");

        this.cancel = new Button("Cancel", l);
        this.cancel.disable();

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(submit);
        this.form.addButton(cancel);

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
        this.password.setAllowBlank(false);
        this.password.setPassword(true);

        // remember me check box
        this.rememberMe = new CheckBox();
        this.rememberMe.setLabelSeparator("");
        this.rememberMe.setBoxLabel("Remember username");
        this.rememberMe.setValue(true);

        this.form.add(this.username, formData);
        this.form.add(this.password, formData);
        this.form.add(this.rememberMe);
    }

    private void initForm() {

        // main form panel
        this.form = new FormPanel();
        this.form.setLabelSeparator("");
        this.form.setBodyBorder(false);
        this.form.setHeaderVisible(false);

        initFields();
        initButtons();

        this.window.add(form);

        resetFormValues();
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setSize(323, 200);
        this.window.setResizable(false);
        this.window.setPlain(true);
        this.window.setClosable(false);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Login");
        this.window.setMonitorWindowResize(true);

        initForm();
    }

    private void onAuthenticationFailure(AppEvent event) {
        MessageBox.alert(null,
                "Failed to sign in: incorrect user name or password. Please try again.",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        resetFormValues();
                    }
                });
        setBusy(false);
    }

    private void onCancelled(AppEvent event) {
        resetFormValues();
    }

    private void onError(AppEvent event) {
        MessageBox.alert(null, "Failed to sign in. Please try again.",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        resetFormValues();
                    }
                });
    }

    private void onLoggedIn(AppEvent event) {

        // save new user name if the user wants it
        if (this.rememberMe.getValue()) {
            long expiry = 1000l * 60 * 60 * 24 * 14; // 2 weeks
            Date expires = new Date(new Date().getTime() + expiry);
            Cookies.setCookie("username", this.username.getValue(), expires);
            username.setOriginalValue(username.getValue());
        } else {
            Cookies.removeCookie("username");
            username.setOriginalValue("");
        }

        hideWindow();
    }

    private void onLoggedOut(AppEvent event) {
        resetFormValues();
    }

    private void onShow(AppEvent event) {
        resetFormValues();
        this.window.show();
    }

    private void requestLogin() {
        setBusy(true);
        AppEvent event = new AppEvent(LoginEvents.RequestLogin);
        event.setData("username", this.username.getValue());
        event.setData("password", this.password.getValue());
        Dispatcher.forwardEvent(event);
    }

    private void resetFormValues() {
        // auto-fill username field from cookie
        final String cookieName = Cookies.getCookie("username");
        if (null != cookieName) {
            this.username.setValue(cookieName);
            this.username.setOriginalValue(cookieName);
        }

        // clear password field
        this.password.clear();

        // auto-set remember me checkbox
        this.rememberMe.setValue(true);

        setBusy(false);
    }

    private void setBusy(boolean busy) {
        // Log.d(TAG, "setBusy(" + busy + ")");
        if (busy) {
            this.submit.setIcon(IconHelper.create(Constants.ICON_LOADING));
            this.cancel.enable();
        } else {
            this.submit.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
            this.cancel.disable();
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
                requestLogin();
            }

        });
    }
}
