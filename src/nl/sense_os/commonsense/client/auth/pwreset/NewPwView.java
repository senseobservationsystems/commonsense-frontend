package nl.sense_os.commonsense.client.auth.pwreset;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window.Location;

public class NewPwView extends View {

    private static final Logger LOG = Logger.getLogger(NewPwView.class.getName());

    private FormPanel form;
    private Button submit;

    private String token;

    private TextField<String> password;

    private TextField<String> retype;

    public NewPwView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (PwResetEvents.ShowNewPasswordForm.equals(type)) {
            LOG.finest("ShowNewPasswordForm");
            LayoutContainer parent = event.getData("parent");
            show(parent);

        } else if (PwResetEvents.NewPasswordSuccess.equals(type)) {
            LOG.finest("NewPasswordSuccess");
            onSuccess();

        } else if (PwResetEvents.NewPasswordFailure.equals(type)) {
            LOG.finest("NewPasswordFailure");
            onFailure();

        } else {
            LOG.severe("Unexpected event: " + event);
        }
    }

    @Override
    protected void initialize() {

        LabelField explanation = new LabelField("Please enter a new password for your account.");
        explanation.setHideLabel(true);

        password = new TextField<String>();
        password.setFieldLabel("New password");
        password.setPassword(true);
        password.setAllowBlank(false);

        retype = new TextField<String>();
        retype.setFieldLabel("Retype password");
        retype.setPassword(true);
        retype.setAllowBlank(false);
        retype.setValidator(new Validator() {

            @Override
            public String validate(Field<?> field, String value) {
                if (value != null && value.equals(password.getValue())) {
                    return null;
                } else {
                    return "Passwords do not match";
                }
            }
        });

        submit = new Button("Submit", SenseIconProvider.ICON_BUTTON_GO,
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        form.submit();
                    }
                });
        submit.setType("submit");

        form = new FormPanel();
        form.setSize(300, 200);
        form.setScrollMode(Scroll.AUTOY);
        form.setHeading("Choose new password");
        form.setLabelAlign(LabelAlign.TOP);
        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                submit();
            }
        });

        form.add(explanation, new FormData(""));
        form.add(password, new FormData("-10"));
        form.add(retype, new FormData("-10"));
        form.addButton(submit);

        new FormButtonBinding(form).addButton(submit);
    }

    private void onFailure() {
        setBusy(false);
        MessageBox.alert("Password reset", "Failed to set the new password!", null);
    }
    private void onSuccess() {
        setBusy(false);
        MessageBox.info("Password reset", "Your password was successfully changed.",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        History.newItem(NavPanel.HOME);
                    }
                });
    }

    private void setBusy(boolean busy) {
        if (busy) {
            submit.setIcon(SenseIconProvider.ICON_LOADING);
            submit.setEnabled(false);
        } else {
            submit.setIcon(SenseIconProvider.ICON_BUTTON_GO);
            submit.setEnabled(true);
        }
    }

    private void show(LayoutContainer parent) {

        token = Location.getParameter("token");

        LayoutContainer lc = new LayoutContainer(new CenterLayout());
        lc.add(form);

        parent.add(lc);
        parent.layout();
    }

    private void submit() {
        setBusy(true);

        AppEvent event = new AppEvent(PwResetEvents.NewPasswordRequest);
        event.setData("token", token);
        event.setData("password", password.getValue());
        Dispatcher.forwardEvent(event);
    }
}
