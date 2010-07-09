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
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import nl.sense_os.commonsense.client.utility.MD5Wrapper;
import nl.sense_os.commonsense.dto.UserModel;

public class Login extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "LoginForm";
    private final AsyncCallback<UserModel> callback;

    public Login(AsyncCallback<UserModel> callback) {
        this.callback = callback;
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
            }

            @Override
            public void onSuccess(UserModel userModel) {
                waitBox.close();                
                if (userModel != null) {
                    Login.this.callback.onSuccess(userModel);
                } else {
                    MessageBox.alert("Login failure!", "Invalid username or password.", null);
                }
            }
        };
        final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);
        service.checkLogin(name, password, callback);
    }

    private FormPanel createForm() {
        final FormData formData = new FormData("-20");

        // email field
        final TextField<String> email = new TextField<String>();
        email.setFieldLabel("Email");
//        email.setValue("vestia@sense-os.nl");
        email.setAllowBlank(false);

        // password field
        final TextField<String> pass = new TextField<String>();
        pass.setFieldLabel("Password");
//        pass.setValue("vestia_delfgauw");
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

        this.add(form);
    }
}
