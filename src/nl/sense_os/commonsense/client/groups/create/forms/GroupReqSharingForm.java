package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.HTML;

public class GroupReqSharingForm extends AbstractGroupForm {

    public class NoInfoRadio extends Radio {

    };
    public class ShowInfoRadio extends Radio {

    }

    private final TextArea required = new TextArea();
    private final TextArea optional = new TextArea();
    private final CheckBox anonymous = new CheckBox();

    private final RadioGroup infoRadios = new RadioGroup();
    private final ShowInfoRadio showInfo = new ShowInfoRadio();
    private final NoInfoRadio noInfo = new NoInfoRadio();

    private final CheckBox userId = new CheckBox();
    private final CheckBox username = new CheckBox();
    private final CheckBox firstName = new CheckBox();
    private final CheckBox surname = new CheckBox();
    private final CheckBox email = new CheckBox();
    private final CheckBox phone = new CheckBox();

    public GroupReqSharingForm() {
        super();

        initSharingFields();
        initMemberInfoRadios();
        initMemberInfoCheckBoxes();
        initLayout();

        // initial values
        showInfo.setValue(true);
        onSelectionChange(showInfo);
        username.setValue(true);
        firstName.setValue(true);
        surname.setValue(true);
    }

    public RadioGroup getInfoRadios() {
        return infoRadios;
    }

    public String getOptSensors() {
        return optional.getValue();
    }

    public String getReqSensors() {
        return required.getValue();
    }

    private void initLayout() {

        LabelField shareLabel = new LabelField("<b>Required shared sensors for new members</b>");
        shareLabel.setHideLabel(true);

        final LabelField infoLabel = new LabelField("<b>Required member information</b>");
        infoLabel.setHideLabel(true);

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

        FormData extraSpace = new FormData("-10");
        extraSpace.setMargins(new Margins(0, 10, 10, 0));

        add(shareLabel, layoutData);
        add(required, extraSpace);
        add(optional, extraSpace);
        add(anonymous, layoutData);

        add(infoLabel, layoutData);
        add(showInfo, layoutData);
        add(checkBoxContainer, layoutData);
        add(noInfo, layoutData);
    }

    private void initMemberInfoCheckBoxes() {
        userId.setBoxLabel("User ID");
        username.setBoxLabel("Username");
        firstName.setBoxLabel("First name");
        surname.setBoxLabel("Surname");
        email.setBoxLabel("Email");
        phone.setBoxLabel("Phone");
    }

    private void initMemberInfoRadios() {
        showInfo.setBoxLabel("Members need to share some information with the group:");
        showInfo.setHideLabel(true);
        noInfo.setBoxLabel("Member details are not visible to others");
        noInfo.setHideLabel(true);
        infoRadios.add(showInfo);
        infoRadios.add(noInfo);
        infoRadios.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange(be.getField().getValue());
            }
        });
    }

    private void initSharingFields() {
        required.setFieldLabel("Required shared sensors (comma-separated list)");
        optional.setFieldLabel("Optional shared sensors (comma-separated list)");
        anonymous.setBoxLabel("Sensors are shared anonymously");
        anonymous.setHideLabel(true);
    }

    public boolean isAnonymous() {
        return anonymous.getValue();
    }

    public boolean isEmail() {
        return email.getValue();
    }

    public boolean isFirstName() {
        return firstName.getValue();
    }

    public boolean isPhone() {
        return phone.getValue();
    }

    public boolean isSurname() {
        return surname.getValue();
    }

    public boolean isUserId() {
        return userId.getValue();
    }

    public boolean isUsername() {
        return username.getValue();
    }

    private void onSelectionChange(Object value) {
        boolean enableChecks = showInfo.equals(value);
        userId.setEnabled(enableChecks);
        username.setEnabled(enableChecks);
        firstName.setEnabled(enableChecks);
        surname.setEnabled(enableChecks);
        email.setEnabled(enableChecks);
        phone.setEnabled(enableChecks);
    }
}
