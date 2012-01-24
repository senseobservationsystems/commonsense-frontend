package nl.sense_os.commonsense.client.auth.login;

import java.util.Date;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.LoginForm;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.main.components.NavPanel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;

public class LoginPanel extends View {

    private static final Logger LOG = Logger.getLogger(LoginPanel.class.getName());
    private ContentPanel panel;
    private LoginForm form;

    public LoginPanel(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(LoginEvents.Show)) {
            // LOG.fine("Show");
            LayoutContainer parent = event.<LayoutContainer> getData("parent");
            showPanel(parent);

        } else if (type.equals(LoginEvents.AuthenticationFailure)) {
            LOG.warning("AuthenticationFailure");
            onAuthenticationFailure(event);

        } else if (type.equals(LoginEvents.LoginSuccess)) {
            // LOG.fine("LoginSuccess");
            onLoggedIn(event);

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // LOG.fine("LoggedOut");
            onLoggedOut(event);

        } else if (type.equals(LoginEvents.LoginFailure)) {
            LOG.warning("LoginFailure");
            onError(event);

        } else if (type.equals(LoginEvents.GoogleAuthConflict)) {
            LOG.warning("GoogleAuthConflict");
            final String email = event.getData("email");
            onGoogleAuthConflict(email);

        } else if (type.equals(LoginEvents.GoogleAuthError)) {
            LOG.warning("GoogleAuthError");
            final String msg = event.getData("msg");
            onGoogleAuthError(msg);

        } else {
            LOG.severe("Unexpected event type: " + type);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel();
        this.panel.setLayout(new FitLayout());
        this.panel.setHeading("Login");

        this.form = new LoginForm();
        this.form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                requestLogin();
            }

        });
        this.panel.add(this.form);

        this.form.layout();
    }

    private void onAuthenticationFailure(AppEvent event) {
        MessageBox.alert(null,
                "Failed to sign in: incorrect user name or password. Please try again.",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        resetFormValues();
                        navigateHome();
                    }
                });
        setBusy(false);
    }

    private void navigateHome() {
        String startLocation = NavPanel.HOME;
        History.newItem(startLocation);
        History.fireCurrentHistoryState();
    }

    private void onError(AppEvent event) {
        MessageBox.alert(null, "Failed to sign in. Please try again.",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        resetFormValues();
                        navigateHome();
                    }
                });
    }

    private void onGoogleAuthConflict(String email) {

        final Window window = new Window();
        window.setLayout(new FitLayout());
        window.setSize(325, 300);
        window.setClosable(false);
        window.setHeading("Login Failed");

        FormPanel connectForm = new FormPanel();
        connectForm.setLabelAlign(LabelAlign.TOP);
        connectForm.setHeaderVisible(false);
        connectForm.setBodyBorder(false);
        connectForm.setScrollMode(Scroll.AUTOY);

        LabelField explanation = new LabelField(
                "Your Google email address is already registered with a Sense account!"
                        + "<br><br>"
                        + "Please enter the username and password of your Sense account, so we can link your Google account to it.");
        explanation.setHideLabel(true);

        // form fields
        final TextField<String> username = new TextField<String>();
        username.setFieldLabel("Sense Username");
        username.setValue(email);
        username.setAllowBlank(false);

        final TextField<String> password = new TextField<String>();
        password.setFieldLabel("Sense Password");
        password.setAllowBlank(false);
        password.setPassword(true);

        // add fields to form
        connectForm.add(explanation, new FormData("-10"));
        connectForm.add(username, new FormData("-10"));
        connectForm.add(password, new FormData("-10"));

        // buttons
        Button submit = new Button("Connect");
        submit.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                window.hide();

                AppEvent connect = new AppEvent(LoginEvents.GoogleConnectRequest);
                connect.setData("username", username.getValue());
                connect.setData("password", password.getValue());
                fireEvent(connect);
            }
        });
        new FormButtonBinding(connectForm).addButton(submit);

        Button cancel = new Button("Cancel");
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                window.hide();

                String startLocation = NavPanel.HOME;
                History.newItem(startLocation);
                History.fireCurrentHistoryState();
            }
        });

        // add form and buttons to window
        window.add(connectForm);
        window.addButton(submit);
        window.addButton(cancel);

        // show window
        window.show();
    }

    private void onGoogleAuthError(String msg) {

        MessageBox.alert(null, "Failed to get login credentials from Google!"
                + "<br><br>Error message: '" + msg + "'", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                String startLocation = NavPanel.HOME;
                History.newItem(startLocation);
                History.fireCurrentHistoryState();
            }
        });
    }

    private void onLoggedIn(AppEvent event) {

        // save new user name if the user wants it
        if (this.form.getRememberMe()) {
            long expiry = 1000l * 60 * 60 * 24 * 14; // 2 weeks
            Date expires = new Date(new Date().getTime() + expiry);
            Cookies.setCookie("username", this.form.getUsername(), expires);
        } else {
            Cookies.removeCookie("username");
        }
    }

    private void onLoggedOut(AppEvent event) {
        resetFormValues();
    }

    private void requestLogin() {
        setBusy(true);
        AppEvent event = new AppEvent(LoginEvents.LoginRequest);
        event.setData("username", this.form.getUsername());
        event.setData("password", this.form.getPassword());
        Dispatcher.forwardEvent(event);
    }

    private void resetFormValues() {

        if (form.getUsername() == null || "".equals(form.getUsername())) {

            // auto-fill username field from cookie
            final String cookieName = Cookies.getCookie("username");
            if (null != cookieName) {
                this.form.setUsername(cookieName);
            }
        }

        // clear password field
        this.form.setPassword(null);

        // auto-set remember me checkbox
        this.form.setRememberMe(true);

        setBusy(false);
    }

    private void setBusy(boolean busy) {
        this.form.setBusy(busy);
    }

    private void showPanel(LayoutContainer parent) {
        resetFormValues();
        parent.add(this.panel);
        parent.layout();
    }
}
