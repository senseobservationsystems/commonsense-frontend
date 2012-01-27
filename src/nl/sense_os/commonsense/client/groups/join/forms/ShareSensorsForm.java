package nl.sense_os.commonsense.client.groups.join.forms;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.WizardFormPanel;

import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class ShareSensorsForm extends WizardFormPanel {

    private static final Logger LOG = Logger.getLogger(ShareSensorsForm.class.getName());
    private LabelField lblReqSensors;

    public ShareSensorsForm(List<String> reqSensors, List<String> optSensors) {
        super();

        LOG.setLevel(Level.ALL);

        lblReqSensors = new LabelField("foo");
        lblReqSensors.setHideLabel(true);
        setReqSensors(reqSensors);

        add(lblReqSensors, new FormData("-5"));
    }

    public void setReqSensors(List<String> sensorNames) {

        String labelTxt = "You are required to share some of your sensors to join this group: ";
        boolean visible = true;
        if (sensorNames.size() > 1) {
            for (String sensor : sensorNames) {
                labelTxt += "'" + sensor + "', ";
            }
            labelTxt = labelTxt.substring(0, labelTxt.length() - 2);
            visible = true;

        } else if (sensorNames.size() == 1) {
            labelTxt = "You are required to share your '" + sensorNames.get(0) + "' sensor.";
            visible = true;

        } else {
            labelTxt = "no required sensors";
            visible = false;
        }

        LOG.fine("Required sensors text: " + labelTxt);

        lblReqSensors.setText(labelTxt);
        lblReqSensors.setVisible(visible);
    }
}
