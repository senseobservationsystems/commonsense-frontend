package nl.sense_os.commonsense.main.client.alerts.create.components;

import java.util.Arrays;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class NotificationFields extends FieldSet {

    private SimpleComboBox<String> type;
    private TextField<String> address;
    private TextArea content;

    private Button btnRemove;
    private Button btnAdd;

    public NotificationFields() {

        setHeadingHtml("Notification details");
        setHeight(225);
        setLayout(new FormLayout(LabelAlign.TOP));

        type = new SimpleComboBox<String>();
        type.setFieldLabel("Type of notification");
        type.add(Arrays.asList("Email", "SMS text message", "Call URL"));
        type.setForceSelection(true);

        address = new TextField<String>();
        address.setFieldLabel("Address");
        address.setAllowBlank(false);

        content = new TextArea();
        content.setFieldLabel("Message");

        add(type, new FormData("-15"));
        add(address, new FormData("-15"));
        add(content, new FormData("-15"));

        // buttons
        btnRemove = new Button("Remove");
        btnRemove.setWidth(75);
        btnAdd = new Button("Add");
        btnAdd.setWidth(75);

        ButtonBar buttons = new ButtonBar();
        buttons.setAlignment(HorizontalAlignment.RIGHT);
        buttons.add(btnAdd);
        buttons.add(btnRemove);

        add(buttons, new FormData("-15"));
    }

    public Button getBtnAdd() {
        return btnAdd;
    }

    public Button getBtnRemove() {
        return btnRemove;
    }

    public String getNotificationAddress() {
        return address.getValue();
    }

    public String getNotificationContent() {
        return content.getValue();
    }

    public String getNotificationType() {
        return type.getSimpleValue();
    }
}
