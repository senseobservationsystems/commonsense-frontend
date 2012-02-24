package nl.sense_os.commonsense.client.groups.join.components;

import nl.sense_os.commonsense.client.common.components.WizardFormPanel;

import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class GroupNameForm extends WizardFormPanel {

    private final TextField<String> groupName;
    private final TextField<String> password;

    public GroupNameForm() {
        super();

        setHeaderVisible(false);
        setBodyBorder(false);

        LabelField label = new LabelField(
                "Enter the name and access password of the group you want to join. If the group does not have a password, leave it blank.");
        label.setHideLabel(true);

        groupName = new TextField<String>();
        groupName.setFieldLabel("Group name");
        groupName.setAllowBlank(false);

        password = new TextField<String>();
        password.setFieldLabel("Access password");

        add(label, new FormData("-5"));
        add(groupName, new FormData(anchorSpec));
        add(password, new FormData(anchorSpec));
    }

    public String getGroupName() {
        return groupName.getValue();
    }

    public String getPassword() {
        return password.getValue();
    }
}
