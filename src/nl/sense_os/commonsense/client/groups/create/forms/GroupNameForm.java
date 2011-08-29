package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.widget.form.TextField;

public class GroupNameForm extends AbstractGroupForm {

    private final TextField<String> name = new TextField<String>();
    private final TextField<String> description = new TextField<String>();

    public GroupNameForm() {
        super();

        // init name field
        name.setFieldLabel("Group name");
        name.setValue("foo");
        name.setAllowBlank(false);

        // init description field
        description.setFieldLabel("Group description (optional)");
        description.setAllowBlank(true);
        description.setHeight("100px");

        // init layout
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
