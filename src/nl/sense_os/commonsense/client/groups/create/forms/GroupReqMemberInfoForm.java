package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.HTML;

public class GroupReqMemberInfoForm extends AbstractGroupForm {

    private final RadioGroup radios = new RadioGroup();
    private final Radio showInfo = new Radio();
    private final Radio noInfo = new Radio();

    private final CheckBox userId = new CheckBox();
    private final CheckBox username = new CheckBox();
    private final CheckBox firstName = new CheckBox();
    private final CheckBox surname = new CheckBox();
    private final CheckBox email = new CheckBox();
    private final CheckBox phone = new CheckBox();

    public GroupReqMemberInfoForm() {
        super();

        final LabelField label = new LabelField("<b>Required member information</b>");
        label.setHideLabel(true);

        // radio buttons
        initRadios();

        // checkboxes
        userId.setBoxLabel("User ID");
        username.setBoxLabel("Username");
        firstName.setBoxLabel("First name");
        surname.setBoxLabel("Surname");
        email.setBoxLabel("Email");
        phone.setBoxLabel("Phone");

        TableLayout tableLayout = new TableLayout(4);
        LayoutContainer checkBoxContainer = new LayoutContainer(tableLayout);
        TableData tableData = new TableData("75px", "20px");
        checkBoxContainer.add(new HTML(), new TableData("20px", "10px"));
        checkBoxContainer.add(userId, tableData);
        checkBoxContainer.add(firstName, tableData);
        checkBoxContainer.add(email, tableData);
        checkBoxContainer.add(new HTML(), new TableData("20px", "10px"));
        checkBoxContainer.add(username, tableData);
        checkBoxContainer.add(surname, tableData);
        checkBoxContainer.add(phone, tableData);

        // layout
        add(label, layoutData);
        add(showInfo, layoutData);
        add(checkBoxContainer, layoutData);
        add(noInfo, layoutData);
    }

    private void initRadios() {
        showInfo.setBoxLabel("Members need to share some information with the group:");
        showInfo.setHideLabel(true);
        showInfo.setValue(true);
        noInfo.setBoxLabel("Members are not visible to others");
        noInfo.setHideLabel(true);
        radios.add(showInfo);
        radios.add(noInfo);
        radios.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                boolean enableChecks = showInfo.equals(be.getField().getValue());
                userId.setEnabled(enableChecks);
                username.setEnabled(enableChecks);
                firstName.setEnabled(enableChecks);
                surname.setEnabled(enableChecks);
                email.setEnabled(enableChecks);
                phone.setEnabled(enableChecks);
            }
        });
    }

}
