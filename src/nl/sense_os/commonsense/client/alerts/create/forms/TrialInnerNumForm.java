package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DatePickerEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Label;

public class TrialInnerNumForm extends AbstractAlertForm{
	
    private JsArray<Timeseries> data;
    private TrialNumForm parentForm;
    private Logger LOG = Logger.getLogger(TrialInnerNumForm.class.getName());
    private Date startDate;
    private Date endDate;
    
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
    	if (data.length() > 0) parentForm.passData(data);
    	else getSensibleData();
	
    }
	
	private void getSensibleData() {
		Window win = new  CenteredWindow();
		win.setHeading("Type in custom range");
        win.setSize(450, 250);
        win.setResizable(true);
        
        FormPanel simple = new FormPanel();  
        simple.setHeaderVisible(false);
        simple.setBodyBorder(false);
		
        LabelField label = new LabelField();
        label.setText("<b>No data found in the default range. Type in custom range</b>");
        
        DateField startField = new DateField();  
        startField.setFieldLabel("Start Date");
        startField.setHideLabel(false);
        startField.setWidth(50);
        
        startField.getDatePicker().addListener(Events.Select, new Listener<DatePickerEvent>() {
            public void handleEvent(DatePickerEvent be) {
                LOG.fine ("This date selected: " + be.getDate());
            }          
        });
        
        DateField endField = new DateField();  
        endField.setFieldLabel("End Date");
        endField.setHideLabel(false);
        endField.setWidth(50);
        endField.getDatePicker().addListener(Events.Select, new Listener<DatePickerEvent>() {
            public void handleEvent(DatePickerEvent be) {
                LOG.fine ("This date selected: " + be.getDate());
            }          
        });
        
        simple.add(label);
        simple.add(startField);  
        simple.add(endField);
        
		win.add(simple);
		win.show();
	}
	
	public void setParent(TrialNumForm trialNumForm) {
 		parentForm = trialNumForm;
 	}
}
