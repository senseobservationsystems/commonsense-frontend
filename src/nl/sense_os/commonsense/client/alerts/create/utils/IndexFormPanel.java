package nl.sense_os.commonsense.client.alerts.create.utils;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Button;

public class IndexFormPanel extends FormPanel {
    private int index;
    private Button deleteButton;
    private SimpleComboBox<String> combo;
    private TextField<String> address;
    private TextArea description;

    public IndexFormPanel() {
        super();
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setButton(Button b) {
        this.deleteButton = b;
    }

    public void showDeleteButton(boolean visible) {
        this.deleteButton.setVisible(visible);
    }

    public void setCombo(SimpleComboBox<String> combo1) {
        this.combo = combo1;
    }

    public void setAddress(TextField<String> text) {
        this.address = text;
    }

    public void setDescription(TextArea text) {
        this.description = text;
    }

    public SimpleComboBox<String> getCombo() {
        return combo;
    }

    public TextField<String> getTextField() {
        return this.address;
    }

    public TextArea getTextArea() {
        return this.description;
    }

    public String getType() {
        String comboValue = combo.getSimpleValue();
        String name = null;
        if (comboValue != null) {
            name = comboValue;
        }
        return name;
    }

    public String getAddress() {
        String address1 = null;
        if (address.getValue() != null) {
            address1 = address.getValue();
        }
        return address1;
    }

    public String getDescription() {
        String description1 = null;
        if (description.getValue() != null) {
            description1 = description.getValue().toString();
        }
        return description1;
    }
}
