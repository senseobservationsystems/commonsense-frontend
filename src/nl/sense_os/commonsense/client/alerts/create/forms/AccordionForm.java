package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.AlertCreator;
import nl.sense_os.commonsense.client.alerts.create.triggers.AlertType;
import nl.sense_os.commonsense.client.alerts.create.utils.IndexContentPanel;
import nl.sense_os.commonsense.client.alerts.create.utils.IndexFormPanel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class AccordionForm extends ContentPanel {

    private static final Logger LOG = Logger.getLogger(AccordionForm.class.getName());
    private IndexContentPanel cp;
    private FormData formData;
    private LabelField titleLabel;
    private int parent_width;
    private List<String> comboValues;
    private ArrayList<IndexFormPanel> formList;
    private ArrayList<IndexContentPanel> contentList;
    private int currentIndex;
    private AlertCreator view;

    public AccordionForm() {
        super();
        LOG.setLevel(Level.ALL);
        setHeaderVisible(false);
        setBodyBorder(false);
        // setScrollMode(Scroll.AUTOY);
        setLayout(new AccordionLayout());
        formData = new FormData("-20");

        initLists();

        createFormPanel();
    }

    private void initLists() {

        if (formList != null)
            formList.clear();
        if (contentList != null)
            contentList.clear();

        formList = new ArrayList<IndexFormPanel>();
        contentList = new ArrayList<IndexContentPanel>();
    }

    public void createFormPanel() {
        cp = new IndexContentPanel();
        cp.setIndex(currentIndex);
        // LOG.fine ("Setting CP index at " + currentIndex);

        IndexFormPanel fp = new IndexFormPanel();
        fp.setBodyBorder(false);
        fp.setBorders(false);
        fp.setHeaderVisible(false);
        fp.setIndex(currentIndex);
        // LOG.fine ("Setting FP index at " + currentIndex);
        currentIndex++;

        contentList.add(cp);
        formList.add(fp);

        createSensorValues();
        createTitleLabel(fp);
        createFormFields(fp);

        cp.add(fp);
        this.add(cp);
        cp.expand();
        layout();
        // LOG.fine ("Finished creating FormPanel. Content list size is " + contentList.size());
    }

    public void createFormFields(IndexFormPanel fp1) {

        final IndexFormPanel fp = fp1;
        fp.setHeaderVisible(false);
        fp.setBorders(false);
        fp.setBodyBorder(false);
        // this.setLabelAlign(LabelAlign.LEFT);
        // this.setLabelWidth(90);
        fp.addStyleName("alertPanel");
        // LOG.fine ("Create formfields for " + fp.getIndex());

        SimpleComboBox<String> combo1 = createComboBox();
        combo1.setFieldLabel("Alert Type");
        combo1.addStyleName("formField");
        combo1.setAllowBlank(false);
        combo1.setWidth(350);
        fp.setCombo(combo1);
        fp.add(combo1, formData);

        TextField<String> address = new TextField<String>();
        address.setFieldLabel("Address");
        address.setWidth(350);
        address.addStyleName("formField");
        address.setAllowBlank(false);
        fp.setAddress(address);
        fp.add(address, formData);

        TextArea description = new TextArea();
        description.setPreventScrollbars(true);
        description.setFieldLabel("Message");
        description.addStyleName("formField");
        description.setHeight(100);
        description.setWidth(350);
        description.setAllowBlank(false);
        fp.setDescription(description);
        fp.add(description, formData);

        // listener for clicks on the buttons
        ClickHandler l = new ClickHandler() {

            @Override
            public void onClick(ClickEvent ce) {
                for (int i = 0; i < contentList.size(); i++) {
                    IndexContentPanel cp = contentList.get(i);
                    if (fp.getIndex() == cp.getIndex()) {
                        contentList.remove(cp);
                        formList.remove(fp);
                        remove(cp);
                        view.changeBinding();
                        checkDeleteButtons();
                        break;
                    }
                }
            }
        };

        Button deleteButton = new Button("Delete", l);
        deleteButton.addStyleName("formButton");
        fp.setButtonAlign(HorizontalAlignment.RIGHT);
        fp.add(deleteButton);

        fp.setButton(deleteButton);
        checkDeleteButtons();

    }

    /**
     * Create form title
     */
    private void createTitleLabel(IndexFormPanel fp1) {
        IndexFormPanel fp = fp1;
        titleLabel = new LabelField("<b>Alert Parameters</b>");
        titleLabel.setHideLabel(true);
        titleLabel.setStyleName("titleLabel3");
        fp.add(titleLabel);
    }

    /**
     * Create a ComboBox
     */
    private SimpleComboBox<String> createComboBox() {
        SimpleComboBox<String> combo = new SimpleComboBox<String>();
        combo.setDisplayField("name");
        combo.setHideLabel(false);
        combo.setAllowBlank(true);
        combo.add(comboValues);
        combo.setTriggerAction(TriggerAction.ALL);
        return combo;
    }

    /**
     * Initialize an arrayList of string sensor values
     */
    private void createSensorValues() {
        comboValues = new ArrayList<String>();
        comboValues.add("E-mail");
        comboValues.add("URL");
        comboValues.add("SMS");
    }

    public void collapseFormPanels() {
        if (contentList.size() > 1) {
            for (int i = 0; i < contentList.size() - 1; i++) {
                contentList.get(i).collapse();
            }
        }
    }

    public boolean checkValidFields() {

        boolean valid = false;

        for (int i = 0; i < formList.size(); i++) {
            IndexFormPanel pan = formList.get(i);
            if (pan.getType() != null && pan.getAddress() != null && pan.getDescription() != null)
                valid = true;
            else {
                valid = false;
                break;
            }
        }

        return valid;
    }

    private void checkDeleteButtons() {

        if (formList.size() > 1) {
            for (int i = 0; i < formList.size(); i++) {
                formList.get(i).showDeleteButton(true);
            }
        }

        else if (formList.size() == 1) {
            formList.get(0).showDeleteButton(false);
        }

        else
            return;
    }

    public FormPanel getFormPanel() {
        FormPanel fp = formList.get(formList.size() - 1);
        return fp;
    }

    /**
     * Resizes the graph according to parent window size (from AlertCreator)
     */
    public void passParentWindowSize(int width, int height) {
        // LOG.fine ("Window width is " + width + " window height is " + height);
        parent_width = width;
        final int SIZE = 170;

        for (int i = 0; i < formList.size(); i++) {
            IndexFormPanel fp = formList.get(i);
            SimpleComboBox<String> combo = fp.getCombo();
            combo.setWidth(parent_width - SIZE);
            TextField<String> address = fp.getTextField();
            address.setWidth(parent_width - SIZE);
            TextArea description = fp.getTextArea();
            description.setWidth(parent_width - SIZE);
        }
    }

    public ArrayList<AlertType> getAlertTypes() {
        ArrayList<AlertType> alertList = new ArrayList<AlertType>();

        for (int i = 0; i < formList.size(); i++) {
            AlertType alertType = new AlertType();
            alertType.setType(formList.get(i).getType());
            alertType.setAddress(formList.get(i).getAddress());
            alertType.setDescription(formList.get(i).getDescription());
            alertList.add(alertType);
        }

        return alertList;
    }

    public void setView(AlertCreator alertCreator) {
        this.view = alertCreator;
    }
}
