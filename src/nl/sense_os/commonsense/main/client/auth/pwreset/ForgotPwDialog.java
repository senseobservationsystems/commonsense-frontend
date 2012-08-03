package nl.sense_os.commonsense.main.client.auth.pwreset;

import java.util.logging.Logger;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class ForgotPwDialog extends Window {

    private static final Logger LOG = Logger.getLogger(ForgotPwDialog.class.getName());
    private Button btnSubmit;
    private Button btnCancel;
    private TextField<String> username;
    private TextField<String> email;
    private FormPanel form;
    private RadioGroup radios;

    public ForgotPwDialog() {
        setHeading("Reset password");
        setClosable(false);
        setSize(320, 275);
        setLayout(new FitLayout());

        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        form.setLayout(new FormLayout(LabelAlign.TOP));
        form.setScrollMode(Scroll.AUTOY);
        form.setAction("javascript:;");

        LabelField label = new LabelField(
                "Please enter your email address or username so we can send you an email to reset your password.");
        label.setHideLabel(true);

        final Radio emailRadio = new Radio();
        emailRadio.setBoxLabel("Email:");
        emailRadio.setValueAttribute("email");
        emailRadio.setHideLabel(true);

        email = new TextField<String>();
        email.setHideLabel(true);
        // email.setAllowBlank(false);

        LabelField or = new LabelField("or");
        or.setHideLabel(true);

        final Radio usernameRadio = new Radio();
        usernameRadio.setValueAttribute("username");
        usernameRadio.setBoxLabel("Username:");
        usernameRadio.setHideLabel(true);

        username = new TextField<String>();
        username.setHideLabel(true);
        // username.setAllowBlank(false);

        form.add(label, new FormData("-5"));
        form.add(emailRadio, new FormData());
        form.add(email, new FormData("-20"));
        form.add(or, new FormData());
        form.add(usernameRadio, new FormData());
        form.add(username, new FormData("-20"));

        radios = new RadioGroup();
        radios.add(usernameRadio);
        radios.add(emailRadio);

        radios.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                LOG.finest("Radio changed");
                boolean isEmail = emailRadio.equals(radios.getValue());
                username.setEnabled(!isEmail);
                username.setAllowBlank(isEmail);
                email.setEnabled(isEmail);
                email.setAllowBlank(!isEmail);
            }
        });
        radios.setValue(emailRadio);

        btnSubmit = new Button("Submit");
        btnSubmit.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (form.isValid()) {
                    form.submit();
                }
            }
        });
        btnSubmit.setIconStyle("sense-btn-icon-go");
        btnSubmit.setType("submit");
        new FormButtonBinding(form).addButton(btnSubmit);
        form.addButton(btnSubmit);

        btnCancel = new Button("Cancel");
        form.addButton(btnCancel);

        add(form);
    }

    public String getInformationType() {
        return radios.getValue().getValueAttribute();
    }

    public Button getBtnSubmit() {
        return btnSubmit;
    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    public FormPanel getForm() {
        return form;
    }

    public String getUsername() {
        return username.isEnabled() ? username.getValue() : null;
    }

    public String getEmail() {
        return email.isEnabled() ? email.getValue() : null;
    }

    public void setBusy(boolean busy) {
        if (busy) {
            btnSubmit.setIconStyle("sense-btn-icon-loading");
            btnSubmit.setEnabled(false);
        } else {
            btnSubmit.setIconStyle("sense-btn-icon-go");
            btnSubmit.setEnabled(false);
        }

    }
}
