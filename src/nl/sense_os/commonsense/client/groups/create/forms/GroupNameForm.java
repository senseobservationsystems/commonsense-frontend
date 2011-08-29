package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class GroupNameForm extends AbstractGroupForm {

    private final TextField<String> name = new TextField<String>();
    private final TextArea description = new TextArea();

    public GroupNameForm() {
        super();

        LabelField label = new LabelField("<b>Group name and description</b>");
        label.setHideLabel(true);

        // init name field
        name.setFieldLabel("Group name");
        name.setValue("foo");
        name.setAllowBlank(false);

        // init description field
        description.setFieldLabel("Group description (optional)");
        description.setAllowBlank(true);

        // init layout
        layoutData.setMargins(new Margins(0, 0, 10, 0));
        add(label, layoutData);
        add(name, layoutData);
        add(description, layoutData);
    }

    public TextField<String> getName() {
        return name;
    }

    public TextField<String> getDescription() {
        return description;
    }

    public String getNameValue() {
        return name.getValue();
    }

    public String getDescriptionValue() {
        return description.getValue();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public void setDescription(String description) {
        this.description.setValue(description);
    }
}
