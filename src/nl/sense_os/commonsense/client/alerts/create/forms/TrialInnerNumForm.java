package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.google.gwt.core.client.JsArray;

public class TrialInnerNumForm extends AbstractAlertForm{
	
    private JsArray<Timeseries> data;
    private TrialNumForm parentForm;
    private Logger LOG = Logger.getLogger(TrialInnerNumForm.class.getName());
    
    public TrialInnerNumForm (List<SensorModel> sensors, long start, long end, boolean subsample, String title) {
	    	super();
	        LOG.setLevel(Level.ALL);	
	        setHeaderVisible(false);
	        setBodyBorder(false);
	        visualize(sensors, start, end, subsample);
    }
	
	@Override
    protected void onNewData(JsArray<Timeseries> data) {
    	LOG.fine ("Hey got data. Length is " + data.get(0).getData().length());
    	this.data = data;
    	parentForm.passData(data);
	
    }
	
//	public JsArray<Timeseries> getData() {
//		return this.data;
//	}
	
	public void setParent(TrialNumForm trialNumForm) {
 		parentForm = trialNumForm;
 	}
}
