package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.utils.NewRangeRequest;
import nl.sense_os.commonsense.client.alerts.create.utils.StringSensorValue;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.google.gwt.core.client.JsArray;

public class InnerStringForm extends AbstractAlertForm {

    private Logger LOG = Logger.getLogger(InnerStringForm.class.getName());
    InnerStringForm form = this;
    private StringTriggerForm parentForm;

    private List<StringSensorValue> stringSensorValues;
    private ListStore<StringSensorValue> store;
    private boolean subsample;

    // private JsArray<Timeseries> data;

    public InnerStringForm(List<SensorModel> sensors, long start, long end, boolean subsample,
            String title) {
        super();
        LOG.setLevel(Level.ALL);
        this.setLayoutOnChange(true);
        layoutData.setMargins(new Margins(0, 0, 10, 0));
        this.setButtonAlign(HorizontalAlignment.RIGHT);
        stringSensorValues = new ArrayList<StringSensorValue>();
        this.subsample = subsample;
        store = new ListStore<StringSensorValue>();

        visualize(sensors, start, end, subsample);
    }

    @Override
    protected void onNewData(JsArray<Timeseries> data) {
        LOG.fine("Hey got data");
        // LOG.fine ("Datatype is " + datatype);
        int records = data.get(0).getData().length();
        LOG.fine("Number of records: " + records);

        if (records > 0 ) {
	        ArrayList<String> values = new ArrayList<String>();
	        
	        for (int i = 0; i < records; i++) {
	            String el = data.get(0).getData().get(i).getRawValue();
	            if (!values.contains(el)) {
	                values.add(el);
	            }
	        }
	
	        stringSensorValues.clear();
	        stringSensorValues.add(new StringSensorValue("(no selection)"));
	
	        for (int i = 0; i < values.size(); i++) {
	            // LOG.fine ("Element " + i + " equals " + values.get(i));
	            stringSensorValues.add(new StringSensorValue(values.get(i)));
	            layout();
	
	        }
	        parentForm.passSensorValues(stringSensorValues);
	        
        } 
        
        else {
        	NewRangeRequest request = new NewRangeRequest(sensors, subsample, this); 
        }
    }

    public List<StringSensorValue> getStringSensorValues() {
        return this.stringSensorValues;
    }

    public void setParent(StringTriggerForm trialStringForm) {
        parentForm = trialStringForm;
    }
}
