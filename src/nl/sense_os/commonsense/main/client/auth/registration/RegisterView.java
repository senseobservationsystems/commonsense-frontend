package nl.sense_os.commonsense.main.client.auth.registration;

import java.util.logging.Logger;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.http.client.Response;

public class RegisterView extends View {

    private static final Logger LOG = Logger.getLogger(RegisterView.class.getName());
    // private ContentPanel panel;
    private RegisterForm form;

    public RegisterView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(RegisterEvents.Show)) {
            // LOG.fine( "Show");
            LayoutContainer parent = event.<LayoutContainer> getData("parent");
            showPanel(parent);

        } else if (type.equals(RegisterEvents.RegisterSuccess)) {
            // LOG.fine( "RegisterSuccess");
            onSuccess();

        } else if (type.equals(RegisterEvents.RegisterFailure)) {
            LOG.warning("RegisterFailure");
            final int code = event.getData("code");
            onFailure(code);

        } else {
            LOG.severe("Unexpected event type!");
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        // this.panel = new ContentPanel();
        // this.panel.setLayout(new FitLayout());
        // this.panel.setHeading("Register");

        this.form = new RegisterForm();
        this.form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                register();
            }
        });
        // this.panel.add(form);
    }

    private void onSuccess() {
        setBusy(false);
        form.reset();
    }

    private void onFailure(int code) {

        setBusy(false);

        if (code == Response.SC_CONFLICT) {
            MessageBox.alert(null,
                    "Registration failed!\n\nYour username is already taken. Try another one.",
                    new Listener<MessageBoxEvent>() {

                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            setBusy(false);
                        }
                    });
        } else {
            MessageBox.confirm(null, "Something went wrong during registration.\n\nRetry?",
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

    private void setBusy(boolean busy) {
        form.setBusy(busy);
    }

    private void showPanel(LayoutContainer parent) {
        form.reset();
        parent.add(form);
        parent.layout();
    }
}
