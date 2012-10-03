package nl.sense_os.commonsense.main.client.groups.create.components;

import nl.sense_os.commonsense.main.client.ext.component.WizardFormPanel;

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

public class GroupPresetsForm extends WizardFormPanel {

    private final Radio anonymous = new Radio();
    private final Radio hidden = new Radio();
    private final Radio community = new Radio();
    private final Radio custom = new Radio();
    private final RadioGroup presets = new RadioGroup();

    private final TextField<String> password = new TextField<String>();
    private final TextField<String> passwordConfirm = new TextField<String>();

    public GroupPresetsForm() {
        super();

        initRadios();
        initTextFields();
        initLayout();

        anonymous.setValue(true);
        onSelectionChange(anonymous);
    }

    public String getAccessPassword() {
        return passwordConfirm.isEnabled() && passwordConfirm.isValid()
                ? password.getValue()
                : null;
    }

    public String getPresetChoice() {
        return presets.getValue().getValueAttribute();
    }

    public RadioGroup getPresets() {
        return presets;
    }

    private void initLayout() {

        LabelField mainLabel = new LabelField("<b>Group presets</b>");
        mainLabel.setHideLabel(true);

        LabelField anonyLabel = new LabelField(
                "An anonymous group is visible for everyone and anyone can join the group."
                        + "<br>" + "Members share their sensor data anonymously.");
        anonyLabel.setHideLabel(true);

        LabelField hiddenLabel = new LabelField(
                "A private group is hidden and the group has a password for new members." + "<br>"
                        + "Members can see eachother's shared sensors and all user details.");
        hiddenLabel.setHideLabel(true);
        hiddenLabel.setEnabled(false);

        LabelField communityLabel = new LabelField(
                "A community group is visible for everyone and anyone can join the group." + "<br>"
                        + "Members can see eachother's shared sensors and some user details.");
        communityLabel.setHideLabel(true);

        LabelField customLabel = new LabelField("Only for true sensei with special needs.");
        customLabel.setHideLabel(true);

        // init layout
        FormPanel pwForm = new FormPanel();
        pwForm.setLayout(new FormLayout(LabelAlign.LEFT));
        pwForm.setHeaderVisible(false);
        pwForm.setBodyBorder(false);
        pwForm.add(password, new FormData(anchorSpec));
        pwForm.add(passwordConfirm, new FormData(anchorSpec));

        setLayout(new FormLayout(LabelAlign.LEFT));
        add(mainLabel, new FormData(anchorSpec));
        add(hidden, new FormData(anchorSpec));
        add(hiddenLabel, new FormData(anchorSpec));
        add(pwForm, new FormData(anchorSpec));
        add(anonymous, new FormData(anchorSpec));
        add(anonyLabel, new FormData(anchorSpec));
        add(community, new FormData(anchorSpec));
        add(communityLabel, new FormData(anchorSpec));
        add(custom, new FormData(anchorSpec));
        add(customLabel, new FormData(anchorSpec));
    }

    private void initRadios() {

        // radio presets manages selection
        presets.setSelectionRequired(true);
        presets.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange(be.getField().getValue());
            }
        });

        anonymous.setBoxLabel("Anonymous");
        anonymous.setValueAttribute("anonymous");
        anonymous.setHideLabel(true);
        presets.add(anonymous);

        community.setBoxLabel("Community");
        community.setValueAttribute("community");
        community.setHideLabel(true);
        presets.add(community);

        hidden.setBoxLabel("Private");
        hidden.setValueAttribute("private");
        hidden.setHideLabel(true);
        hidden.setEnabled(false);
        presets.add(hidden);

        custom.setBoxLabel("Custom");
        custom.setValueAttribute("custom");
        custom.setHideLabel(true);
        presets.add(custom);
    }

    private void initTextFields() {

        password.setFieldLabel("Password");
        password.setPassword(true);
        password.setEnabled(false);

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
        passwordConfirm.setEnabled(false);
    }

    private void onSelectionChange(Object selection) {
        boolean usePasswords = hidden.equals(selection);

        password.setEnabled(usePasswords);
        password.setAllowBlank(!usePasswords);

        passwordConfirm.setEnabled(usePasswords);
        passwordConfirm.setAllowBlank(!usePasswords);
    }
}
