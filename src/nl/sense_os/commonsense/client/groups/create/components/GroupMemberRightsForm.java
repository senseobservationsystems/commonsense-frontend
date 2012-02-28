package nl.sense_os.commonsense.client.groups.create.components;

import nl.sense_os.commonsense.client.common.components.WizardFormPanel;

import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class GroupMemberRightsForm extends WizardFormPanel {

    private final CheckBox readUsers = new CheckBox();
    private final CheckBox createUsers = new CheckBox();
    private final CheckBox deleteUsers = new CheckBox();
    private final CheckBox readSensors = new CheckBox();
    private final CheckBox createSensors = new CheckBox();
    private final CheckBox deleteSensors = new CheckBox();

    public GroupMemberRightsForm() {
        super();

        initCheckBoxes();
        initLayout();

        // initial values
        readSensors.setValue(true);
        readUsers.setValue(true);
    }

    private void initCheckBoxes() {
        readUsers.setBoxLabel("Members can see other group members");
        readUsers.setHideLabel(true);
        createUsers.setBoxLabel("Members can add new group members");
        createUsers.setHideLabel(true);
        deleteUsers.setBoxLabel("Members can remove other group members");
        deleteUsers.setHideLabel(true);
        readSensors.setBoxLabel("Members can see the group sensors");
        readSensors.setHideLabel(true);
        createSensors.setBoxLabel("Members can share sensors with the group");
        createSensors.setHideLabel(true);
        deleteSensors.setBoxLabel("Members can remove group sensors");
        deleteSensors.setHideLabel(true);
    }

    private void initLayout() {

        final LabelField label = new LabelField("<b>Default group member rights</b>");
        label.setHideLabel(true);

        // layout
        add(label, new FormData(anchorSpec));
        add(readUsers, new FormData(anchorSpec));
        add(createUsers, new FormData(anchorSpec));
        add(deleteUsers, new FormData(anchorSpec));
        add(readSensors, new FormData(anchorSpec));
        add(createSensors, new FormData(anchorSpec));
        add(deleteSensors, new FormData(anchorSpec));
    }

    public boolean isCreateMembers() {
        return createUsers.getValue();
    }

    public boolean isCreateSensors() {
        return createSensors.getValue();
    }

    public boolean isDeleteMembers() {
        return deleteUsers.getValue();
    }

    public boolean isDeleteSensors() {
        return deleteSensors.getValue();
    }

    public boolean isReadMembers() {
        return readUsers.getValue();
    }

    public boolean isReadSensors() {
        return readSensors.getValue();
    }
}
