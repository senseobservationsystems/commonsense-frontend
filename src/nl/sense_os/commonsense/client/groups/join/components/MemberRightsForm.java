package nl.sense_os.commonsense.client.groups.join.components;

import nl.sense_os.commonsense.client.common.components.WizardFormPanel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class MemberRightsForm extends WizardFormPanel {

    private CheckBox readSensors;
    private CheckBox readMembers;
    private CheckBox createMembers;
    private CheckBox createSensors;
    private CheckBox deleteMembers;
    private CheckBox deleteSensors;

    public MemberRightsForm() {
        super();

        CheckBoxGroup memberRights = new CheckBoxGroup();
        memberRights.setOrientation(Orientation.VERTICAL);

        readSensors = new CheckBox();
        memberRights.add(readSensors);
        readSensors.setHideLabel(true);
        readSensors.setBoxLabel("See the group's sensors");
        readSensors.setReadOnly(true);

        readMembers = new CheckBox();
        memberRights.add(readMembers);
        readMembers.setBoxLabel("See the group members");
        readMembers.setHideLabel(true);
        readMembers.setReadOnly(true);

        createMembers = new CheckBox();
        memberRights.add(createMembers);
        createMembers.setBoxLabel("Add new group members");
        createMembers.setHideLabel(true);
        createMembers.setReadOnly(true);

        createSensors = new CheckBox();
        memberRights.add(createSensors);
        createSensors.setHideLabel(true);
        createSensors.setBoxLabel("Share sensors with the group");
        createSensors.setReadOnly(true);

        deleteMembers = new CheckBox();
        memberRights.add(deleteMembers);
        deleteMembers.setBoxLabel("Remove members from the group");
        deleteMembers.setHideLabel(true);
        deleteMembers.setReadOnly(true);

        deleteSensors = new CheckBox();
        memberRights.add(deleteSensors);
        deleteSensors.setHideLabel(true);
        deleteSensors.setBoxLabel("Remove sensors from the group");
        deleteSensors.setReadOnly(true);

        add(memberRights, new FormData(anchorSpec));
        memberRights.setFieldLabel("You will have the following rights in this group");
    }

    public void setMemberRights(boolean createMembers, boolean createSensors, boolean readMembers,
            boolean readSensors, boolean deleteMembers, boolean deleteSensors) {
        this.readMembers.setValue(readMembers);
        this.readSensors.setValue(readSensors);
        this.createMembers.setValue(createMembers);
        this.createSensors.setValue(createSensors);
        this.deleteMembers.setValue(deleteMembers);
        this.deleteSensors.setValue(deleteSensors);
    }

    public CheckBox getCreatemembers() {
        return createMembers;
    }

    public CheckBox getCreatesensors() {
        return createSensors;
    }

    public CheckBox getDeletemembers() {
        return deleteMembers;
    }

    public CheckBox getDeletesensors() {
        return deleteSensors;
    }

    public CheckBox getReadmembers() {
        return readMembers;
    }

    public CheckBox getReadsensors() {
        return readSensors;
    }
}
