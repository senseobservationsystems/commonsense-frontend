package nl.sense_os.commonsense.client.alerts.create.components;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.core.client.JsArray;

/**
 * Main window that contains the alert creator wizard.
 */
public class AlertCreator extends CenteredWindow {

    private static final Logger LOG = Logger.getLogger(AlertCreator.class.getName());

    private CardLayout layout;

    private Button btnBack;
    private Button btnCancel;
    private Button btnNext;

    private NotificationsForm notificationForm;

    private StringTriggerForm stringTriggerForm;
    private NumTriggerPanel numTriggerPanel;
    private PosTriggerForm posTriggerForm;

    private FormButtonBinding formButtonBinding;

    public AlertCreator() {
        super();

        setHeading("Create new alert");
        setSize(500, 500);
        setResizable(true);
        setClosable(false);

        layout = new CardLayout();
        setLayout(layout);

        initButtons();
    }

    public Button getBackButton() {
        return btnBack;
    }

    public Button getCancelButton() {
        return btnCancel;
    }

    public Button getNextButton() {
        return btnNext;
    }

    private void initButtons() {
        btnNext = new Button("Next");
        btnNext.setIconStyle("sense-btn-icon-go");

        btnBack = new Button("Back");
        btnBack.setEnabled(false);

        btnCancel = new Button("Cancel");

        addButton(btnBack);
        addButton(btnNext);
        addButton(btnCancel);
    }

    public void onNewNumData(JsArray<Timeseries> data) {
        LOG.fine("Hey got data. Length is " + data.get(0).getData().length());
        if (data.length() > 0) {
            numTriggerPanel.addData(data);
        } else {
            LOG.fine("No data received");
        }
    }

    public void onNewStringData(JsArray<Timeseries> data) {
        LOG.fine("Hey got data");
        // LOG.fine ("Datatype is " + datatype);
        int records = data.get(0).getData().length();
        LOG.fine("Number of records: " + records);

        if (records > 0) {
            ArrayList<String> values = new ArrayList<String>();

            for (int i = 0; i < records; i++) {
                String el = data.get(0).getData().get(i).getRawValue();
                if (!values.contains(el)) {
                    values.add(el);
                }
            }

            List<String> stringSensorValues = new ArrayList<String>();
            stringSensorValues.add("(no selection)");

            for (int i = 0; i < values.size(); i++) {
                // LOG.fine ("Element " + i + " equals " + values.get(i));
                stringSensorValues.add(values.get(i));

            }
            stringTriggerForm.passSensorValues(stringSensorValues);

        }

        else {
            // NewRangeRequest request = new NewRangeRequest(sensors, subsample, this);
        }
    }

    public void showNotificationsForm() {

        if (null == notificationForm) {
            notificationForm = new NotificationsForm();
            add(notificationForm);
        }

        layout.setActiveItem(notificationForm);

        formButtonBinding = new FormButtonBinding(notificationForm.getFormPanel());

        btnNext.setText("Submit");
        btnBack.setEnabled(true);
    }

    public void showNumTriggerForm() {

        if (null == numTriggerPanel) {
            numTriggerPanel = new NumTriggerPanel();
            add(numTriggerPanel);
        }

        layout.setActiveItem(numTriggerPanel);

        if (null != formButtonBinding) {
            formButtonBinding.removeButton(btnNext);
        }
        formButtonBinding = new FormButtonBinding(numTriggerPanel.getForm());
        formButtonBinding.addButton(btnNext);

        btnNext.setText("Next");
        btnBack.setEnabled(false);
    }

    public void showPosTriggerForm() {
        if (null == posTriggerForm) {
            posTriggerForm = new PosTriggerForm();
            add(posTriggerForm);
        }

        layout.setActiveItem(posTriggerForm);

        if (null != formButtonBinding) {
            formButtonBinding.removeButton(btnNext);
        }

        formButtonBinding = new FormButtonBinding(posTriggerForm);
        formButtonBinding.addButton(btnNext);

        // not sure if this works here
        if (layout.getActiveItem().equals(posTriggerForm)) {
            posTriggerForm.afterShow();
        }

        btnNext.setText("Next");
        btnBack.setEnabled(false);
    }

    public void showStringTriggerForm() {

        if (null == stringTriggerForm) {
            stringTriggerForm = new StringTriggerForm();
            add(stringTriggerForm);
        }

        layout.setActiveItem(stringTriggerForm);
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(btnNext);
        }

        formButtonBinding = new FormButtonBinding(stringTriggerForm);
        formButtonBinding.addButton(btnNext);

        btnNext.setText("Next");
        btnBack.setEnabled(false);
    }
}
