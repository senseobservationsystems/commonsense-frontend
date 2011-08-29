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

    private final RadioGroup visibility = new RadioGroup();
    private final Radio visible = new Radio();
    private final Radio invisible = new Radio();

    private final RadioGroup joinPolicy = new RadioGroup();
    private final Radio freeEntrance = new Radio();
    private final Radio passEntrance = new Radio();
    private final Radio adminEntrance = new Radio();

    private final TextField<String> password = new TextField<String>();
    private final TextField<String> passwordConfirm = new TextField<String>();

    public GroupAccessMgtForm() {
        super();

        LabelField visibleLabel = new LabelField("<b>Group visibility</b>");
        visibleLabel.setHideLabel(true);

        visible.setBoxLabel("Everyone can see that this group exists");
        visible.setHideLabel(true);
        invisible.setBoxLabel("Group is hidden if you are not a member");
        invisible.setHideLabel(true);
        visibility.add(invisible);
        visibility.add(visible);
        visibility.setOrientation(Orientation.VERTICAL);
        visibility.setSelectionRequired(true);

        LabelField policyLabel = new LabelField("<b>Policy for new members</b>");
        policyLabel.setHideLabel(true);

        freeEntrance.setBoxLabel("New members do not need a password");
        freeEntrance.setHideLabel(true);
        passEntrance.setBoxLabel("There is an access password for new members");
        passEntrance.setHideLabel(true);
        adminEntrance.setBoxLabel("New members need to be accepted by an admin");
        adminEntrance.setHideLabel(true);
        joinPolicy.add(freeEntrance);
        joinPolicy.add(passEntrance);
        joinPolicy.add(adminEntrance);
        joinPolicy.setSelectionRequired(true);
        joinPolicy.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange((Radio) be.getField().getValue());
            }
        });

        initTextFields();

        // init layout
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
        add(invisible, layoutData);
        add(visible, layoutData);
        add(policyLabel, extraSpace);
        add(passEntrance, layoutData);
        add(pwForm, layoutData);
        add(adminEntrance, layoutData);
        add(freeEntrance, layoutData);

        // set initial values
        invisible.setValue(true);
        passEntrance.setValue(true);
        onSelectionChange(passEntrance);
    }

    private void initTextFields() {
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

    private void onSelectionChange(Radio selection) {
        boolean isPassRequired = selection.equals(passEntrance);
        password.setEnabled(isPassRequired);
        password.setAllowBlank(!isPassRequired);

        passwordConfirm.setEnabled(isPassRequired);
        passwordConfirm.setAllowBlank(!isPassRequired);
    }
}
