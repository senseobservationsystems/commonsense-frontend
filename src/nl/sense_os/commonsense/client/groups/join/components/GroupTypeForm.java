package nl.sense_os.commonsense.client.groups.join.components;

import nl.sense_os.commonsense.client.common.components.WizardFormPanel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class GroupTypeForm extends WizardFormPanel {

    private RadioGroup typeChoice;

    public GroupTypeForm() {
        super();

        LabelField explanation = new LabelField(
                "Do you know the name of the group, or do you want to list all groups you can join?");
        explanation.setHideLabel(true);

        typeChoice = new RadioGroup("group type");
        typeChoice.setOrientation(Orientation.VERTICAL);
        typeChoice.setFieldLabel("Make a choice");

        Radio publicGroup = new Radio();
        publicGroup.setBoxLabel("Browse all groups");
        publicGroup.setValueAttribute("visible");
        typeChoice.add(publicGroup);

        Radio privateGroup = new Radio();
        privateGroup.setBoxLabel("Enter the group name");
        privateGroup.setValueAttribute("hidden");
        privateGroup.setEnabled(false);
        typeChoice.add(privateGroup);

        typeChoice.setValue(publicGroup);

        add(explanation, new FormData("-5"));
        add(typeChoice, new FormData(anchorSpec));
    }

    public String getType() {
        return typeChoice.getValue().getValueAttribute();
    }
}
