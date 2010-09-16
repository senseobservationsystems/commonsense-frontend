package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Date;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.MD5Wrapper;
import nl.sense_os.commonsense.dto.UserModel;

public class Login extends LayoutContainer {

    private static final String TAG = "LoginForm";
    private final AsyncCallback<UserModel> callback;
    private boolean autoLogin;
    private String cookieName;
    private String cookiePass;
    private final TextField<String> email = new TextField<String>();
    private final TextField<String> pass = new TextField<String>();

    public Login(AsyncCallback<UserModel> callback) {

        this.callback = callback;
        
        // get user from Cookie
        this.cookieName = Cookies.getCookie("user_name");
        this.cookiePass = Cookies.getCookie("user_pass");
        if ((null != cookieName) && (null != cookiePass) && (cookieName.length() > 0)
                && (cookiePass.length() > 0)) {
            Log.d(TAG, "Autologin");
            this.autoLogin = true;
        } else {
            this.autoLogin = false;
        }
    }

    private void checkLogin(String name, String password) {

        // show progress dialog
        final MessageBox waitBox = MessageBox.wait("CommonSense Login",
                "Logging in, please wait...", "Logging in...");

        final AsyncCallback<UserModel> callback = new AsyncCallback<UserModel>() {
            @Override
            public void onFailure(Throwable ex) {
                waitBox.close();
                MessageBox.alert("Login failure!", "Server-side failure.", null);
                pass.clear();
            }

            @Override
            public void onSuccess(UserModel user) {
                waitBox.close();
                if (user != null) {

                    final long DURATION = 1000 * 60 * 60 * 24 * 14; // 2 weeks
                    Date expires = new Date(System.currentTimeMillis() + DURATION);
                    Cookies.setCookie("user_name", user.getName(), expires, null, "/", false);
                    Cookies.setCookie("user_pass", user.getPassword(), expires, null, "/", false);

                    Login.this.callback.onSuccess(user);

                } else {
                    MessageBox.alert("Login failure!", "Invalid username or password.", null);
                    pass.clear();
                }
            }
        };
        final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);
        service.checkLogin(name, password, callback);
    }

    private FormPanel createForm() {
        final FormData formData = new FormData("-20");

        // email field        
        email.setFieldLabel("Email");
        if (this.autoLogin) {
            email.setValue(this.cookieName);
        }
        email.setAllowBlank(false);

        // password field
        pass.setFieldLabel("Password");
        if (this.autoLogin) {
            pass.setValue("********");
        }
        pass.setAllowBlank(false);
        pass.setPassword(true);

        // submit button
        final Button b = new Button("Submit");
        b.setType("submit");
        b.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent be) {
                final String mailString = email.getValue();
                final String passString = MD5Wrapper.toMD5(pass.getValue());
                pass.setValue("********");

                checkLogin(mailString, passString);
            }
        });

        // main form panel
        final FormPanel form = new FormPanel();
        form.setBodyStyle("padding: 6px");
        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.setFrame(true);
        form.setHeading("CommonSense Login");
        form.setWidth(350);

        form.add(email, formData);
        form.add(pass, formData);
        form.addButton(b);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(b);

        return form;
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        final FormPanel form = createForm();

        this.setLayout(new CenterLayout());
        this.add(form);

        if (this.autoLogin) {
            checkLogin(this.cookieName, this.cookiePass);
        }
    }
}
