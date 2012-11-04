package nl.sense_os.commonsense.main.client.groupmanagement.creating.component;

import nl.sense_os.commonsense.main.client.gxt.component.WizardFormPanel;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class GroupNameForm extends WizardFormPanel {

    private final TextField<String> name = new TextField<String>();
    private final TextArea description = new TextArea();

    public GroupNameForm() {
        super();

        initFields();
        initLayout();
    }

    private void initLayout() {

        LabelField label = new LabelField("<b>Group name and description</b>");
        label.setHideLabel(true);

        // layout
        FormData layoutData = new FormData(anchorSpec);
        layoutData.setMargins(new Margins(0, 0, 10, 0));
        add(label, layoutData);
        add(name, layoutData);
        add(description, layoutData);
    }

    private void initFields() {

        name.setFieldLabel("Group name");
        name.setAllowBlank(false);

        description.setFieldLabel("Group description (optional)");
        description.setAllowBlank(true);
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
