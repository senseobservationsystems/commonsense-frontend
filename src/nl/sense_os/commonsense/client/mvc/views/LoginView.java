package nl.sense_os.commonsense.client.mvc.views;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
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

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;

public class LoginView extends View {

    private static final String TAG = "LoginView";
    private final LayoutContainer loginPanel = new LayoutContainer(new CenterLayout());
    private final TextField<String> password = new TextField<String>();
    private final CheckBox rememberMe = new CheckBox();
    private final TextField<String> username = new TextField<String>();
    
    public LoginView(Controller controller) {
        super(controller);
    }
    
    private FormPanel createForm() {
        final FormData formData = new FormData("-10");

        // username field
        username.setFieldLabel("Username:");
        username.setAllowBlank(false);

        // auto-fill username from cookie
        final String cookieName = Cookies.getCookie("username");
        if (null != cookieName) {
            this.username.setValue(cookieName);
            this.username.setOriginalValue(cookieName);
        }

        // password field (will not be POSTed)
        password.setFieldLabel("Password:");
        password.setAllowBlank(false);
        password.setPassword(true);

        rememberMe.setLabelSeparator("");
        rememberMe.setBoxLabel("Remember username");
        rememberMe.setValue(true);

        // main form panel
        final FormPanel form = new FormPanel();
        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.setLabelSeparator("");
        form.setFrame(true);
        form.setHeading("CommonSense Login");
        form.setLabelWidth(100);
        form.setWidth(350);

        setupSubmitAction(form);

        form.add(username, formData);
        form.add(password, formData);
        form.add(rememberMe);

        return form;
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType eventType = event.getType();
        if (eventType.equals(MainEvents.Init)) {
            onInit(event);
        } else if (eventType.equals(LoginEvents.AuthenticationFailure)) {
            onAuthenticationFailure(event);
        } else if (eventType.equals(LoginEvents.LoggedIn)) {
            onLoggedIn(event);
        } else if (eventType.equals(LoginEvents.LoggedOut)) {
            onLoggedOut(event);
        } else if (eventType.equals(LoginEvents.LoginError)) {
            onLoginError(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + eventType);
        }
    }
    
    /**
     * Defines how to submit the form, and the actions to take when the form is submitted.
     */
    private void setupSubmitAction(final FormPanel form) {

        // submit button
        final Button b = new Button("Submit");
        b.setType("submit");
        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent be) {
                form.submit();
            }
        });
        form.addButton(b);
        form.setButtonAlign(HorizontalAlignment.CENTER);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(b);

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
        username.addKeyListener(submitListener);
        password.addKeyListener(submitListener);

        // form action is not a regular URL, but we perform a custom login request instead
        form.setAction("javascript:;");
        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                requestLogin();
            }

        });
    }
    
    private void onAuthenticationFailure(AppEvent event) {
        Log.d(TAG, "onAuthenticationFailure");
        
        this.password.clear();
        MessageBox.alert(null, "Failed to sign in: incorrect user name or password. Please try again.",
                null);
    }

    private void onInit(AppEvent event) {
        Log.d(TAG, "onInit");
        
        final FormPanel form = createForm();
        this.loginPanel.add(form);
        
        Dispatcher.forwardEvent(new AppEvent(LoginEvents.LoginPanelReady, this.loginPanel));
    }
    
    private void onLoggedIn(AppEvent event) {
        Log.d(TAG, "onLoggedIn");
        
        if (rememberMe.getValue()) {
            long expiry = 1000l * 60 * 60 * 24 * 14; // 2 weeks
            Date expires = new Date(new Date().getTime() + expiry);
            Cookies.setCookie("username", username.getValue(), expires);
        } else {
            Cookies.removeCookie("username");
        }
        
        // auto-fill username from cookie
        final String cookieName = Cookies.getCookie("username");
        if (null != cookieName) {
            this.username.setValue(cookieName);
            this.username.setOriginalValue(cookieName);
        }
        this.password.clear();
    }
    
    private void onLoggedOut(AppEvent event) {
        Log.d(TAG, "onLoggedOut");
        
        // auto-fill username from cookie
        final String cookieName = Cookies.getCookie("username");
        if (null != cookieName) {
            this.username.setValue(cookieName);
            this.username.setOriginalValue(cookieName);
        }
        this.password.clear();
    }
    
    private void onLoginError(AppEvent event) {
        Log.d(TAG, "onLoginError");
        
        this.password.clear();
        MessageBox.alert(null, "Failed to sign in. Please try again.", null);
    }
    
    private void requestLogin() {
        AppEvent event = new AppEvent(LoginEvents.RequestLogin);
        event.setData("username", this.username.getValue());
        event.setData("password", this.password.getValue());
        Dispatcher.forwardEvent(event);
    }
}
