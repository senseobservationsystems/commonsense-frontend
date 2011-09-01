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
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class GroupPresetsForm extends AbstractGroupForm {

    public class AnonymousRadio extends Radio {

    };

    public class PrivateRadio extends Radio {

    };

    public class CommunityRadio extends Radio {

    }

    public class CustomRadio extends Radio {

    }

    private final AnonymousRadio anonymous = new AnonymousRadio();
    private final PrivateRadio hidden = new PrivateRadio();
    private final CommunityRadio community = new CommunityRadio();
    private final CustomRadio custom = new CustomRadio();
    private final RadioGroup presets = new RadioGroup();

    private final TextField<String> password = new TextField<String>();
    private final TextField<String> passwordConfirm = new TextField<String>();

    public GroupPresetsForm() {

        initRadios();
        initTextFields();
        initLayout();

        hidden.setValue(true);
        onSelectionChange(hidden);
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
        pwForm.add(password, layoutData);
        pwForm.add(passwordConfirm, layoutData);

        setLayout(new FormLayout(LabelAlign.LEFT));
        add(mainLabel, layoutData);
        add(hidden, layoutData);
        add(hiddenLabel, layoutData);
        add(pwForm, layoutData);
        add(anonymous, layoutData);
        add(anonyLabel, layoutData);
        add(community, layoutData);
        add(communityLabel, layoutData);
        add(custom, layoutData);
        add(customLabel, layoutData);
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

    private void initRadios() {

        hidden.setBoxLabel("Private");
        hidden.setHideLabel(true);
        anonymous.setBoxLabel("Anonymous");
        anonymous.setHideLabel(true);
        community.setBoxLabel("Community");
        community.setHideLabel(true);
        custom.setBoxLabel("Custom");
        custom.setHideLabel(true);

        // radio presets manages selection
        presets.add(anonymous);
        presets.add(hidden);
        presets.add(community);
        presets.add(custom);
        presets.setSelectionRequired(true);
        presets.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange(be.getField().getValue());
            }
        });
    }

    private void onSelectionChange(Object selection) {
        boolean usePasswords = hidden.equals(selection);

        password.setEnabled(usePasswords);
        password.setAllowBlank(!usePasswords);

        passwordConfirm.setEnabled(usePasswords);
        passwordConfirm.setAllowBlank(!usePasswords);
    }

    public RadioGroup getPresets() {
        return presets;
    }

    public String getPrivatePass() {
        return passwordConfirm.isEnabled() && passwordConfirm.isValid()
                ? password.getValue()
                : null;
    }
}
