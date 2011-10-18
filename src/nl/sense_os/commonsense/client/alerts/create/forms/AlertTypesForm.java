package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.triggers.AlertType;
import nl.sense_os.commonsense.client.alerts.create.utils.StringSensorValue;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AlertTypesForm extends FormPanel {

    private VerticalPanel vp;
    private FormData formData;

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(AlertTypesForm.class.getName());
    private LabelField titleLabel;
    private int parent_width;
    @SuppressWarnings("unchecked")
    private ComboBox combo1;
    private TextArea description;
    private TextField<String> address;

    private List<StringSensorValue> stringSensorValues;
    private ListStore<StringSensorValue> store;

    public AlertType getAlertType() {
        AlertType alertType = new AlertType();

        StringSensorValue combo1Value = (StringSensorValue) combo1.getValue();
        if (combo1Value != null)
            alertType.setType(combo1Value.getName());
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

    public AlertTypesForm() {
        super();
        setHeaderVisible(false);
        setBodyBorder(false);


        // setScrollMode(Scroll.AUTOY);
        
        //setLayout(new AccordionLayout());  
        //setIcon(Resources.ICONS.accordion());  

        formData = new FormData("-20");

        vp = new VerticalPanel();
        vp.setSpacing(10);

        createSensorValues();
        createTitleLabel();
        createFormFields();

        this.add(vp);
    }

    public void setFieldsBlank() {
        combo1.setRawValue("");
        address.setValue("");
        description.setValue("");
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

    /**
     * Create a ComboBox and assign its Store
     */

    @SuppressWarnings("unchecked")
    private ComboBox createComboBox() {
        ComboBox<StringSensorValue> combo = new ComboBox<StringSensorValue>();
        combo.setDisplayField("name");
        combo.setHideLabel(false);
        combo.setStore(store);
        combo.setAllowBlank(true);
        combo.setTriggerAction(TriggerAction.ALL);

        return combo;
    }

    /**
     * Initialize an arrayList of string sensor values
     */

    private void createSensorValues() {
        stringSensorValues = new ArrayList<StringSensorValue>();
        stringSensorValues.add(new StringSensorValue("E-mail"));
        stringSensorValues.add(new StringSensorValue("URL"));
        stringSensorValues.add(new StringSensorValue("SMS"));

        store = new ListStore<StringSensorValue>();
        store.add(stringSensorValues);
    }

    public void getNewForm() {
        combo1.setRawValue(null);
        address.setRawValue(null);

    }

    public void setDescription(String message) {
        description.setValue(message);
    }

    public void createFormFields() {

        this.setHeaderVisible(false);
        this.setBorders(false);
        this.setBodyBorder(false);
        this.setLabelAlign(LabelAlign.LEFT);
        this.setLabelWidth(90);
        this.addStyleName("alertPanel");

        combo1 = createComboBox();
        combo1.setFieldLabel("Alert Type");
        combo1.addStyleName("formField");
        combo1.setAllowBlank(false);
        this.add(combo1, formData);

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
     * Resizes the graph according to parent window size (from AlertCreator)
     */

    public void passParentWindowSize(int width, int height) {
        // LOG.fine ("Window width is " + width + " window height is " + height);
        parent_width = width;
        final int SIZE = 170;
        address.setWidth(parent_width - SIZE);
        combo1.setWidth(parent_width - SIZE);
        description.setWidth(parent_width - SIZE);
    }

}
