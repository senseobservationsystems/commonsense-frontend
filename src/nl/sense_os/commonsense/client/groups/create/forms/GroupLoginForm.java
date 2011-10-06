package nl.sense_os.commonsense.client.groups.create.forms;

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

public class GroupLoginForm extends AbstractGroupForm {

    public class UseLoginRadio extends Radio {

    };
    public class DoNotUseLoginRadio extends Radio {

    };

    private final RadioGroup radios = new RadioGroup();
    private final UseLoginRadio useLogin = new UseLoginRadio();
    private final DoNotUseLoginRadio doNotUseLogin = new DoNotUseLoginRadio();

    private final TextField<String> login = new TextField<String>();
    private final TextField<String> password = new TextField<String>();
    private final TextField<String> passConfirm = new TextField<String>();

    public GroupLoginForm() {

        initRadios();
        initTextFields();
        initLayout();

        // initial values
        useLogin.setValue(true);
        onSelectionChange(useLogin);
    }

    public RadioGroup getRadios() {
        return radios;
    }

    public String getLogin() {
        return login.isEnabled() && login.isValid() ? login.getValue() : null;
    }

    public String getPassword() {
        return passConfirm.isEnabled() && passConfirm.isValid() ? password.getValue() : null;
    }

    private void initLayout() {

        LabelField label = new LabelField("<b>Group login</b>");
        label.setHideLabel(true);

        FormPanel pwForm = new FormPanel();
        pwForm.setLayout(new FormLayout(LabelAlign.LEFT));
        pwForm.setHeaderVisible(false);
        pwForm.setBodyBorder(false);
        pwForm.add(login, new FormData("-10"));
        pwForm.add(password, new FormData("-10"));
        pwForm.add(passConfirm, new FormData("-10"));

        // layout
        setLayout(new FormLayout(LabelAlign.LEFT));
        add(label, layoutData);
        add(useLogin, layoutData);
        add(pwForm, layoutData);
        add(doNotUseLogin, layoutData);
    }

    private void initTextFields() {

        login.setFieldLabel("Group username");

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

    private void initRadios() {
        useLogin.setBoxLabel("The group needs to be able to log in as a user");
        useLogin.setHideLabel(true);
        doNotUseLogin.setBoxLabel("The group needs no user login");
        doNotUseLogin.setHideLabel(true);
        radios.add(useLogin);
        radios.add(doNotUseLogin);
        radios.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange(be.getField().getValue());
            }
        });
    }

    private void onSelectionChange(Object selected) {
        boolean isUseLogin = useLogin.equals(selected);

        login.setAllowBlank(!isUseLogin);
        login.setEnabled(isUseLogin);

        password.setAllowBlank(!isUseLogin);
        password.setEnabled(isUseLogin);

        passConfirm.setAllowBlank(!isUseLogin);
        passConfirm.setEnabled(isUseLogin);
    }
}
