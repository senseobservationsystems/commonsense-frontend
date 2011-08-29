package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.widget.form.TextField;

public class GroupReqSharingForm extends AbstractGroupForm {

    private TextField<String> required = new TextField<String>();
    private final TextField<String> optional = new TextField<String>();

    public GroupReqSharingForm() {
        super();

        required.setFieldLabel("Required shared sensors (comma-separated list)");
        required.setHeight(100);

        optional.setFieldLabel("Optional shared sensors (comma-separated list)");
        optional.setHeight(100);

        // init layout
        add(required, layoutData);
        add(optional, layoutData);
    }
}
