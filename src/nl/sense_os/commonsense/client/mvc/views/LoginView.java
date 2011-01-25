package nl.sense_os.commonsense.client.mvc.views;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;

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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Cookies;

import java.util.Date;

public class LoginView extends View {

    private static final String TAG = "LoginView";
    private LayoutContainer loginPanel;
    private TextField<String> password;
    private CheckBox rememberMe;
    private Button submit;
    private TextField<String> username;

    public LoginView(Controller controller) {
        super(controller);
    }

    private FormPanel createForm() {
        final FormData formData = new FormData("-10");

        // username field
        this.username.setFieldLabel("Username:");
        this.username.setAllowBlank(false);

        // password field
        this.password.setFieldLabel("Password:");
        this.password.setAllowBlank(false);
        this.password.setPassword(true);

        // remember me check box
        this.rememberMe.setLabelSeparator("");
        this.rememberMe.setBoxLabel("Remember username");
        this.rememberMe.setValue(true);

        // main form panel
        final FormPanel form = new FormPanel();
        form.setLabelSeparator("");
        form.setFrame(true);
        form.setHeading("CommonSense Login");
        form.setLabelWidth(100);
        form.setWidth(350);

        // submit button
        submit = new Button("Submit");
        submit.setType("submit");
        submit.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent be) {
                form.submit();
            }
        });
        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.addButton(submit);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(submit);

        Button cancel = new Button("Cancel");
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent be) {
                cancelLogin();
            }
        });
        form.addButton(cancel);

        setupSubmit(form);

        form.add(this.username, formData);
        form.add(this.password, formData);
        form.add(this.rememberMe);

        return form;
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType eventType = event.getType();
        if (eventType.equals(MainEvents.ShowLogin)) {
            // Log.d(TAG, "onShow");
            onShow(event);
        } else if (eventType.equals(LoginEvents.AuthenticationFailure)) {
            Log.w(TAG, "AuthenticationFailure");
            onAuthenticationFailure(event);
        } else if (eventType.equals(LoginEvents.LoggedIn)) {
            // Log.d(TAG, "LoggedIn");
            onLoggedIn(event);
        } else if (eventType.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else if (eventType.equals(LoginEvents.LoginError)) {
            Log.w(TAG, "LoginError");
            onError(event);
        } else if (eventType.equals(LoginEvents.LoginCancelled)) {
            Log.d(TAG, "LoginCancelled");
            onCancelled(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + eventType);
        }
    }

    private void onCancelled(AppEvent event) {
        resetFormValues();
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.loginPanel = new LayoutContainer(new CenterLayout());
        this.password = new TextField<String>();
        this.rememberMe = new CheckBox();
        this.username = new TextField<String>();

        final FormPanel form = createForm();
        this.loginPanel.add(form);

        resetFormValues();
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

    private void onLoggedIn(AppEvent event) {

        // save new user name if the user wants it
        if (this.rememberMe.getValue()) {
            long expiry = 1000l * 60 * 60 * 24 * 14; // 2 weeks
            Date expires = new Date(new Date().getTime() + expiry);
            Cookies.setCookie("username", this.username.getValue(), expires);
        } else {
            Cookies.removeCookie("username");
        }
    }

    private void onLoggedOut(AppEvent event) {
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

    private void onShow(AppEvent event) {

        resetFormValues();

        ContentPanel center = event.<ContentPanel> getData();
        center.removeAll();
        center.add(this.loginPanel);
        center.layout();
    }

    private void requestLogin() {
        setBusy(true);
        AppEvent event = new AppEvent(LoginEvents.RequestLogin);
        event.setData("username", this.username.getValue());
        event.setData("password", this.password.getValue());
        Dispatcher.forwardEvent(event);
    }

    private void cancelLogin() {
        Dispatcher.forwardEvent(LoginEvents.CancelLogin);
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
            this.submit.setIcon(IconHelper.create("gxt/images/gxt/icons/loading.gif"));
        } else {
            this.submit.setIcon(IconHelper.create("gxt/images/gxt/icons/page-next.gif"));
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
