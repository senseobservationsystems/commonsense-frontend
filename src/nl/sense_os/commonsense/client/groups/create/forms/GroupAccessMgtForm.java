package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class GroupAccessMgtForm extends AbstractGroupForm {

    private final RadioGroup hidden = new RadioGroup();
    private final Radio radioNotHidden = new Radio();
    private final Radio radioHidden = new Radio();

    private final RadioGroup groupPublic = new RadioGroup();
    private final Radio radioPublic = new Radio();
    private final Radio radioNotPublicPass = new Radio();
    private final Radio radioNotPublic = new Radio();

    private final TextField<String> password = new TextField<String>();
    private final TextField<String> passwordConfirm = new TextField<String>();

    public GroupAccessMgtForm() {
        super();

        initHidden();
        initPublic();
        initLayout();

        // set initial values
        radioNotHidden.setValue(true);
        radioPublic.setValue(true);
        onSelectionChange(radioNotPublic);
    }

    public RadioGroup getJoinPolicy() {
        return groupPublic;
    }

    public String getPassword() {
        return passwordConfirm.isEnabled() && passwordConfirm.isValid()
                ? password.getValue()
                : null;
    }

    private void initPublic() {

        groupPublic.setName("public");
        groupPublic.setSelectionRequired(true);
        groupPublic.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange((Radio) be.getField().getValue());
            }
        });

        radioPublic.setBoxLabel("Everyone can freely join the group");
        radioPublic.setValueAttribute("true");
        radioPublic.setHideLabel(true);
        groupPublic.add(radioPublic);

        radioNotPublicPass.setBoxLabel("There is an access password for new members");
        radioNotPublicPass.setValueAttribute("pass");
        radioNotPublicPass.setHideLabel(true);
        groupPublic.add(radioNotPublicPass);

        radioNotPublic.setBoxLabel("New members need to be accepted by an admin");
        radioNotPublic.setValueAttribute("false");
        radioNotPublic.setHideLabel(true);
        groupPublic.add(radioNotPublic);

        initPasswordFields();
    }

    private void initLayout() {

        LabelField visibleLabel = new LabelField("<b>Group hidden</b>");
        visibleLabel.setHideLabel(true);

        LabelField policyLabel = new LabelField("<b>Policy for new members</b>");
        policyLabel.setHideLabel(true);

        FormPanel pwForm = new FormPanel();
        pwForm.setLayout(new FormLayout(LabelAlign.LEFT));
        pwForm.setHeaderVisible(false);
        pwForm.setBodyBorder(false);
        pwForm.add(password, layoutData);
        pwForm.add(passwordConfirm, layoutData);

        FormData extraSpace = new FormData("-10");
        extraSpace.setMargins(new Margins(20, 0, 0, 0));

        setLayout(new FormLayout(LabelAlign.LEFT));
        add(visibleLabel, layoutData);
        add(radioNotHidden, layoutData);
        add(radioHidden, layoutData);
        add(policyLabel, extraSpace);
        add(radioPublic, layoutData);
        add(radioNotPublic, layoutData);
        add(radioNotPublicPass, layoutData);
        add(pwForm, layoutData);
    }

    private void initPasswordFields() {

        password.setFieldLabel("Group password");
        password.setPassword(true);

        passwordConfirm.setFieldLabel("Confirm password");
        passwordConfirm.setPassword(true);
        passwordConfirm.setValidator(new Validator() {

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

    private void initHidden() {

        hidden.setName("hidden");
        hidden.setOrientation(Orientation.VERTICAL);
        hidden.setSelectionRequired(true);

        radioNotHidden.setBoxLabel("Everyone can see that this group exists");
        radioNotHidden.setValueAttribute("false");
        radioNotHidden.setHideLabel(true);
        hidden.add(radioNotHidden);

        radioHidden.setBoxLabel("Group is hidden if you are not a member");
        radioHidden.setValueAttribute("true");
        radioHidden.setHideLabel(true);
        hidden.add(radioHidden);
    }

    public boolean isGroupHidden() {
        return "true".equals(hidden.getValue().getValueAttribute());
    }

    public boolean isGroupPublic() {
        return "true".equals(groupPublic.getValue().getValueAttribute());
    }

    private void onSelectionChange(Radio selection) {
        boolean isPassRequired = selection.equals(radioNotPublicPass);

        password.setEnabled(isPassRequired);
        password.setAllowBlank(!isPassRequired);

        passwordConfirm.setEnabled(isPassRequired);
        passwordConfirm.setAllowBlank(!isPassRequired);
    }
}
