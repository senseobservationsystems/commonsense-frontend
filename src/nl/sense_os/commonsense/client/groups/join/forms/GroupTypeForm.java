package nl.sense_os.commonsense.client.groups.join.forms;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class GroupTypeForm extends FormPanel {

    private RadioGroup typeChoice;

    public GroupTypeForm() {
        setHeaderVisible(false);
        setBodyBorder(false);
        setLabelAlign(LabelAlign.TOP);

        typeChoice = new RadioGroup("group type");
        typeChoice.setOrientation(Orientation.VERTICAL);
        typeChoice
                .setFieldLabel("Do you know the name of the group, or do you want to search the public groups?");

        Radio publicGroup = new Radio();
        publicGroup.setBoxLabel("Search public groups");
        publicGroup.setValueAttribute("public");
        typeChoice.add(publicGroup);

        Radio privateGroup = new Radio();
        privateGroup.setBoxLabel("Enter a private group");
        privateGroup.setValueAttribute("private");
        typeChoice.add(privateGroup);

        typeChoice.setValue(publicGroup);

        add(typeChoice, new FormData("-20"));
    }

    public String getType() {
        return typeChoice.getValue().getValueAttribute();
    }
}
