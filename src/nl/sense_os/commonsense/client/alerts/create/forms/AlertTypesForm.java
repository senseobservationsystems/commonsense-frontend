package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.triggers.AlertType;

import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AlertTypesForm extends FormPanel {

    private static final Logger LOG = Logger.getLogger(AlertTypesForm.class.getName());
    private VerticalPanel vp;
    private FormData formData;
    private LabelField titleLabel;
    private int parent_width;
    private SimpleComboBox<String> typeCombo;
    private TextArea description;
    private TextField<String> address;

    public AlertTypesForm() {
        super();

        LOG.finest("Create form");

        setHeaderVisible(false);
        setBodyBorder(false);

        // setScrollMode(Scroll.AUTOY);

        // setLayout(new AccordionLayout());
        // setIcon(Resources.ICONS.accordion());

        formData = new FormData("-20");

        vp = new VerticalPanel();
        vp.setSpacing(10);

        createTitleLabel();
        createFormFields();

        this.add(vp);
    }

    /**
     * Create a ComboBox and assign its Store
     */
    private SimpleComboBox<String> createComboBox() {
        SimpleComboBox<String> combo = new SimpleComboBox<String>();
        combo.setHideLabel(false);
        combo.setAllowBlank(true);
        combo.setTriggerAction(TriggerAction.ALL);
        combo.add("Email");
        combo.add("URL");
        combo.add("SMS");
        combo.setSimpleValue("Email");

        return combo;
    }

    public void createFormFields() {

        this.setHeaderVisible(false);
        this.setBorders(false);
        this.setBodyBorder(false);
        this.setLabelAlign(LabelAlign.LEFT);
        this.setLabelWidth(90);
        this.addStyleName("alertPanel");

        typeCombo = createComboBox();
        typeCombo.setFieldLabel("Alert Type");
        typeCombo.addStyleName("formField");
        typeCombo.setAllowBlank(false);
        this.add(typeCombo, formData);

        address = new TextField<String>();
        address.setFieldLabel("Address");
        address.setWidth(350);
        address.addStyleName("formField");
        address.setAllowBlank(false);
        this.add(address, formData);

        description = new TextArea();
        description.setPreventScrollbars(true);
        description.setFieldLabel("Message");
        description.addStyleName("formField");
        description.setHeight(100);
        description.setAllowBlank(false);
        this.add(description, formData);
    }

    /**
     * Create form title
     */

    private void createTitleLabel() {
        titleLabel = new LabelField("<b>Alert Parameters</b>");
        titleLabel.setHideLabel(true);
        titleLabel.setStyleName("titleLabel3");
        this.add(titleLabel);
    }

    public AlertType getAlertType() {
        AlertType alertType = new AlertType();

        String combo1Value = typeCombo.getSimpleValue();
        if (combo1Value != null)
            alertType.setType(combo1Value);
        else
            return null;

        if (address.getValue() != null)
            alertType.setAddress(address.getValue());
        else
            return null;

        if (description.getValue() != null)
            alertType.setDescription(description.getValue());
        else
            return null;

        return alertType;
    }

    public void getNewForm() {
        typeCombo.setRawValue(null);
        address.setRawValue(null);

    }

    /**
     * Resizes the graph according to parent window size (from AlertCreator)
     */

    public void passParentWindowSize(int width, int height) {
        // LOG.fine ("Window width is " + width + " window height is " + height);
        parent_width = width;
        final int SIZE = 170;
        address.setWidth(parent_width - SIZE);
        typeCombo.setWidth(parent_width - SIZE);
        description.setWidth(parent_width - SIZE);
    }

    public void setDescription(String message) {
        description.setValue(message);
    }

    public void setFieldsBlank() {
        typeCombo.setRawValue("");
        address.setValue("");
        description.setValue("");
    }

}
