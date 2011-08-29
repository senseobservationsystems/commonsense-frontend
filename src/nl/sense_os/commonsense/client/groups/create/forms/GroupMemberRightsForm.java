package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;

public class GroupMemberRightsForm extends AbstractGroupForm {

    private final CheckBox readUsers = new CheckBox();
    private final CheckBox createUsers = new CheckBox();
    private final CheckBox deleteUsers = new CheckBox();
    private final CheckBox readSensors = new CheckBox();
    private final CheckBox createSensors = new CheckBox();
    private final CheckBox deleteSensors = new CheckBox();

    public GroupMemberRightsForm() {
        super();

        final LabelField label = new LabelField("<b>Default group member rights</b>");
        label.setHideLabel(true);

        readUsers.setBoxLabel("Members may see the other group members");
        readUsers.setHideLabel(true);
        readUsers.setValue(true);

        createUsers.setBoxLabel("Members may add new group members");
        createUsers.setHideLabel(true);

        deleteUsers.setBoxLabel("Members may remove the other group members");
        deleteUsers.setHideLabel(true);

        readSensors.setBoxLabel("Members may see the group sensors");
        readSensors.setHideLabel(true);
        readSensors.setValue(true);

        createSensors.setBoxLabel("Members may add new group sensors");
        createSensors.setHideLabel(true);

        deleteSensors.setBoxLabel("Members may remove group sensors");
        deleteSensors.setHideLabel(true);

        // layout
        add(label, layoutData);
        add(readUsers, layoutData);
        add(createUsers, layoutData);
        add(deleteUsers, layoutData);
        add(readSensors, layoutData);
        add(createSensors, layoutData);
        add(deleteSensors, layoutData);
    }
}
