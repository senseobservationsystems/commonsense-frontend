package nl.sense_os.commonsense.client.groups.create.components;

import nl.sense_os.commonsense.client.common.components.WizardFormPanel;

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

public class GroupReqSharingForm extends WizardFormPanel {

    private final TextArea required = new TextArea();
    private final TextArea optional = new TextArea();

    private final RadioGroup anonymous = new RadioGroup();
    private final Radio radioNotAnonymous = new Radio();
    private final Radio radioAnonymous = new Radio();

    private final CheckBox userId = new CheckBox();
    private final CheckBox username = new CheckBox();
    private final CheckBox firstName = new CheckBox();
    private final CheckBox surname = new CheckBox();
    private final CheckBox email = new CheckBox();
    private final CheckBox phone = new CheckBox();

    public GroupReqSharingForm() {
        super();

        initSharingFields();
        initAnonymousRadios();
        initMemberInfoCheckBoxes();
        initLayout();

        // initial values
        radioNotAnonymous.setValue(true);
        onSelectionChange(radioNotAnonymous);
        username.setValue(true);
        firstName.setValue(true);
        surname.setValue(true);
    }

    public RadioGroup getInfoRadios() {
        return anonymous;
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

        add(shareLabel, new FormData(anchorSpec));
        add(required, extraSpace);
        add(optional, extraSpace);

        add(infoLabel, new FormData(anchorSpec));
        add(radioNotAnonymous, new FormData(anchorSpec));
        add(checkBoxContainer, new FormData(anchorSpec));
        add(radioAnonymous, new FormData(anchorSpec));
    }

    private void initMemberInfoCheckBoxes() {
        userId.setBoxLabel("User ID");
        username.setBoxLabel("Username");
        firstName.setBoxLabel("First name");
        surname.setBoxLabel("Surname");
        email.setBoxLabel("Email");
        phone.setBoxLabel("Phone");
    }

    private void initAnonymousRadios() {

        anonymous.setName("anonymous");
        anonymous.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange(be.getField().getValue());
            }
        });

        radioNotAnonymous.setBoxLabel("Members need to share some information with the group:");
        radioNotAnonymous.setValueAttribute("false");
        radioNotAnonymous.setHideLabel(true);
        anonymous.add(radioNotAnonymous);

        radioAnonymous
                .setBoxLabel("Members are not required to share information about themselves");
        radioAnonymous.setValueAttribute("true");
        radioAnonymous.setHideLabel(true);
        anonymous.add(radioAnonymous);
    }

    private void initSharingFields() {
        required.setFieldLabel("Required shared sensors (comma-separated list)");
        optional.setFieldLabel("Optional shared sensors (comma-separated list)");
    }

    public boolean isEmailRequired() {
        return email.isEnabled() && email.getValue();
    }

    public boolean isFirstNameRequired() {
        return firstName.isEnabled() && firstName.getValue();
    }

    public boolean isPhoneRequired() {
        return phone.isEnabled() && phone.getValue();
    }

    public boolean isSurnameRequired() {
        return surname.isEnabled() && surname.getValue();
    }

    public boolean isUserIdRequired() {
        return userId.isEnabled() && userId.getValue();
    }

    public boolean isUsernameRequired() {
        return username.isEnabled() && username.getValue();
    }

    public boolean isGroupAnonymous() {
        return "true".equals(anonymous.getValue().getValueAttribute());
    }

    private void onSelectionChange(Object value) {
        boolean enableChecks = radioNotAnonymous.equals(value);
        userId.setEnabled(enableChecks);
        username.setEnabled(enableChecks);
        firstName.setEnabled(enableChecks);
        surname.setEnabled(enableChecks);
        email.setEnabled(enableChecks);
        phone.setEnabled(enableChecks);
    }
}
