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

    private final RadioGroup radios = new RadioGroup();
    private final Radio useLogin = new Radio();
    private final Radio doNotUseLogin = new Radio();

    private final TextField<String> login = new TextField<String>();
    private final TextField<String> password = new TextField<String>();
    private final TextField<String> passConfirm = new TextField<String>();

    public GroupLoginForm() {

        LabelField label = new LabelField("<b>Group login:</b>");
        label.setHideLabel(true);

        initRadios();
        initTextFields();

        FormPanel subForm = new FormPanel();
        subForm.setLayout(new FormLayout(LabelAlign.LEFT));
        subForm.setHeaderVisible(false);
        subForm.setBodyBorder(false);
        subForm.add(login, new FormData("-10"));
        subForm.add(password, new FormData("-10"));
        subForm.add(passConfirm, new FormData("-10"));

        // layout
        setLayout(new FormLayout(LabelAlign.LEFT));
        add(label, layoutData);
        add(useLogin, layoutData);
        add(subForm, layoutData);
        add(doNotUseLogin, layoutData);

        useLogin.setValue(true);
        onSelectionChange(useLogin);
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
        useLogin.setBoxLabel("Create a user login for the new group");
        useLogin.setHideLabel(true);
        doNotUseLogin.setBoxLabel("This group has no user login");
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
        login.validate();

        password.setAllowBlank(!isUseLogin);
        password.setEnabled(isUseLogin);
        password.validate();

        passConfirm.setAllowBlank(!isUseLogin);
        passConfirm.setEnabled(isUseLogin);
        passConfirm.validate();
    }
}
