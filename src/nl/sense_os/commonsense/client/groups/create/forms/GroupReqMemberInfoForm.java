package nl.sense_os.commonsense.client.groups.create.forms;

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

        final LabelField label = new LabelField(
                "<b>Requirements for shared member information:</b>");
        label.setHideLabel(true);

        // radio buttons
        showInfo.setBoxLabel("Members need to share some information with the group:");
        showInfo.setHideLabel(true);
        showInfo.setValue(true);
        noInfo.setBoxLabel("Members are not visible to others");
        noInfo.setHideLabel(true);
        radios.add(showInfo);
        radios.add(noInfo);

        // checkboxes
        userId.setBoxLabel("User ID");
        username.setBoxLabel("Username");
        firstName.setBoxLabel("First name");
        surname.setBoxLabel("Surname");
        email.setBoxLabel("Email");
        phone.setBoxLabel("Phone");

        TableLayout tableLayout = new TableLayout(3);
        LayoutContainer checkBoxContainer = new LayoutContainer(tableLayout);
        TableData tableData = new TableData("75px", "20px");
        checkBoxContainer.add(new HTML(), new TableData("20px", "10px"));
        checkBoxContainer.add(userId, tableData);
        checkBoxContainer.add(email, tableData);
        checkBoxContainer.add(new HTML(), new TableData("20px", "10px"));
        checkBoxContainer.add(username, tableData);
        checkBoxContainer.add(phone, tableData);
        checkBoxContainer.add(new HTML(), new TableData("20px", "10px"));
        checkBoxContainer.add(firstName, tableData);
        checkBoxContainer.add(new HTML(), new TableData("20px", "10px"));
        checkBoxContainer.add(new HTML(), new TableData("20px", "10px"));
        checkBoxContainer.add(surname, tableData);
        checkBoxContainer.add(new HTML(), new TableData("20px", "10px"));

        // layout
        add(label, layoutData);
        add(showInfo, layoutData);
        add(checkBoxContainer, layoutData);
        add(noInfo, layoutData);
    }

}
