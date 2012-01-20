package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.google.gwt.core.client.JsArray;

public class InnerNumForm extends AbstractAlertForm {

    private static final Logger LOG = Logger.getLogger(InnerNumForm.class.getName());
    private NumTriggerForm parentForm;

    public InnerNumForm(List<SensorModel> sensors, long start, long end, boolean subsample,
            String title) {
        super();
        // LOG.setLevel(Level.ALL);
        setHeaderVisible(false);
        setBodyBorder(false);
        visualize(sensors, start, end, subsample);
    }

    @Override
    protected void onNewData(JsArray<Timeseries> data) {
        LOG.fine("Hey got data. Length is " + data.get(0).getData().length());
        this.data = data;
        if (data.length() > 0)
            parentForm.passData(data);
        else {
            // NewRangeRequest request = new NewRangeRequest(sensors, subsample, this);
        }
    }

    public void setParent(NumTriggerForm trialNumForm) {
        parentForm = trialNumForm;
    }
}
