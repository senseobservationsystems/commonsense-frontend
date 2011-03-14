package nl.sense_os.commonsense.client.register;

import nl.sense_os.commonsense.client.common.forms.UserForm;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class RegisterPanel extends View {

    private static final String TAG = "RegisterPanel";
    private ContentPanel panel;
    private UserForm form;

    public RegisterPanel(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(RegisterEvents.Show)) {
            // Log.d(TAG, "Show");
            LayoutContainer parent = event.<LayoutContainer> getData("parent");
            showWindow(parent);

        } else if (type.equals(RegisterEvents.RegisterSuccess)) {
            // Log.d(TAG, "RegisterSuccess");
            setBusy(false);

        } else if (type.equals(RegisterEvents.RegisterFailure)) {
            Log.w(TAG, "RegisterFailure");
            onFailure();

        } else {
            Log.e(TAG, "Unexpected event type!");
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel();
        this.panel.setLayout(new FitLayout());
        this.panel.setHeading("Register");

        this.form = new UserForm();
        this.panel.add(form);

        this.form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                register();
            }

        });
    }

    private void onFailure() {
        setBusy(false);
        MessageBox.confirm(null,
                "Registration failed! Your username might already be taken.\n\nRetry anyway?",
                new Listener<MessageBoxEvent>() {

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
        request.setData("username", this.form.getUsername());
        request.setData("password", this.form.getPassword());
        request.setData("name", this.form.getName());
        request.setData("surname", this.form.getSurname());
        request.setData("email", this.form.getEmail());
        request.setData("mobile", this.form.getMobile());

        fireEvent(request);
    }

    private void resetFields() {
        this.form.setUsername(null);
        this.form.setPassword(null);
        this.form.setName(null);
        this.form.setSurname(null);
        this.form.setEmail(null);
        this.form.setMobile(null);
    }

    private void setBusy(boolean busy) {
        this.form.setBusy(busy);
    }

    private void showWindow(LayoutContainer parent) {
        resetFields();
        parent.add(this.panel);
        parent.layout();
    }
}
