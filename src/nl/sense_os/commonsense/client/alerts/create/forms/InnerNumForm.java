package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.utils.NewRangeRequest;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.core.client.JsArray;

public class InnerNumForm extends AbstractAlertForm{
	
    private JsArray<Timeseries> data;
    private NumTriggerForm parentForm;
    private Logger LOG = Logger.getLogger(InnerNumForm.class.getName());
    private List<SensorModel> sensors;
    private boolean subsample;
    private Window win;
    
    public InnerNumForm (List<SensorModel> sensors, long start, long end, boolean subsample, String title) {
	    	super();
	        LOG.setLevel(Level.ALL);	
	        setHeaderVisible(false);
	        setBodyBorder(false);
	        this.sensors = sensors;
	        this.subsample = subsample;
	        visualize(sensors, start, end, subsample);
    }
	
	@Override
    protected void onNewData(JsArray<Timeseries> data) {
    	LOG.fine ("Hey got data. Length is " + data.get(0).getData().length());
    	this.data = data;
    	if (data.length() > 0) parentForm.passData(data);
    	else {
    		NewRangeRequest request = new NewRangeRequest(sensors, subsample, this); 
    	}	
    }
	
	
	public void setParent(NumTriggerForm trialNumForm) {
 		parentForm = trialNumForm;
 	}
}
