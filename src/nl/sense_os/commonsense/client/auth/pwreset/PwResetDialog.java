package nl.sense_os.commonsense.client.auth.pwreset;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class PwResetDialog extends View {

    private static final Logger LOG = Logger.getLogger(PwResetDialog.class.getName());

    public PwResetDialog(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(PwResetEvents.ShowDialog)) {
            LOG.finest("ShowDialog");
            onShow();

        } else if (PwResetEvents.PwRemindSuccess.equals(type)) {
            LOG.finest("PwRemindSuccess");
            FormPanel form = event.getData("form");
            onSuccess(form);

        } else if (PwResetEvents.PwRemindFailure.equals(type)) {
            LOG.finest("PwRemindFailure");
            FormPanel form = event.getData("form");
            onFailure(form);

        } else if (PwResetEvents.PwRemindNotFound.equals(type)) {
            LOG.finest("PwRemindNotFound");
            FormPanel form = event.getData("form");
            onNotFound(form);

        } else {
            LOG.severe("Unexpected event! " + event);
        }
    }

    private void onNotFound(final FormPanel form) {

        Button submitBtn = (Button) form.getButtonBar().getItemByItemId("pwreset-submit");
        submitBtn.setIconStyle("sense-btn-icon-go");

        MessageBox.alert("Reset password",
                "Sorry, we could not find any users with the details you entered...",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        // do nothing
                    }
                });
    }

    private void onFailure(final FormPanel form) {

        Button submitBtn = (Button) form.getButtonBar().getItemByItemId("pwreset-submit");
        submitBtn.setIconStyle("sense-btn-icon-go");

        MessageBox.confirm("Reset password",
                "Sorry, an error occurred during communication! Retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            form.submit();
                        } else {
                            // do nothing
                        }
                    }
                });
    }

    private void onSuccess(final FormPanel form) {

        Button submitBtn = (Button) form.getButtonBar().getItemByItemId("pwreset-submit");
        submitBtn.setIconStyle("sense-btn-icon-go");

        MessageBox.info("Reset password",
                "We sent you an email with a link to reset your password.",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        ((Window) form.getParent()).hide();
                    }
                });
    }

    private void onShow() {
        Window w = new CenteredWindow();
        w.setHeading("Reset password");
        w.setSize(300, 300);
        w.add(createForm());
        w.show();
    }

    private FormPanel createForm() {
        final FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        form.setLayout(new FormLayout(LabelAlign.TOP));
        form.setScrollMode(Scroll.AUTOY);

        LabelField label = new LabelField(
                "Please enter your email address or username so we can send you an email to reset your password.");
        label.setHideLabel(true);

        final Radio emailRadio = new Radio();
        emailRadio.setBoxLabel("Email:");
        emailRadio.setHideLabel(true);

        final TextField<String> email = new TextField<String>();
        email.setHideLabel(true);
        // email.setAllowBlank(false);

        LabelField or = new LabelField("or");
        or.setHideLabel(true);

        final Radio usernameRadio = new Radio();
        usernameRadio.setBoxLabel("Username:");
        usernameRadio.setHideLabel(true);

        final TextField<String> username = new TextField<String>();
        username.setHideLabel(true);
        // username.setAllowBlank(false);

        form.add(label, new FormData(""));
        form.add(emailRadio, new FormData());
        form.add(email, new FormData("-10"));
        form.add(or, new FormData());
        form.add(usernameRadio, new FormData());
        form.add(username, new FormData("-10"));

        final RadioGroup radios = new RadioGroup();
        radios.add(usernameRadio);
        radios.add(emailRadio);

        radios.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                boolean isEmail = emailRadio.equals(radios.getValue());
                username.setEnabled(!isEmail);
                username.setAllowBlank(isEmail);
                email.setEnabled(isEmail);
                email.setAllowBlank(!isEmail);
            }
        });
        radios.setValue(emailRadio);

        final Button submit = new Button("Submit", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                form.submit();
            }
        });
        submit.setIconStyle("sense-btn-icon-go");
        submit.setItemId("pwreset-submit");
        submit.setType("submit");
        new FormButtonBinding(form).addButton(submit);

        form.addButton(submit);

        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                Button submitBtn = (Button) form.getButtonBar().getItemByItemId("pwreset-submit");
                submitBtn.setIconStyle("sense-btn-icon-loading");

                String emailStr = email.getValue();
                String usernameStr = username.getValue();
                if (emailRadio == radios.getValue()) {
                    resetPassword(emailStr, null, form);
                } else {
                    resetPassword(null, usernameStr, form);
                }
            }
        });

        return form;
    }
    private void resetPassword(String email, String username, FormPanel form) {
        AppEvent event = new AppEvent(PwResetEvents.SubmitRequest);
        event.setData("email", email);
        event.setData("username", username);
        event.setData("form", form);
        Dispatcher.forwardEvent(event);
    }
}
