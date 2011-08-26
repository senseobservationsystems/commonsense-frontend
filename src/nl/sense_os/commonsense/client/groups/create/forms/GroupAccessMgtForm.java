package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class GroupAccessMgtForm extends FormPanel {

    private final RadioGroup visibility = new RadioGroup();
    private final Radio visible = new Radio();
    private final Radio invisible = new Radio();

    private final RadioGroup joinPolicy = new RadioGroup();
    private final Radio freeEntrance = new Radio();
    private final Radio passEntrance = new Radio();
    private final TextField<String> privatePass = new TextField<String>();
    private final Radio adminEntrance = new Radio();

    public GroupAccessMgtForm() {

        visible.setBoxLabel("Everyone can see that this group exists");
        visible.setHideLabel(true);
        invisible.setBoxLabel("Group is hidden if you are not a member");
        invisible.setHideLabel(true);
        visibility.setFieldLabel("<b>Group visibility</b>");
        visibility.add(visible);
        visibility.add(invisible);
        visibility.setOrientation(Orientation.VERTICAL);
        visibility.setSelectionRequired(true);

        LabelField policyLabel = new LabelField("<b>Policy for new members:</b>");
        // policyLabel.setHideLabel(true);

        freeEntrance.setBoxLabel("New members do not need a password");
        freeEntrance.setHideLabel(true);
        passEntrance.setBoxLabel("There is an access password for new members");
        passEntrance.setHideLabel(true);
        adminEntrance.setBoxLabel("New members need to be accepted by an admin");
        adminEntrance.setHideLabel(true);
        joinPolicy.add(freeEntrance);
        joinPolicy.add(passEntrance);
        joinPolicy.add(adminEntrance);
        joinPolicy.setSelectionRequired(true);
        joinPolicy.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange((Radio) be.getField().getValue());
            }
        });

        privatePass.setEmptyText("Enter a group password");
        privatePass.setAllowBlank(false);
        privatePass.setHideLabel(true);
        privatePass.setEnabled(false);

        visible.setValue(true);
        freeEntrance.setValue(true);

        // init layout
        setHeaderVisible(false);
        setBodyBorder(false);
        setLayout(new FormLayout(LabelAlign.TOP));
        FormData data = new FormData("-10");
        add(visibility, data);
        add(policyLabel, data);
        add(freeEntrance, data);
        add(passEntrance, data);
        add(privatePass, data);
        add(adminEntrance, data);
    }

    private void onSelectionChange(Radio selection) {
        boolean isPassRequired = selection.equals(passEntrance);
        privatePass.setEnabled(isPassRequired);
        privatePass.setAllowBlank(!isPassRequired);
        privatePass.validate();
    }
}
