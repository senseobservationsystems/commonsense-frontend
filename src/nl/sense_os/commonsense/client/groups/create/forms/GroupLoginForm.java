package nl.sense_os.commonsense.client.groups.create.forms;

import nl.sense_os.commonsense.client.common.components.WizardFormPanel;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class GroupLoginForm extends WizardFormPanel {

    private final RadioGroup loginChoice = new RadioGroup();
    private Radio useLogin;
    private Radio doNotUseLogin;
    private final TextField<String> username = new TextField<String>();
    private final TextField<String> password = new TextField<String>();
    private final TextField<String> passConfirm = new TextField<String>();

    public GroupLoginForm() {
        super();

        initRadios();
        initTextFields();
        initLayout();

        // initial values
        useLogin.setValue(true);
        onSelectionChange(useLogin);
    }

    public String getLogin() {
        return username.isEnabled() && username.isValid() ? username.getValue() : null;
    }

    public String getPassword() {
        return passConfirm.isEnabled() && passConfirm.isValid() ? password.getValue() : null;
    }

    private void initLayout() {

        LabelField label = new LabelField("<b>Group username</b>");
        label.setHideLabel(true);

        FormPanel pwForm = new FormPanel();
        pwForm.setLayout(new FormLayout(LabelAlign.LEFT));
        pwForm.setHeaderVisible(false);
        pwForm.setBodyBorder(false);
        pwForm.add(username, new FormData(anchorSpec));
        pwForm.add(password, new FormData(anchorSpec));
        pwForm.add(passConfirm, new FormData(anchorSpec));

        // layout
        setLayout(new FormLayout(LabelAlign.LEFT));
        add(label, new FormData("-5"));
        add(useLogin, new FormData(anchorSpec));
        add(pwForm, new FormData(anchorSpec));
        add(doNotUseLogin, new FormData(anchorSpec));
    }

    private void initRadios() {

        loginChoice.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange(be.getField().getValue());
            }
        });

        useLogin = new Radio();
        useLogin.setValueAttribute("true");
        useLogin.setBoxLabel("The group needs to be able to log in as a user");
        useLogin.setHideLabel(true);
        loginChoice.add(useLogin);

        doNotUseLogin = new Radio();
        doNotUseLogin.setValueAttribute("false");
        doNotUseLogin.setBoxLabel("The group needs no user username");
        doNotUseLogin.setHideLabel(true);
        loginChoice.add(doNotUseLogin);
    }

    private void initTextFields() {

        username.setFieldLabel("Group username");

        password.setFieldLabel("Group password");
        password.setPassword(true);

        passConfirm.setFieldLabel("Confirm password");
        passConfirm.setPassword(true);
        passConfirm.setValidator(new Validator() {

            @Override
            public String validate(Field<?> field, String value) {
                if (password.getValue() == null || password.getValue().length() == 0) {
                    return null;
                } else {
                    if (value.equals(password.getValue())) {
                        return null;
                    } else {
                        return "passwords are not identical";
                    }
                }
            }
        });
    }

    public boolean isGroupLogin() {
        return "true".equals(loginChoice.getValue().getValueAttribute());
    }

    private void onSelectionChange(Object selected) {
        boolean isUseLogin = isGroupLogin();

        username.setAllowBlank(!isUseLogin);
        username.setEnabled(isUseLogin);

        password.setAllowBlank(!isUseLogin);
        password.setEnabled(isUseLogin);

        passConfirm.setAllowBlank(!isUseLogin);
        passConfirm.setEnabled(isUseLogin);
    }
}
